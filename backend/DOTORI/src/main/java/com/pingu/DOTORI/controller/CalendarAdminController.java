package com.pingu.DOTORI.controller;

import com.pingu.DOTORI.dto.CalendarRequest;
import com.pingu.DOTORI.dto.CalendarResponse;
import com.pingu.DOTORI.entity.Calendars;
import com.pingu.DOTORI.service.CalendarService;
import org.springframework.http.HttpStatus;
//import org.springframework.ngboot에
import org.springframework.http.ResponseEntity;
//http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/calendars")
@CrossOrigin( // ✅ 프론트(vite)에서 오는 요청 허용
    origins = "http://localhost:5173", allowedHeaders = { "*" }, methods = { RequestMethod.GET, RequestMethod.POST,
        RequestMethod.DELETE, RequestMethod.PUT, RequestMethod.PATCH,
        RequestMethod.OPTIONS }, allowCredentials = "true", maxAge = 3600)
public class CalendarAdminController {
  private final CalendarService service;

  public CalendarAdminController(CalendarService service) {
    this.service = service;
  }

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
  public String ping() {
    return "ok";
  }
}
