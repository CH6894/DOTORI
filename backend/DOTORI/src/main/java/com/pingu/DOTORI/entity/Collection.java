//package com.pingu.DOTORI.entity;
//// 미완성임 고칠 예정 수정 필수
//import jakarta.persistence.*;
//import lombok.*;
//import java.util.*;
//
//@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
//@Entity(name = "Collection")
//@Table(name = "Collection")
//public class Collection {
//
//    @Id
//    @Column(name = "Collection_ID", nullable = false)
//    private Long id;
//
//    @Column(name = "Status", nullable = false)
//    private Short status;
//
//    @ManyToOne(fetch = FetchType.LAZY, optional = false)
//    @JoinColumn(name = "Item_Code", nullable = false,
//            foreignKey = @ForeignKey(name = "FK_Item_TO_Collection"))
//    private Item item;
//
//    // 양방향: 매핑 테이블
//    @OneToMany(mappedBy = "collection", cascade = CascadeType.ALL, orphanRemoval = false)
//    private Set<CollectionMapping> mappings = new HashSet<>();
//}
