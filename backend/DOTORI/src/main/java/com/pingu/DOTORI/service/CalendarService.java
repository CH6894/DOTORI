// src/main/java/com/pingu/DOTORI/service/CalendarService.java
package com.pingu.DOTORI.service;

import com.pingu.DOTORI.dto.CalendarRequest;
import com.pingu.DOTORI.dto.CalendarResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CalendarService {

    // 비즈니스 로직을 가진 실제 서비스(이미 구현되어 있어야 함)
    private final CalendarService calendarService;

    /** Public 조회 컨트롤러에서 호출 */
    public List<CalendarResponse> findInRange(LocalDateTime start, LocalDateTime end) {
        return calendarService.findInRange(start, end);
    }

    /** Admin 컨트롤러에서 호출 */
    public CalendarResponse create(CalendarRequest req) {
        return calendarService.create(req);
    }

    public CalendarResponse update(Long id, CalendarRequest req) {
        return calendarService.update(id, req);
    }

    public void delete(Long id) {
        calendarService.delete(id);
    }
}
