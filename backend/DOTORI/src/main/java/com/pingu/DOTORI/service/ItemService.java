package com.pingu.DOTORI.service;

import lombok.RequiredArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.ArrayList;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.pingu.DOTORI.dto.ItemDTO;
import com.pingu.DOTORI.dto.ItemDetailsDTO;
import com.pingu.DOTORI.entity.Item;
import com.pingu.DOTORI.repository.ItemRepository;
import com.pingu.DOTORI.entity.ItemDetails;
import com.pingu.DOTORI.repository.ItemDetailsRepository;
import com.pingu.DOTORI.entity.ItemImg;
import com.pingu.DOTORI.repository.ItemImgRepository;

@Service
@RequiredArgsConstructor
public class ItemService {
	private final ItemRepository itemRepository;
	private final ItemDetailsRepository itemDetailsRepository;
	private final ItemImgRepository itemImgRepository;

	public Page<ItemDTO> findAll(Pageable pageable) {
		return itemRepository.findAll(pageable).map(this::toDto);
	}

	public ItemDTO findById(String itemCode) {
		Item item = itemRepository.findByItemCode(itemCode)
				.orElseThrow(() -> new RuntimeException("Item not found with itemCode: " + itemCode));
		return toDto(item);
	}

	public Page<ItemDTO> findByGenre(String genre, Pageable pageable) {
		return itemRepository.findByGenreIgnoreCase(genre, pageable).map(this::toDto);
	}

	public Page<ItemDTO> findByGenreAndTitle(String genre, String title, Pageable pageable) {
		return itemRepository.findByGenreIgnoreCaseAndTitleContainingIgnoreCase(genre, title, pageable)
				.map(this::toDto);
	}

	// 검색어로 아이템 찾기
	public Page<ItemDTO> searchItems(String query, Pageable pageable) {
		return itemRepository.findByNameContainingIgnoreCaseOrTitleContainingIgnoreCaseOrGenreContainingIgnoreCase(
				query, query, query, pageable).map(this::toDto);
	}

	// 승인된 상품의 ItemDetails 조회
	public List<ItemDetails> findApprovedItemDetailsByItemCode(String itemCode) {
		return itemDetailsRepository.findByItem_ItemCodeAndStatusOrderByUnpackedAsc(itemCode, true);
	}

	// 디버깅용: 모든 ItemDetails 조회 (승인 여부 상관없이)
	public List<ItemDetails> findAllItemDetailsByItemCode(String itemCode) {
		System.out.println("=== 디버깅: 모든 ItemDetails 조회 ===");
		System.out.println("조회할 itemCode: " + itemCode);

		// 1. Item이 존재하는지 확인
		try {
			Item item = itemRepository.findByItemCode(itemCode)
					.orElseThrow(() -> new RuntimeException("Item not found with itemCode: " + itemCode));
			System.out.println("Item 찾음: " + item.getName());
		} catch (Exception e) {
			System.out.println("Item 조회 실패: " + e.getMessage());
			return new ArrayList<>();
		}

		// 2. 모든 ItemDetails 조회 (조건 없이)
		List<ItemDetails> allDetails = itemDetailsRepository.findAll();
		System.out.println("전체 ItemDetails 개수: " + allDetails.size());

		// 3. 해당 itemCode와 관련된 ItemDetails만 필터링
		List<ItemDetails> filteredDetails = new ArrayList<>();
		for (ItemDetails detail : allDetails) {
			if (detail.getItem() != null && itemCode.equals(detail.getItem().getItemCode())) {
				filteredDetails.add(detail);
				System.out.println("관련 ItemDetails - ID: " + detail.getItemId() +
						", Status: " + detail.getStatus() +
						", Unpacked: " + detail.getUnpacked() +
						", Cost: " + detail.getCost() +
						", ItemCode: " + (detail.getItem() != null ? detail.getItem().getItemCode() : "null"));
			}
		}

		System.out.println("해당 itemCode와 관련된 ItemDetails 개수: " + filteredDetails.size());
		return filteredDetails;
	}

	// 승인된 미개봉 상품의 ItemDetails만 조회 (Admin 테이블의 admission_state = 1 확인)
	public List<ItemDetails> findApprovedUnpackedItemDetailsByItemCode(String itemCode) {
		System.out.println("ItemService - 미개봉 상품 조회 시작: itemCode=" + itemCode);

		// Repository의 쿼리 메서드 사용 (Item과 Admin 정보를 함께 조회)
		List<ItemDetails> result = itemDetailsRepository.findApprovedUnpackedItemDetailsWithAdminByItemCode(itemCode);
		System.out.println("Repository 쿼리 결과: " + result.size() + "개");

		// 쿼리에서 이미 admissionState = 1로 필터링했으므로 모든 결과가 승인된 상품
		for (ItemDetails detail : result) {
			System.out.println("승인된 미개봉 ItemDetails - ID: " + detail.getItemId() +
					", Cost: " + detail.getCost() +
					", Unpacked: " + detail.getUnpacked() +
					", ItemName: " + (detail.getItem() != null ? detail.getItem().getName() : "null") +
					", ItemCode: " + (detail.getItem() != null ? detail.getItem().getItemCode() : "null"));
		}

		System.out.println("승인된 미개봉 상품 최종 개수: " + result.size());
		return result;
	}

	// 승인된 미개봉 상품의 ItemDetails를 DTO로 조회 (JSON 직렬화 문제 해결)
	public List<ItemDetailsDTO> findApprovedUnpackedItemDetailsByItemCodeAsDTO(String itemCode) {
		System.out.println("ItemService - 미개봉 상품 DTO 조회 시작: itemCode=" + itemCode);

		// Repository의 쿼리 메서드 사용 (Item과 Admin 정보를 함께 조회)
		List<ItemDetails> result = itemDetailsRepository.findApprovedUnpackedItemDetailsWithAdminByItemCode(itemCode);
		System.out.println("Repository 쿼리 결과: " + result.size() + "개");

		// DTO로 변환하여 반환
		List<ItemDetailsDTO> dtoList = new ArrayList<>();
		for (ItemDetails detail : result) {
			System.out.println("승인된 미개봉 ItemDetails - ID: " + detail.getItemId() +
					", Cost: " + detail.getCost() +
					", Unpacked: " + detail.getUnpacked() +
					", ItemName: " + (detail.getItem() != null ? detail.getItem().getName() : "null") +
					", ItemCode: " + (detail.getItem() != null ? detail.getItem().getItemCode() : "null"));

			ItemDetailsDTO dto = toItemDetailsDto(detail);
			dtoList.add(dto);
		}

		System.out.println("승인된 미개봉 상품 DTO 최종 개수: " + dtoList.size());
		return dtoList;
	}

	// 승인된 개봉 상품의 ItemDetails와 Admin 정보를 함께 조회 (Admin.admission_state = 1 확인)
	public List<ItemDetailsDTO> findApprovedOpenedItemDetailsByItemCode(String itemCode) {
		List<ItemDetails> result = itemDetailsRepository.findApprovedOpenedItemDetailsWithAdminByItemCode(itemCode);
		System.out.println("ItemService - 승인된 개봉 상품 조회 결과: " + result.size() + "개");

		List<ItemDetailsDTO> dtoList = new ArrayList<>();
		for (ItemDetails detail : result) {
			System.out.println("ItemDetails ID: " + detail.getItemId());
			System.out.println("ItemDetails Cost: " + detail.getCost());
			System.out.println("ItemDetails ProductCondition: " + detail.getProductCondition());
			System.out.println("Admin 개수: " + (detail.getAdmins() != null ? detail.getAdmins().size() : 0));

			if (detail.getAdmins() != null && !detail.getAdmins().isEmpty()) {
				System.out.println("첫 번째 Admin Quality: " + detail.getAdmins().get(0).getQuality());
				System.out.println("첫 번째 Admin RegistrationDate: " + detail.getAdmins().get(0).getRegistrationDate());
				System.out.println("첫 번째 Admin ItemExplanation: " + detail.getAdmins().get(0).getItemExplanation());
				System.out.println("첫 번째 Admin AdmissionState: " + detail.getAdmins().get(0).getAdmissionState());
			}

			ItemDetailsDTO dto = toItemDetailsDto(detail);
			dtoList.add(dto);
		}
		return dtoList;
	}

	// ItemDetails의 모든 이미지 조회 (관리자 이미지 우선, 판매자 이미지 후순위)
	public List<String> getAllImagesByItemDetailsId(Long itemDetailsId) {
		System.out.println("ItemService - 모든 이미지 조회 시작: itemDetailsId=" + itemDetailsId);
		
		List<String> imageUrls = new ArrayList<>();
		
		// 1. 관리자 이미지 먼저 조회 (img_type = 2)
		List<ItemImg> adminImages = itemImgRepository.findByItemDetails_ItemIdAndImgTypeOrderByIdAsc(itemDetailsId, (byte) 2);
		System.out.println("관리자 이미지 개수: " + adminImages.size());
		
		for (ItemImg img : adminImages) {
			String imageUrl = img.getImgUrl();
			if (imageUrl.startsWith("http")) {
				imageUrls.add(imageUrl);
			} else {
				imageUrls.add("http://localhost:8081/uploads/admin/" + imageUrl);
			}
			System.out.println("관리자 이미지 URL 추가: " + imageUrl);
		}
		
		// 2. 판매자 이미지 조회 (img_type = 0)
		List<ItemImg> sellerImages = itemImgRepository.findByItemDetails_ItemIdAndImgTypeOrderByIdAsc(itemDetailsId, (byte) 0);
		System.out.println("판매자 이미지 개수: " + sellerImages.size());
		
		for (ItemImg img : sellerImages) {
			String imageUrl = img.getImgUrl();
			if (imageUrl.startsWith("http")) {
				imageUrls.add(imageUrl);
			} else {
				imageUrls.add("http://localhost:8081/uploads/items/" + imageUrl);
			}
			System.out.println("판매자 이미지 URL 추가: " + imageUrl);
		}
		
		System.out.println("최종 이미지 URL 개수: " + imageUrls.size() + " (관리자: " + adminImages.size() + ", 판매자: " + sellerImages.size() + ")");
		return imageUrls;
	}

	// 이미지 정보를 포함한 DTO 클래스
	@Getter
	@Setter
	@AllArgsConstructor
	@NoArgsConstructor
	public static class ImageInfo {
		private String url;
		private String type; // "admin" 또는 "seller"
		private String typeLabel; // "관리자" 또는 "판매자"
	}

	// ItemDetails의 모든 이미지 정보 조회 (관리자 이미지 우선, 판매자 이미지 후순위)
	public List<ImageInfo> getAllImageInfosByItemDetailsId(Long itemDetailsId) {
		System.out.println("ItemService - 모든 이미지 정보 조회 시작: itemDetailsId=" + itemDetailsId);
		
		List<ImageInfo> imageInfos = new ArrayList<>();
		
		// 1. 관리자 이미지 먼저 조회 (img_type = 2)
		List<ItemImg> adminImages = itemImgRepository.findByItemDetails_ItemIdAndImgTypeOrderByIdAsc(itemDetailsId, (byte) 2);
		System.out.println("관리자 이미지 개수: " + adminImages.size());
		
		for (ItemImg img : adminImages) {
			String imageUrl = img.getImgUrl();
			if (!imageUrl.startsWith("http")) {
				imageUrl = "http://localhost:8081/uploads/admin/" + imageUrl;
			}
			imageInfos.add(new ImageInfo(imageUrl, "admin", "관리자"));
			System.out.println("관리자 이미지 정보 추가: " + imageUrl);
		}
		
		// 2. 판매자 이미지 조회 (img_type = 0)
		List<ItemImg> sellerImages = itemImgRepository.findByItemDetails_ItemIdAndImgTypeOrderByIdAsc(itemDetailsId, (byte) 0);
		System.out.println("판매자 이미지 개수: " + sellerImages.size());
		
		for (ItemImg img : sellerImages) {
			String imageUrl = img.getImgUrl();
			if (!imageUrl.startsWith("http")) {
				imageUrl = "http://localhost:8081/uploads/items/" + imageUrl;
			}
			imageInfos.add(new ImageInfo(imageUrl, "seller", "판매자"));
			System.out.println("판매자 이미지 정보 추가: " + imageUrl);
		}
		
		System.out.println("최종 이미지 정보 개수: " + imageInfos.size() + " (관리자: " + adminImages.size() + ", 판매자: " + sellerImages.size() + ")");
		return imageInfos;
	}

	// 관리자 이미지만 조회
	public List<String> getAdminImagesByItemDetailsId(Long itemDetailsId) {
		System.out.println("ItemService - 관리자 이미지 조회 시작: itemDetailsId=" + itemDetailsId);
		
		List<ItemImg> adminImages = itemImgRepository.findByItemDetails_ItemIdAndImgTypeOrderByIdAsc(itemDetailsId, (byte) 2);
		System.out.println("조회된 관리자 이미지 개수: " + adminImages.size());
		
		List<String> imageUrls = new ArrayList<>();
		for (ItemImg img : adminImages) {
			String imageUrl = img.getImgUrl();
			// NCP URL이면 그대로, 로컬 파일이면 경로 추가
			if (imageUrl.startsWith("http")) {
				imageUrls.add(imageUrl);
			} else {
				imageUrls.add("http://localhost:8081/uploads/admin/" + imageUrl);
			}
			System.out.println("관리자 이미지 URL 추가: " + imageUrl);
		}
		
		System.out.println("최종 관리자 이미지 URL 개수: " + imageUrls.size());
		return imageUrls;
	}

	// 판매자 이미지만 조회
	public List<String> getSellerImagesByItemDetailsId(Long itemDetailsId) {
		System.out.println("ItemService - 판매자 이미지 조회 시작: itemDetailsId=" + itemDetailsId);
		
		List<ItemImg> sellerImages = itemImgRepository.findByItemDetails_ItemIdAndImgTypeOrderByIdAsc(itemDetailsId, (byte) 0);
		System.out.println("조회된 판매자 이미지 개수: " + sellerImages.size());
		
		List<String> imageUrls = new ArrayList<>();
		for (ItemImg img : sellerImages) {
			String imageUrl = img.getImgUrl();
			// NCP URL이면 그대로, 로컬 파일이면 경로 추가
			if (imageUrl.startsWith("http")) {
				imageUrls.add(imageUrl);
			} else {
				imageUrls.add("http://localhost:8081/uploads/items/" + imageUrl);
			}
			System.out.println("판매자 이미지 URL 추가: " + imageUrl);
		}
		
		System.out.println("최종 판매자 이미지 URL 개수: " + imageUrls.size());
		return imageUrls;
	}

	private ItemDTO toDto(Item i) {
		return ItemDTO.builder()
				.itemCode(i.getItemCode())
				.name(i.getName())
				.title(i.getTitle())
				.manufacturer(i.getManufacturer())
				.material(i.getMaterial())
				.releaseMonth(i.getReleaseMonth())
				.size(i.getSize())
				.information(i.getInformation())
				.imgUrl(i.getImgUrl())
				.storageFees(i.getStorageFees())
				.genre(i.getGenre())
				.cost(i.getCost())
				.build();
	}

	private ItemDetailsDTO toItemDetailsDto(ItemDetails detail) {
		// Admin 정보에서 quality, registrationDate, itemExplanation 가져오기
		Integer quality = null;
		LocalDateTime registrationDate = null;
		String itemExplanation = null;

		if (detail.getAdmins() != null && !detail.getAdmins().isEmpty()) {
			quality = detail.getAdmins().get(0).getQuality();
			registrationDate = detail.getAdmins().get(0).getRegistrationDate();
			itemExplanation = detail.getAdmins().get(0).getItemExplanation();
		}

		return ItemDetailsDTO.builder()
				.itemId(detail.getItemId())
				.cost(detail.getCost())
				.status(detail.getStatus())
				.unpacked(detail.getUnpacked())
				.productCondition(detail.getProductCondition())
				.quality(quality) // Admin 테이블의 quality 컬럼 사용
				.registrationDate(registrationDate) // Admin 테이블의 registration_date 컬럼 사용
				.itemExplanation(itemExplanation) // Admin 테이블의 item_explanation 컬럼 사용
				.itemCode(detail.getItem() != null ? detail.getItem().getItemCode() : null)
				.itemName(detail.getItem() != null ? detail.getItem().getName() : null)
				.itemImgUrl(detail.getItem() != null ? detail.getItem().getImgUrl() : null)
				.build();
	}

}
