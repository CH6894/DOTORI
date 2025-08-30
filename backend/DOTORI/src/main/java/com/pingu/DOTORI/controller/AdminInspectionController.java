package com.pingu.DOTORI.controller;

import com.pingu.DOTORI.dto.AdminListRow;
import com.pingu.DOTORI.service.InspectionService;
import com.pingu.DOTORI.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/inspections")  // 엔드포인트 명시적으로 /inspections
public class AdminInspectionController {

    private final AdminRepository adminRepo;
    private final InspectionService inspectionService;

    /**
     * 필터링된 판매 신청 내역 조회
     */
    @GetMapping
    public ResponseEntity<Page<AdminListRow>> getFilteredAdminList(
            @RequestParam(required = false) Integer state,
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "registrationDate"));
        Page<AdminListRow> result = adminRepo.findByFilters(state, from, to, pageable);
        return ResponseEntity.ok(result);
    }

    /** 승인 */
    @PostMapping("/{id}/approve")
    public ResponseEntity<Void> approve(
            @PathVariable("id") Long inspectionId,
            @RequestParam(required = false) Integer quality,
            @RequestParam(required = false) String note
    ) {
        inspectionService.approve(inspectionId, quality, note);
        return ResponseEntity.ok().build();
    }

    /** 반려 */
    @PostMapping("/{id}/reject")
    public ResponseEntity<Void> reject(
            @PathVariable("id") Long inspectionId,
            @RequestParam(required = false) Integer reasonCode,
            @RequestParam(required = false) String note
    ) {
        inspectionService.reject(inspectionId, reasonCode, note);
        return ResponseEntity.ok().build();
    }
}
