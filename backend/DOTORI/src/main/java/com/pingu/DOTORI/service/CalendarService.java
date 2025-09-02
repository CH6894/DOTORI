
package com.pingu.DOTORI.service;

import com.pingu.DOTORI.dto.CalendarRequest;
import com.pingu.DOTORI.entity.Calendars;
import com.pingu.DOTORI.entity.Users;
import com.pingu.DOTORI.repository.CalendarRepository;
import com.pingu.DOTORI.repository.UsersRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@Transactional(readOnly = true)
public class CalendarService {

    private final CalendarRepository calendarRepository;
    private final UsersRepository usersRepository;

    public CalendarService(CalendarRepository calendarRepository,
            UsersRepository usersRepository) {
        this.calendarRepository = calendarRepository;
        this.usersRepository = usersRepository;
    }

    public List<Calendars> findInRange(LocalDateTime start, LocalDateTime end) {
        return calendarRepository.findByScheduleDateBetween(start, end);
    }

    @Transactional
    public Calendars create(CalendarRequest req) {
        if (req.getUserId() == null) {
            throw new ResponseStatusException(BAD_REQUEST, "userId는 필수이다.");
        }
        Users user = usersRepository.findById(req.getUserId())
                .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "존재하지 않는 userId이다."));

        if (req.getScheduleName() == null || req.getScheduleDate() == null) {
            throw new ResponseStatusException(BAD_REQUEST, "scheduleName, scheduleDate는 필수이다.");
        }

        Calendars c = new Calendars();
        c.setScheduleName(req.getScheduleName());
        c.setScheduleDate(req.getScheduleDate());
        c.setScheduleInfo(req.getScheduleInfo());
        c.setUser(user);

        return calendarRepository.save(c);
    }

    @Transactional
    public void delete(Long id) {
        Calendars c = calendarRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "일정을 찾을 수 없다."));
        calendarRepository.delete(c);
    }

    // ✅ 추가된 update 메서드
    @Transactional
    public Calendars update(Long id, CalendarRequest req) {
        Calendars c = calendarRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "수정할 일정을 찾을 수 없다."));

        if (req.getScheduleName() != null) {
            c.setScheduleName(req.getScheduleName());
        }

        if (req.getScheduleDate() != null) {
            c.setScheduleDate(req.getScheduleDate());
        }

        if (req.getScheduleInfo() != null) {
            c.setScheduleInfo(req.getScheduleInfo());
        }

        if (req.getUserId() != null) {
            Users user = usersRepository.findById(req.getUserId())
                    .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "존재하지 않는 userId이다."));
            c.setUser(user);
        }

        return calendarRepository.save(c);
    }
}
