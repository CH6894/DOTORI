package com.pingu.DOTORI.controller;

import com.pingu.DOTORI.service.InspectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/inspections")
@RequiredArgsConstructor
public class InspectionController {

	private final InspectionService inspectionService;

  /** 프론트 STEP 최종 제출: multipart/form-data */
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public CreateRes create(
      @RequestParam("userId") Long userId,        // 🔸 임시: 로그인 연동 후 Security에서 가져오기
      @RequestParam ("itemCode") String itemCode,
      @RequestParam("productTitle") String productTitle,
      @RequestParam("price") BigDecimal price,
      @RequestParam("unpacked") Byte unpacked,    // 0:미개봉, 1:개봉
      @RequestParam(value = "memo", required=false, defaultValue="") String memo,
      @RequestParam("images") List<MultipartFile> images
  ) throws Exception {
	  InspectionService.CreateResult result = inspectionService.createInspection(
	            userId, itemCode, productTitle, price, unpacked, memo, images);

	    return new CreateRes(result.getInspectionId(), result.getItemId());
  }

  public static class CreateRes {
      private Long inspectionId;
      private Long itemId;

      public CreateRes(Long inspectionId, Long itemId) {
          this.inspectionId = inspectionId;
          this.itemId = itemId;
      }
  }
}
