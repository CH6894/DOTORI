package com.pingu.DOTORI.entity;


import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@Entity
@Table(name = "Admin") // 실제 테이블명 정확히
public class Admin {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "Inspection_ID") // 실제 컬럼명
  private Long inspectionId;       // ✅ 서비스에서 getInspectionId() 호출

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "Item_ID", nullable = false)
  private ItemDetails itemDetails; // ✅ 서비스에서 getItemDetails()

  @Column(name = "Unpacked")         // 0/1
  private Byte unpacked;

  @Column(name = "Quality")          // 등급(1/2/3 등)
  private Integer quality;

  @Lob
  @Column(name = "Item_Explanation")
  private String itemExplanation;

  @Column(name = "Registration_Date")
  private LocalDateTime registrationDate;

  @Column(name = "Admission_State")  // 0:대기, 1:반려, 2:승인
  private Integer admissionState;

  @Column(name = "Rejection_Reason") // 반려 사유 코드(1~5)
  private Integer rejectionReason;


}
