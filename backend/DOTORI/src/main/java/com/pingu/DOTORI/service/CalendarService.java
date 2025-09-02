package com.pingu.DOTORI.service;

import com.pingu.DOTORI.dto.CalendarRequest;
import com.pingu.DOTORI.dto.CalendarResponse;
import com.pingu.DOTORI.entity.Calendars;
import com.pingu.DOTORI.entity.Users;
import com.pingu.DOTORI.repository.CalendarRepository;
import com.pingu.DOTORI.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CalendarService {

    private final CalendarRepository calendarRepository; // ✅ 리포지토리 주입
    private final UsersRepository usersRepository;       // (필요 시)

    /** 기간 조회 */
    public List<CalendarResponse> findInRange(LocalDateTime start, LocalDateTime end) {
        // Repository에 맞춰 호출 (메소드명은 너희 레포지토리에 맞게 사용)
        // 예시1) 사용자 정의 쿼리 있을 때: calendarRepository.findStartingBefore(end)
        // 예시2) 메소드 쿼리로 대체:
        List<Calendars> rows = calendarRepository
                .findByScheduleDateBetween(start, end); // ← 없으면 아래 레포지토리 예시도 참고

        return rows.stream()
                .map(c -> new CalendarResponse(
                        c.getId(),
                        c.getScheduleName(),                  // title
                        c.getScheduleDate().toString(),       // start (String or ISO 필요시 포맷터 적용)
                        null,                                 // end (없으면 null)
                        false,                                // allDay (필요시 엔티티/메타에서 꺼내기)
                        c.getScheduleInfo(),                  // description
                        null                                  // color
                ))
                .toList();
    }

    /** 생성 */
    public CalendarResponse create(CalendarRequest req) {
        Users user = usersRepository.findById(req.getUserId()).orElseThrow();

        Calendars c = Calendars.builder()
                .scheduleDate(req.getScheduleDate())
                .scheduleName(req.getScheduleName())
                .scheduleInfo(req.getScheduleInfo())
                .user(user)
                .build();

        c = calendarRepository.save(c);

        return new CalendarResponse(
                c.getId(),
                c.getScheduleName(),
                c.getScheduleDate().toString(),
                null,
                false,
                c.getScheduleInfo(),
                null
        );
    }

    /** 부분 수정 */
    public CalendarResponse update(Long id, CalendarRequest req) {
        Calendars c = calendarRepository.findById(id).orElseThrow();

        if (req.getScheduleDate() != null) c.setScheduleDate(req.getScheduleDate());
        if (req.getScheduleName() != null) c.setScheduleName(req.getScheduleName());
        if (req.getScheduleInfo() != null) c.setScheduleInfo(req.getScheduleInfo());
        if (req.getUserId() != null) {
            Users user = usersRepository.findById(req.getUserId()).orElseThrow();
            c.setUser(user);
        }

        c = calendarRepository.save(c);

        return new CalendarResponse(
                c.getId(),
                c.getScheduleName(),
                c.getScheduleDate().toString(),
                null,
                false,
                c.getScheduleInfo(),
                null
        );
    }

    /** 삭제 */
    public void delete(Long id) {
        calendarRepository.deleteById(id);
    }
}

