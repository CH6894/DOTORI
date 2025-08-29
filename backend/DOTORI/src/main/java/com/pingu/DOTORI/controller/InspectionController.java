package com.pingu.DOTORI.controller;

import com.pingu.DOTORI.dto.InspectionRequest;
import com.pingu.DOTORI.service.InspectionService;
import com.pingu.DOTORI.service.InspectionService.CreateResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/inspections")
public class InspectionController {

    private final InspectionService inspectionService;

    /** 판매자 검수 신청 생성 */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CreateResult> create(@ModelAttribute InspectionRequest dto) throws Exception {
        CreateResult result = inspectionService.createInspection(dto);
        return ResponseEntity.ok(result);
    }
}
