package com.pingu.DOTORI.dto;

import com.pingu.DOTORI.entity.Calendars;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.Duration;

@Getter
@Builder
@AllArgsConstructor
public class CalendarResponse {
	public Long id;
    public String title;        // scheduleName
    public String start;        // ISO
    public String end;          // ISO or null (메타에서 복원)
    public boolean allDay;      // 메타에서 복원(없으면 false)
    public String description;  // scheduleInfo(사람 읽는 본문)
    public String color;        // 메타에서 복원(없으면 null)
}
