package com.pingu.DOTORI.service;

import com.pingu.DOTORI.entity.*;
import com.pingu.DOTORI.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class InspectionService {

  private final ItemDetailsRepository itemDetailsRepo;
  private final ItemImgRepository itemImgRepo;
  private final AdminRepository adminRepo;
  private final UsersRepository usersRepo;
  private final FileStorageService storage;

  /** 판매자 검수 신청 생성 */
  @Transactional
  public CreateResult createInspection(Long userId,
                                       String productTitle, // 필요 시 별도 테이블/필드로 확장
                                       BigDecimal price,
                                       Byte unpacked,       // 0 미개봉 / 1 개봉
                                       String sellerMemo,
                                       List<MultipartFile> images) throws Exception {

    if (images == null || images.size() < 3) throw new IllegalArgumentException("이미지는 최소 3장이 필요합니다.");

    // 1) 사용자 조회(필수)
    Users user = usersRepo.findById(userId).orElseThrow(() -> new IllegalArgumentException("유저가 존재하지 않습니다."));

    // 2) ItemDetails 생성
    ItemDetails item = ItemDetails.builder()
        .cost(price)
        .storageDate(LocalDateTime.now())
        .deliveryDate(null)
        .status(false) // 아직 판매중 아님
        .user(user)
        .ean("UNKNOWN")
        .build();
    item = itemDetailsRepo.save(item); // PK 발급

    // 3) EXIF (대표컷 기준)
    LocalDateTime captured = null;
    captured = storage.readExifDateTime(images.get(0)); // 대표컷만 사용. 원하면 전체 중 최소 사용

    // 4) 이미지 저장
    List<String> urls = storage.saveItemImages(item.getItemId(), images);

    // 5) ItemImg 엔티티 저장 (0번 대표)
    for (int i = 0; i < urls.size(); i++) {
      ItemImg img = ItemImg.builder()
          .imgUrl(urls.get(i))
          .imgType((byte) (i == 0 ? 0 : 1))
          .filmingTime(captured) // 개별로 읽고 싶으면 루프 안에서 readExifDateTime 호출
          .itemDetails(item)
          .build();
      itemImgRepo.save(img);
    }

    // 6) Admin(검수 본체) 생성: 상태 PENDING(0)
    Admin ins = Admin.builder()
        .itemDetails(item)
        .unpacked(unpacked)              // 0/1
        .quality(null)                   // 등급은 미정
        .itemExplanation(sellerMemo)
        .registrationDate(LocalDateTime.now())
        .admissionState(0)               // 0 대기
        .rejectionReason(null)
        .build();
    ins = adminRepo.save(ins);

    return new CreateResult(ins.getInspectionId(), item.getItemId());
  }

  /** 상세 사진 */
  @Transactional(readOnly = true)
  public List<ItemImg> photosByInspectionId(Long inspectionId) {
    Admin ins = adminRepo.findById(inspectionId).orElseThrow();
    Long itemId = ins.getItemDetails().getItemId();
    return itemImgRepo.findByItemDetails_ItemIdOrderByImgTypeAscIdAsc(itemId);
  }

  /** 승인(2) + 등급/관리자 메모(선택) */
  @Transactional
  public void approve(Long inspectionId, Integer quality, String note) {
    Admin ins = adminRepo.findById(inspectionId).orElseThrow();
    ins.setAdmissionState(2);
    ins.setQuality(quality);
    if (note != null) {
      String base = ins.getItemExplanation() == null ? "" : ins.getItemExplanation() + "\n";
      ins.setItemExplanation(base + "[ADMIN] " + note);
    }
    adminRepo.save(ins);

    // 승인 시 판매 상태를 true로 전환하고 싶다면:
    ItemDetails item = ins.getItemDetails();
    item.setStatus(true);
    itemDetailsRepo.save(item);
  }

  /** 반려(1) + 사유코드/메모 */
  @Transactional
  public void reject(Long inspectionId, Integer reasonCode, String note) {
    Admin ins = adminRepo.findById(inspectionId).orElseThrow();
    ins.setAdmissionState(1);
    ins.setRejectionReason(reasonCode);
    if (note != null) {
      String base = ins.getItemExplanation() == null ? "" : ins.getItemExplanation() + "\n";
      ins.setItemExplanation(base + "[REJECT] " + note);
    }
    adminRepo.save(ins);
  }

  // --- DTO ---
  @lombok.Value
  public static class CreateResult {
    Long inspectionId;
    Long itemId;
  }
}
