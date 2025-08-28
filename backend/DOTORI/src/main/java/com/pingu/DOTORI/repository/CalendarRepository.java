package com.pingu.DOTORI.repository;

import com.pingu.DOTORI.entity.Calendars;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface CalendarRepository extends JpaRepository<Calendars, Long> {

    // 기간 조회(달력 범위)
	@Query("""
		    SELECT c FROM Calendars c
		     WHERE c.scheduleDate BETWEEN :start AND :end
		     ORDER BY c.scheduleDate ASC
		""")
		List<Calendars> findInRange(@Param("start") LocalDateTime start,
		                            @Param("end") LocalDateTime end);

}
