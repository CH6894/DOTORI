package com.pingu.DOTORI.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface AdminListRow {
    Long getInspectionId();
    Long getItemId();
    LocalDateTime getRegistrationDate();

    Integer getUnpacked();
    Integer getAdmissionState();
    Integer getQuality();

    Long getImageCount();
    LocalDateTime getFirstFilmingTime();

    BigDecimal getCost();
    String getItemExplanation(); // 판매자 코멘트
}
