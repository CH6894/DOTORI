//package com.pingu.DOTORI.controller;
//
//import com.pingu.DOTORI.entity.Calendars;
//import com.pingu.DOTORI.service.CalendarService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api/admin/calendars")
//@RequiredArgsConstructor
//public class CalendarAdminController {
//
//    private final CalendarService service;
//
//    @PostMapping
//    public ResponseEntity<Calendars> create(@RequestBody Calendars body) {
//        return ResponseEntity.ok(service.create(body));
//    }
//
//    @PutMapping("/{id}")
//    public ResponseEntity<Calendars> update(@PathVariable Long id,
//                                            @RequestBody Calendars body) {
//        return ResponseEntity.ok(service.update(id, body));
//    }
//
//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> delete(@PathVariable Long id) {
//        service.delete(id);
//        return ResponseEntity.noContent().build();
//    }
//}
