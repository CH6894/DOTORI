package com.pingu.DOTORI.controller;

import com.pingu.DOTORI.dto.CalendarRequest;
import com.pingu.DOTORI.dto.CalendarResponse;
import com.pingu.DOTORI.entity.Calendars;
import com.pingu.DOTORI.service.CalendarService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/calendars") // ← 프론트에서 이 경로로 호출
public class CalendarAdminController {

    private final CalendarService service;

    public CalendarAdminController(CalendarService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<CalendarResponse> create(@RequestBody CalendarRequest req) {
        Calendars saved = service.create(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(CalendarResponse.from(saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/ping")
    public String ping() { return "ok"; }
}
