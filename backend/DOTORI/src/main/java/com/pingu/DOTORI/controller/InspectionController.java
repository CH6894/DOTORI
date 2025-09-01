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
        System.out.println("FilmingTime: " + dto.getFilmingTime());
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
}
