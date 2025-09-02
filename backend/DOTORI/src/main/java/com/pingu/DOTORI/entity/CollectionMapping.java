package com.pingu.DOTORI.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DB 테이블: Collection_Mapping
 * 대표 컬럼(ERD 기준):
 *  - PK(복합): Collection_ID + Item_Code
 *  - 상태(보유/인증): Status(ENUM 'INACTIVE','OWNED_UNVERIFIED','OWNED_VERIFIED'), Is_Own, Is_Buy, Is_Certified
 *  - 메타: Serial_No, Verified, Activated_At, Deactivated_At
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "Collection_Mapping")
@IdClass(CollectionMapping.Pk.class)
public class CollectionMapping {

    /** 복합 PK: Collection_ID + Item_Code */
    @Id
    @Column(name = "Collection_ID", nullable = false)
    private Long collectionId;

    @Id
    @Column(name = "Item_Code", length = 100, nullable = false)
    private String itemCode;

    /** 보유/인증 상태 ENUM — DB ENUM과 이름을 정확히 일치시킴 */
    public enum OwnStatus {
        INACTIVE, OWNED_UNVERIFIED, OWNED_VERIFIED
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", nullable = false)
    private OwnStatus status;

    @Column(name = "Is_Own", nullable = false)
    private Integer isOwn;           // 0/1

    @Column(name = "Is_Buy")
    private Integer isBuy;           // 0/1

    @Column(name = "Is_Certified")
    private Integer isCertified;     // 0/1

    @Column(name = "Serial_No", length = 64)
    private String serialNo;

    @Column(name = "Verified")
    private Integer verified;        // 0/1
    
    @Column(name = "Activated_At")
    private LocalDateTime activatedAt;

    @Column(name = "Deactivated_At")
    private LocalDateTime deactivatedAt;

    /** 복합키 정의 */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Pk implements Serializable {
        private Long collectionId;
        private String itemCode;
    }
}
