package com.pingu.DOTORI.repository;

import com.pingu.DOTORI.dto.AdminListRow;
import com.pingu.DOTORI.entity.Admin;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminRepository extends JpaRepository<Admin, Long> {

    // 필터링된 검수 신청 목록을 반환하는 쿼리
	@Query("SELECT new com.pingu.DOTORI.dto.AdminListRow(a.inspectionId, a.itemDetails.itemId, a.registrationDate, " +
		       "a.itemDetails.unpacked, a.admissionState, a.quality, COUNT(i.id) AS imageCount, i.filmingTime, a.itemDetails.cost) " +
		       "FROM Admin a " +
		       "LEFT JOIN a.itemDetails.images i " +
		       "WHERE (:state IS NULL OR a.admissionState = :state) " +
		       "AND (:from IS NULL OR a.registrationDate >= :from) " +
		       "AND (:to IS NULL OR a.registrationDate <= :to) " +
		       "GROUP BY a.inspectionId, a.itemDetails.itemId, a.registrationDate, a.itemDetails.unpacked, " +
		       "a.admissionState, a.quality, i.filmingTime, a.itemDetails.cost")
		Page<AdminListRow> findByFilters(@Param("state") Integer state,
		                                 @Param("from") LocalDateTime from,
		                                 @Param("to") LocalDateTime to,
		                                 Pageable pageable);




}
