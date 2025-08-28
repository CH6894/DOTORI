package com.pingu.DOTORI.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

/** 관리자 목록 1행에 필요한 필드만 가져오는 Projection */
// 데이터를 담아서 보내는 용도 -> 계층 간 데이터를 효율적으로 전송
public interface AdminListRow {
  Long getInspectionId();
  Long getItemId();
  LocalDateTime getRegistrationDate();

  Integer getUnpacked();       // 0/1
  Integer getAdmissionState(); // 0/1/2
  Integer getQuality();        // 1/2/3

  Long getImageCount();        // 이미지 개수
  LocalDateTime getFirstFilmingTime(); // 가장 빠른 촬영 시각(있으면)

  BigDecimal getCost();        // Item_details.cost
  // 필요하면 이름/판매자 등도 추가 가능(스키마에 있다면)
}
