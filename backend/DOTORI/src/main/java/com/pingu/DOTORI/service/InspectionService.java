package com.pingu.DOTORI.service;

import com.pingu.DOTORI.dto.InspectionRequest;
import com.pingu.DOTORI.entity.*;
import com.pingu.DOTORI.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InspectionService {

    private final ItemDetailsRepository itemDetailsRepo;
    private final ItemImgRepository itemImgRepo;
    private final AdminRepository adminRepo;
    private final UsersRepository usersRepo;
    private final FileStorageService storage;
    private final ItemRepository itemRepo;

    /** 판매자 검수 신청 생성 */
    @Transactional
    public CreateResult createInspection(InspectionRequest dto) throws Exception {

        if (dto.getImages() == null || dto.getImages().size() < 3) {
            throw new IllegalArgumentException("이미지는 최소 3장이 필요합니다.");
        }

        // 1) 사용자 조회
        Users user = usersRepo.findById(dto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("❌ 유저가 존재하지 않습니다."));

        // 2) 상품 기본 정보 조회
        Item baseItem = itemRepo.findById(dto.getItemCode())
                .orElseThrow(() -> new IllegalArgumentException("❌ Item not found: " + dto.getItemCode()));

        // 3) ItemDetails 생성
        ItemDetails item = ItemDetails.builder()
                .cost(dto.getPrice())
                .storageDate(LocalDateTime.now())
                .deliveryDate(null)
                .status(false) // 아직 판매중 아님
                .user(user)
                .ean("UNKNOWN")
                .item(baseItem)
                .build();
        item = itemDetailsRepo.save(item);

        // 4) 이미지 저장
        LocalDateTime captured = storage.readExifDateTime(dto.getImages().get(0));
        List<String> urls = storage.saveItemImages(item.getItemId(), dto.getImages());

        for (int i = 0; i < urls.size(); i++) {
            ItemImg img = ItemImg.builder()
                    .imgUrl(urls.get(i))
                    .imgType((byte) (i == 0 ? 0 : 1)) // 첫 번째 이미지는 대표
                    .filmingTime(captured)
                    .itemDetails(item)
                    .build();
            itemImgRepo.save(img);
        }

        // 5) Admin(검수 신청) 생성
        Admin ins = Admin.builder()
                .itemDetails(item)
                .unpacked((byte) dto.getUnpacked())
                .quality(null)
                .itemExplanation(dto.getMemo())
                .registrationDate(LocalDateTime.now())
                .admissionState(0) // 대기
                .rejectionReason(null)
                .build();
        ins = adminRepo.save(ins);

        return new CreateResult(ins.getInspectionId(), item.getItemId());
    }

    /** 승인 */
    @Transactional
    public void approve(Long inspectionId, Integer quality, String note) {
        Admin ins = adminRepo.findById(inspectionId).orElseThrow();
        ins.setAdmissionState(2); // 승인
        ins.setQuality(quality);
        if (note != null) {
            ins.setItemExplanation((ins.getItemExplanation() == null ? "" : ins.getItemExplanation() + "\n")
                    + "[ADMIN] " + note);
        }
        adminRepo.save(ins);

        ItemDetails item = ins.getItemDetails();
        item.setStatus(true);
        itemDetailsRepo.save(item);
    }

    /** 반려 */
    @Transactional
    public void reject(Long inspectionId, Integer reasonCode, String note) {
        Admin ins = adminRepo.findById(inspectionId).orElseThrow();
        ins.setAdmissionState(1); // 반려
        ins.setRejectionReason(reasonCode);
        if (note != null) {
            ins.setItemExplanation((ins.getItemExplanation() == null ? "" : ins.getItemExplanation() + "\n")
                    + "[REJECT] " + note);
        }
        adminRepo.save(ins);
    }

    // 결과 DTO
    @lombok.Value
    public static class CreateResult {
        Long inspectionId;
        Long itemId;
    }
}
