//package com.pingu.DOTORI.repository;
//
//import com.pingu.DOTORI.entity.Admin;
//import com.pingu.DOTORI.dto.AdminListRow;
//import org.springframework.data.domain.*;
//import org.springframework.data.jpa.repository.*;
//import org.springframework.data.repository.query.Param;
//
//import java.time.Instant;
//import java.util.List;
//
//public interface AdminRepository extends JpaRepository<Admin, Long> {
//
//  /** 상태 필터 페이징 (엔티티 그대로) */
//  Page<Admin> findByAdmissionState(Integer admissionState, Pageable pageable);
//
//  /** 기간 + 상태 필터 페이징 (엔티티 그대로) */
//  Page<Admin> findByRegistrationDateBetweenAndAdmissionState(
//      Instant from, Instant to, Integer admissionState, Pageable pageable);
//
//  /** 상태별 카운트 (상단 배지 등에 사용) */
//  long countByAdmissionState(Integer admissionState);
//
//  /** 특정 아이템의 마지막 신청건 조회(있으면) */
//  List<Admin> findTop1ByItemIdOrderByRegistrationDateDesc(Long itemId);
//
//  /* =========================
//   * 목록 화면 최적화 쿼리 (Projection)
//   * - 이미지 개수/최초 촬영시각을 서브쿼리로 뽑아와 한 방에 페이징
//   * - 스키마 이름 그대로 사용 (nativeQuery)
//   * ========================= */
//  @Query(
//    value = """
//      SELECT
//        a.inspection_id  AS inspectionId,
//        a.item_id        AS itemId,
//        a.registration_date AS registrationDate,
//        a.unpacked       AS unpacked,
//        a.admission_state AS admissionState,
//        a.quality        AS quality,
//        (SELECT COUNT(*) FROM item_img ii WHERE ii.item_id = a.item_id)       AS imageCount,
//        (SELECT MIN(ii.filming_time) FROM item_img ii WHERE ii.item_id = a.item_id) AS firstFilmingTime,
//        d.cost           AS cost
//      FROM admin a
//      LEFT JOIN item_details d ON d.item_id = a.item_id
//      WHERE (:state IS NULL OR a.admission_state = :state)
//        AND (:from IS NULL OR a.registration_date >= :from)
//        AND (:to   IS NULL OR a.registration_date <  :to)
//      ORDER BY a.registration_date DESC
//      """,
//    countQuery = """
//      SELECT COUNT(*)
//      FROM admin a
//      WHERE (:state IS NULL OR a.admission_state = :state)
//        AND (:from IS NULL OR a.registration_date >= :from)
//        AND (:to   IS NULL OR a.registration_date <  :to)
//      """,
//    nativeQuery = true
//  )
//  Page<AdminListRow> findAdminList(
//      @Param("state") Integer state,
//      @Param("from") Instant from,
//      @Param("to")   Instant to,
//      Pageable pageable
//  );
//}
