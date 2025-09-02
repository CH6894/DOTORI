package com.pingu.DOTORI.dto;

import com.pingu.DOTORI.entity.Calendars;
import java.time.format.DateTimeFormatter;

public class CalendarResponse {

    private Long id;
    private String title; // scheduleName
    private String start; // scheduleDate(ISO_LOCAL_DATE_TIME)
    private String info; // scheduleInfo

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public static CalendarResponse from(Calendars c) {
        CalendarResponse r = new CalendarResponse();
        r.id = c.getId();
        r.title = c.getScheduleName();
        r.start = (c.getScheduleDate() == null) ? null : c.getScheduleDate().format(ISO);
        r.info = c.getScheduleInfo();
        return r;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getStart() {
        return start;
    }

    public String getInfo() {
        return info;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public void setInfo(String info) {
        this.info = info;
    }
}
