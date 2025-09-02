
// Controller (이름 유지)
package com.pingu.DOTORI.controller;

import com.pingu.DOTORI.dto.CalendarResponse;
import com.pingu.DOTORI.service.CalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/calendarmanager")
public class CalendarPublicController {

    private final CalendarService service;

    @GetMapping
    public List<CalendarResponse> list(
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam("end")   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end
    ) {
        return service.findInRange(start, end);
    }
}

