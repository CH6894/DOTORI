package com.pingu.DOTORI.dto;

import lombok.Data;

@Data
public class InspectionUpdateRequest {
    private Long inspectionId;  // 검수 신청 ID
    private String status;      // 상태 ("PENDING", "APPROVED", "REJECTED")
    private String grade;       // 등급 ("S", "A", "B", "C")

    
}
