
// Controller (이름 유지)
package com.pingu.DOTORI.controller;

import com.pingu.DOTORI.dto.CalendarResponse;
import com.pingu.DOTORI.entity.Calendars;
import com.pingu.DOTORI.service.CalendarService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/public/calendars")
public class CalendarPublicController {

    private final CalendarService service;

    public CalendarPublicController(CalendarService service) {
        this.service = service;
    }

    @GetMapping
    public List<CalendarResponse> list(

            @RequestParam("start") @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime start,
            @RequestParam("end") @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime end

    ) {
        List<Calendars> found = service.findInRange(start, end);
        return found.stream().map(CalendarResponse::from).toList();
    }
}
