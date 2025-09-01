package com.pingu.DOTORI.controller;

import com.pingu.DOTORI.dto.CalendarRequest;
import com.pingu.DOTORI.dto.CalendarResponse;
import com.pingu.DOTORI.service.CalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/calendars")
@RequiredArgsConstructor
public class CalendarAdminController {

    private final CalendarService service;

    @PostMapping
    public ResponseEntity<CalendarResponse> create(@RequestBody CalendarRequest body) {
        return ResponseEntity.ok(service.create(body));
    }

    // 부분 수정
    @PatchMapping("/{id}")
    public ResponseEntity<CalendarResponse> patch(@PathVariable Long id,
                                                  @RequestBody CalendarRequest body) {
        return ResponseEntity.ok(service.update(id, body));
    }

    // 전체 수정이 꼭 필요하면 주석 해제해 사용 (Service 재사용)
    // @PutMapping("/{id}")
    // public ResponseEntity<CalendarResponse> put(@PathVariable Long id,
    //                                             @RequestBody CalendarRequest body) {
    //     return ResponseEntity.ok(service.update(id, body));
    // }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
