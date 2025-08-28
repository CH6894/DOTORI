package com.pingu.DOTORI.service;

import com.pingu.DOTORI.entity.Calendars;
import com.pingu.DOTORI.repository.CalendarRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional
public class CalendarService {

    private final CalendarRepository repo;

    public Calendars create(Calendars in) {
        if (in.getScheduleName() == null || in.getScheduleName().isBlank()) {
            throw new IllegalArgumentException("schedule_name은 필수이다.");
        }
        if (in.getScheduleDate() == null) {
            throw new IllegalArgumentException("schedule_date는 필수이다.");
        }
        return repo.save(in);  // LocalDateTime 그대로 저장됨
    }

    @Transactional(readOnly = true)
    public List<Calendars> getList(LocalDateTime start, LocalDateTime end) {
        return repo.findInRange(start, end);
    }

    public Calendars update(Long id, Calendars patch) {
        Calendars cur = repo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("일정을 찾을 수 없다: " + id));

        if (patch.getScheduleName() != null) cur.setScheduleName(patch.getScheduleName());
        if (patch.getScheduleInfo() != null) cur.setScheduleInfo(patch.getScheduleInfo());
        if (patch.getScheduleDate() != null) cur.setScheduleDate(patch.getScheduleDate());
        if (patch.getUserId() != null) cur.setUserId(patch.getUserId());

        return cur; // 영속 객체 → 트랜잭션 종료 시 자동 UPDATE flush
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }
}
