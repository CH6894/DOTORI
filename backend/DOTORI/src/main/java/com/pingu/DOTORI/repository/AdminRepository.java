package com.pingu.DOTORI.repository;

import com.pingu.DOTORI.dto.AdminListRow;
import com.pingu.DOTORI.entity.Admin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface AdminRepository extends JpaRepository<Admin, Long> {

	@Query("""
		    SELECT a.inspectionId AS inspectionId,
		           d.itemId AS itemId,
		           a.registrationDate AS registrationDate,
		           a.unpacked AS unpacked,
		           a.admissionState AS admissionState,
		           a.quality AS quality,
		           d.cost AS cost
		    FROM Admin a
		    JOIN a.itemDetails d
		    WHERE (:admissionState IS NULL OR a.admissionState = :admissionState)
		      AND (:from IS NULL OR a.registrationDate >= :from)
		      AND (:to IS NULL OR a.registrationDate <= :to)
		""")
    Page<AdminListRow> findAdminList(
            @Param("admissionState") Integer admissionState,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable
    );
}
