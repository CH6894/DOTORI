// src/main/java/com/pingu/DOTORI/repository/CalendarRepository.java
package com.pingu.DOTORI.repository;

import com.pingu.DOTORI.entity.Calendars;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.*;

public interface CalendarRepository extends JpaRepository<Calendars, Long> {

    @Query("""
      select c from Calendars c
      where c.scheduleDate <= :end
      order by c.scheduleDate asc
    """)
    List<Calendars> findStartingBefore(LocalDateTime end);
}
