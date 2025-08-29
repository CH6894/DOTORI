// src/main/java/com/pingu/DOTORI/service/CalendarService.java
package com.pingu.DOTORI.service;

import com.pingu.DOTORI.dto.CalendarRes;       // 이전에 만든 응답 DTO (FullCalendar 친화)
import com.pingu.DOTORI.dto.CalendarResponse;  // 이번 컨트롤러가 사용하는 DTO
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CalendarService {

    // 이미 구현된 비즈니스 서비스(메서드명: findInRange)
    private final CalendarService calendarsService;

    public List<CalendarResponse> getList(LocalDateTime start, LocalDateTime end) {
        List<CalendarResponse> resList = calendarsService.findInRange(start, end);
        return resList.stream()
                .map(r -> new CalendarResponse(
                        r.getId(),
                        r.getTitle(),
                        r.getStart(),
                        r.getEnd(),
                        r.isAllDay(),
                        r.getDescription(),
                        r.getColor()
                ))
                .toList();
    }
}
