//package com.pingu.DOTORI.controller;
//
//import com.pingu.DOTORI.entity.Calendars;
//import com.pingu.DOTORI.service.CalendarService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.format.annotation.DateTimeFormat;
//import org.springframework.web.bind.annotation.*;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/public/calendars")
//@RequiredArgsConstructor
//public class CalendarPublicController {
//
//    private final CalendarService service;
//
//    @GetMapping
//    public List<Calendars> list(
//            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
//            @RequestParam("end")   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end
//    ) {
//        return service.getList(start, end);
//    }
//}
