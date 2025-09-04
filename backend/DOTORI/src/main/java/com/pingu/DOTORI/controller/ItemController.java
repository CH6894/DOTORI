package com.pingu.DOTORI.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pingu.DOTORI.dto.ItemDTO;
import com.pingu.DOTORI.dto.ItemDetailsDTO;
import com.pingu.DOTORI.service.ItemService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/open/items")
@RequiredArgsConstructor
public class ItemController {
   private final ItemService itemService;

   @GetMapping
   public ResponseEntity<Page<ItemDTO>> findAll(@PageableDefault(size = 18) Pageable pageable) {
      return ResponseEntity.ok(itemService.findAll(pageable));
   }

   @GetMapping("/{id}")
   public ResponseEntity<ItemDTO> findById(@PathVariable String id) {
      return ResponseEntity.ok(itemService.findById(id));
   }

   @GetMapping("/genre/{genre}")
   public ResponseEntity<Page<ItemDTO>> byGenre(@PathVariable String genre,
         @PageableDefault(size = 300) Pageable pageable) {
      return ResponseEntity.ok(itemService.findByGenre(genre, pageable));
   }

   @GetMapping("/genre/{genre}/title/{title}")
   public ResponseEntity<Page<ItemDTO>> byGenreAndTitle(@PathVariable String genre,
         @PathVariable(required = false) String title, @PageableDefault(size = 300) Pageable pageable) {
      return ResponseEntity.ok(itemService.findByGenreAndTitle(genre, title, pageable));
   }

   @GetMapping("/{itemCode}/approved-unpacked-details")
   public ResponseEntity<List<ItemDetailsDTO>> getApprovedUnpackedItemDetails(@PathVariable String itemCode) {
      List<ItemDetailsDTO> details = itemService.findApprovedUnpackedItemDetailsByItemCodeAsDTO(itemCode);
      return ResponseEntity.ok(details);
   }

   @GetMapping("/{itemCode}/approved-opened-details")
   public ResponseEntity<List<ItemDetailsDTO>> getApprovedOpenedItemDetails(@PathVariable String itemCode) {
      List<ItemDetailsDTO> details = itemService.findApprovedOpenedItemDetailsByItemCode(itemCode);
      return ResponseEntity.ok(details);
   }

   // 검색 API
   @GetMapping("/search")
   public ResponseEntity<Page<ItemDTO>> searchItems(@RequestParam String q,
         @PageableDefault(size = 30) Pageable pageable) {
      return ResponseEntity.ok(itemService.searchItems(q, pageable));
   }

   // ItemDetails의 모든 이미지 조회 (관리자 이미지 포함)
   @GetMapping("/item-details/{itemDetailsId}/images")
   public ResponseEntity<List<String>> getAllImagesByItemDetailsId(@PathVariable Long itemDetailsId) {
      List<String> images = itemService.getAllImagesByItemDetailsId(itemDetailsId);
      return ResponseEntity.ok(images);
   }

   // ItemDetails의 관리자 이미지만 조회
   @GetMapping("/item-details/{itemDetailsId}/admin-images")
   public ResponseEntity<List<String>> getAdminImagesByItemDetailsId(@PathVariable Long itemDetailsId) {
      List<String> images = itemService.getAdminImagesByItemDetailsId(itemDetailsId);
      return ResponseEntity.ok(images);
   }

   // ItemDetails의 판매자 이미지만 조회
   @GetMapping("/item-details/{itemDetailsId}/seller-images")
   public ResponseEntity<List<String>> getSellerImagesByItemDetailsId(@PathVariable Long itemDetailsId) {
      List<String> images = itemService.getSellerImagesByItemDetailsId(itemDetailsId);
      return ResponseEntity.ok(images);
   }

   // ItemDetails의 모든 이미지 정보 조회 (관리자 이미지 우선, 판매자 이미지 후순위)
   @GetMapping("/item-details/{itemDetailsId}/image-infos")
   public ResponseEntity<List<com.pingu.DOTORI.service.ItemService.ImageInfo>> getAllImageInfosByItemDetailsId(@PathVariable Long itemDetailsId) {
      List<com.pingu.DOTORI.service.ItemService.ImageInfo> imageInfos = itemService.getAllImageInfosByItemDetailsId(itemDetailsId);
      return ResponseEntity.ok(imageInfos);
   }

   @GetMapping("/item-details/{itemDetailsId}/details-with-admin")
   public ResponseEntity<com.pingu.DOTORI.service.ItemService.ItemDetailsWithAdminInfo> getItemDetailsWithAdminInfo(@PathVariable Long itemDetailsId) {
      com.pingu.DOTORI.service.ItemService.ItemDetailsWithAdminInfo info = itemService.getItemDetailsWithAdminInfo(itemDetailsId);
      return ResponseEntity.ok(info);
   }

}
