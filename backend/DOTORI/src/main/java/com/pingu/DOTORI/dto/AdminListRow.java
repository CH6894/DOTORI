package com.pingu.DOTORI.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AdminListRow {
    private Long inspectionId;
    private Long itemId;
    private LocalDateTime registrationDate;
    private Boolean unpacked;
    private Integer admissionState;
    private Integer quality;
    private Long imageCount;
    private LocalDateTime filmingTime;
    private BigDecimal cost;

    // 생성자
    public AdminListRow(Long inspectionId, Long itemId, LocalDateTime registrationDate, Boolean unpacked, 
                         Integer admissionState, Integer quality, Long imageCount, LocalDateTime filmingTime, BigDecimal cost) {
        this.inspectionId = inspectionId;
        this.itemId = itemId;
        this.registrationDate = registrationDate;
        this.unpacked = unpacked;
        this.admissionState = admissionState;
        this.quality = quality;
        this.imageCount = imageCount;
        this.filmingTime = filmingTime;
        this.cost = cost;
    }

    // Getter and Setter methods
    public Long getInspectionId() {
        return inspectionId;
    }

    public void setInspectionId(Long inspectionId) {
        this.inspectionId = inspectionId;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }

    public Boolean getUnpacked() {
        return unpacked;
    }

    public void setUnpacked(Boolean unpacked) {
        this.unpacked = unpacked;
    }

    public Integer getAdmissionState() {
        return admissionState;
    }

    public void setAdmissionState(Integer admissionState) {
        this.admissionState = admissionState;
    }

    public Integer getQuality() {
        return quality;
    }

    public void setQuality(Integer quality) {
        this.quality = quality;
    }

    public Long getImageCount() {
        return imageCount;
    }

    public void setImageCount(Long imageCount) {
        this.imageCount = imageCount;
    }

    public LocalDateTime getFilmingTime() {
        return filmingTime;
    }

    public void setFilmingTime(LocalDateTime filmingTime) {
        this.filmingTime = filmingTime;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }
}

