// src/main/java/com/pingu/DOTORI/repository/CalendarRepository.java
package com.pingu.DOTORI.repository;

import com.pingu.DOTORI.entity.Calendars;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface CalendarRepository extends JpaRepository<Calendars, Long> {
  @Query("""
        select c
        from Calendars c
        where c.scheduleDate between :start and :end
        order by c.scheduleDate asc
      """)
  List<Calendars> findByScheduleDateBetween(@Param("start") LocalDateTime start,
      @Param("end") LocalDateTime end);
}
