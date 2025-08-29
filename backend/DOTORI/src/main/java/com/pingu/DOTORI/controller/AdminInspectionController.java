package com.pingu.DOTORI.controller;

import com.pingu.DOTORI.dto.AdminListRow;
import com.pingu.DOTORI.entity.Admin;
import com.pingu.DOTORI.repository.AdminRepository;
import com.pingu.DOTORI.service.InspectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/inspections")
public class AdminInspectionController {

    private final AdminRepository adminRepo;
    private final InspectionService inspectionService;

    /** 관리자 페이지에서 검수 신청 목록 조회 */
    @GetMapping
    public Page<AdminListRow> list(
            @RequestParam(required = false) Integer state,      // 상태(PENDING, APPROVED, REJECTED)
            @RequestParam(required = false) LocalDateTime from, // 시작일
            @RequestParam(required = false) LocalDateTime to,   // 종료일
            Pageable pageable
    ) {
        return adminRepo.findAdminList(state, from, to, pageable);
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
