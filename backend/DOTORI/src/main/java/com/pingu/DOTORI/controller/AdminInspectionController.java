//package com.pingu.DOTORI.controller;
//
//import com.pingu.DOTORI.dto.AdminListRow;
//import com.pingu.DOTORI.entity.ItemImg;
//import com.pingu.DOTORI.repository.AdminRepository;
//import com.pingu.DOTORI.service.InspectionService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.data.domain.*;
//import org.springframework.format.annotation.DateTimeFormat;
//import org.springframework.web.bind.annotation.*;
//
//import java.time.Instant;
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/inspections/admin")
//@RequiredArgsConstructor
//public class AdminInspectionController {
//
//  private final AdminRepository adminRepo;
//  private final InspectionService service;
//
//  /** 목록 (projection) */
//  @GetMapping
//  public Page<AdminListRow> list(
//      @RequestParam(required=false) Integer state,
//      @RequestParam(required=false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
//      @RequestParam(required=false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
//      @RequestParam(defaultValue = "0") int page,
//      @RequestParam(defaultValue = "20") int size
//  ) {
//    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "registrationDate"));
//    return adminRepo.findAdminList(state, from, to, pageable);
//  }
//
//  /** 상세 이미지 */
//  @GetMapping("/{inspectionId}/photos")
//  public List<ItemImg> photos(@PathVariable Long inspectionId) {
//    return service.photosByInspectionId(inspectionId);
//  }
//
//  /** 승인 */
//  @PostMapping("/{inspectionId}/approve")
//  public void approve(@PathVariable Long inspectionId,
//                      @RequestParam(required=false) Integer quality,
//                      @RequestParam(required=false, defaultValue = "") String note) {
//    service.approve(inspectionId, quality, note);
//  }
//
//  /** 반려 */
//  @PostMapping("/{inspectionId}/reject")
//  public void reject(@PathVariable Long inspectionId,
//                     @RequestParam Integer reason,       // 1~5
//                     @RequestParam(required=false, defaultValue = "") String note) {
//    service.reject(inspectionId, reason, note);
//  }
//}
