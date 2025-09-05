package com.pingu.DOTORI.controller;

import com.pingu.DOTORI.dto.ItemDetailsDTO;
import com.pingu.DOTORI.entity.ItemDetails;
import com.pingu.DOTORI.repository.ItemDetailsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/itemdetails")
@RequiredArgsConstructor
public class ItemDetailsController {

    private final ItemDetailsRepository itemDetailsRepository;

    /** ✅ itemDetailsId로 단일 상품 조회 */
    @GetMapping("/{id}")
    public ResponseEntity<ItemDetailsDTO> getItemDetails(@PathVariable Long id) {
        return itemDetailsRepository.findById(id)
                .map(detail -> {
                    ItemDetailsDTO dto = ItemDetailsDTO.builder()
                            .itemId(detail.getItemId())
                            .cost(detail.getCost())
                            .status(detail.getStatus())
                            .unpacked(detail.getUnpacked())
                            .productCondition(detail.getProductCondition())
                            .quality(null) 
                            .registrationDate(detail.getStorageDate())
                            .itemExplanation(null) 
                            .itemCode(detail.getItem().getItemCode())
                            .itemName(detail.getItem().getName())
                            .itemImgUrl(detail.getItem().getImgUrl())
                            .build();
                    return ResponseEntity.ok(dto);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
