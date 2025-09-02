package com.pingu.DOTORI.dto;

import java.time.LocalDateTime;

public class CalendarRequest {

    private String scheduleName;        // 필수
    private LocalDateTime scheduleDate; // 필수
    private String scheduleInfo;        // 선택
    private Long userId;                // 필수(외래키)

    public String getScheduleName() { return scheduleName; }
    public LocalDateTime getScheduleDate() { return scheduleDate; }
    public String getScheduleInfo() { return scheduleInfo; }
    public Long getUserId() { return userId; }

    public void setScheduleName(String scheduleName) { this.scheduleName = scheduleName; }
    public void setScheduleDate(LocalDateTime scheduleDate) { this.scheduleDate = scheduleDate; }
    public void setScheduleInfo(String scheduleInfo) { this.scheduleInfo = scheduleInfo; }
    public void setUserId(Long userId) { this.userId = userId; }
}
