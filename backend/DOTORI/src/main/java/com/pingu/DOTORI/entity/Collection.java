package com.pingu.DOTORI.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * DB 테이블: Collection
 * 컬럼(ERD 기준): Collection_ID(PK), Status(TINYINT), User_ID(BIGINT)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "Collection")
public class Collection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Collection_ID")
    private Long collectionId;

    /** 도감 상태: 0(비활성), 1(활성-미인증), 2(활성-인증) 등 TINYINT로 운영 */
    @Column(name = "Status", nullable = false)
    private Byte status;

    /** 소유자(Users.User_ID) — FK 제약은 DB에 맞춰 운용, 여기서는 수치 컬럼으로 보유 */
    @Column(name = "User_ID", nullable = false)
    private Long userId;
}
