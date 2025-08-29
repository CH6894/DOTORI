package com.pingu.DOTORI.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CalendarRequest {
	 private String scheduleName;          // 제목
	    private LocalDateTime scheduleDate;   // 시작
	    private LocalDateTime endDate;        // 종료(선택) — 엔티티엔 없지만 메타로 저장
	    private boolean allDay;               // 종일 — 메타
	    private String color;                 // 색상 — 메타
	    private String scheduleInfo;          // 상세 본문(사람이 읽는 설명)
	    private Long userId;                  // 작성자
	}
