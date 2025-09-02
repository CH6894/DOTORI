package com.pingu.DOTORI.controller;

import com.pingu.DOTORI.dto.AdminListRow;
import com.pingu.DOTORI.dto.InspectionRequest;
import com.pingu.DOTORI.dto.InspectionUpdateRequest;
import com.pingu.DOTORI.service.InspectionService;
import com.pingu.DOTORI.service.InspectionService.CreateResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/inspections")
public class InspectionController {

    private final InspectionService inspectionService;

    /** 판매자 검수 신청 생성 */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CreateResult> create(@ModelAttribute InspectionRequest dto) throws Exception {
        System.out.println("=== 받은 데이터 디버깅 ===");
        System.out.println("itemCode: " + dto.getItemCode());
        System.out.println("productTitle: " + dto.getProductTitle());
        System.out.println("userId: " + dto.getUserId());
        System.out.println("price: " + dto.getPrice());
        System.out.println("unpacked: " + dto.getUnpacked());
        System.out.println("memo: " + dto.getMemo());
        System.out.println("FilmingTime: " + dto.getFilmingTime());
        System.out.println("images count: " + (dto.getImages() != null ? dto.getImages().size() : 0));
        System.out.println("========================");
        
        CreateResult result = inspectionService.createInspection(dto);
        return ResponseEntity.ok(result);
    }

    /** 상태 및 등급 업데이트 */
    @PostMapping("/update")
    public ResponseEntity<Void> updateInspectionStatus(@RequestBody InspectionUpdateRequest request) {
        inspectionService.updateStatusAndGrade(
                request.getInspectionId(),
                request.getStatus(),
                request.getGrade()
        );
        return ResponseEntity.ok().build();
    }

    /** 관리자용 검수 목록 조회 */
    @GetMapping
    public ResponseEntity<Page<AdminListRow>> getAdminInspections(
            @RequestParam(required = false) Integer state,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        Page<AdminListRow> adminList =
                inspectionService.getAdminInspections(state, from, to, page, size);

        return ResponseEntity.ok(adminList);
    }

    /** 관리자 승인 */
    @PostMapping("/{inspectionId}/approve")
    public ResponseEntity<Void> approveInspection(
            @PathVariable Long inspectionId,
            @RequestParam(required = false) Integer grade,
            @RequestParam(required = false) String reason) {
        
        inspectionService.approve(inspectionId, grade, reason);
        return ResponseEntity.ok().build();
    }

    /** 관리자 반려 */
    @PostMapping("/{inspectionId}/reject")
    public ResponseEntity<Void> rejectInspection(
            @PathVariable Long inspectionId,
            @RequestParam(required = false) Integer grade,
            @RequestParam(required = false) String reason) {
        
        inspectionService.reject(inspectionId, grade, reason);
        return ResponseEntity.ok().build();
    }
}
