package com.pingu.DOTORI.repository;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

import com.pingu.DOTORI.dto.AdminListRow;
import com.pingu.DOTORI.entity.Admin;

public interface AdminRepository extends JpaRepository<Admin, Long> {

    // ✅ Native Query (MySQL 전용: GROUP_CONCAT 사용)
    @Query(
        value = """
            SELECT 
              a.inspection_id AS inspectionId,
              i.item_id AS itemId,
              it.name AS title,
              u.user_name AS sellerName,
              i.cost AS cost,
              i.unpacked AS unpacked,
              a.registration_date AS registrationDate,
              a.admission_state AS admissionState,
              a.quality AS quality,
              a.item_explanation AS itemExplanation,
              COUNT(img.img_id) AS imageCount,
              MIN(img.filming_time) AS filmingTime,
              GROUP_CONCAT(img.img_url ORDER BY img.img_id) AS imageUrls
            FROM admin a
            JOIN item_details i ON a.item_id = i.item_id
            JOIN item it ON i.item_code = it.item_code
            JOIN users u ON i.user_id = u.user_id
            LEFT JOIN item_img img ON i.item_id = img.item_id
            WHERE (:state IS NULL OR a.admission_state = :state)
              AND (:from IS NULL OR a.registration_date >= :from)
              AND (:to IS NULL OR a.registration_date <= :to)
            GROUP BY a.inspection_id, i.item_id, it.name, u.user_name, i.cost, i.unpacked, 
                     a.registration_date, a.admission_state, a.quality, a.item_explanation
            ORDER BY a.inspection_id DESC
            """,
        countQuery = """
            SELECT COUNT(DISTINCT a.inspection_id) 
            FROM admin a
            JOIN item_details i ON a.item_id = i.item_id
            JOIN item it ON i.item_code = it.item_code
            JOIN users u ON i.user_id = u.user_id
            LEFT JOIN item_img img ON i.item_id = img.item_id
            WHERE (:state IS NULL OR a.admission_state = :state)
              AND (:from IS NULL OR a.registration_date >= :from)
              AND (:to IS NULL OR a.registration_date <= :to)
            """,
        nativeQuery = true
    )
    Page<Object[]> findByFiltersNative(@Param("state") Integer state,
                                       @Param("from") LocalDateTime from,
                                       @Param("to") LocalDateTime to,
                                       Pageable pageable);

}
