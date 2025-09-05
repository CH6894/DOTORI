package com.pingu.DOTORI.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "Item")
@Table(name = "item", uniqueConstraints = { @UniqueConstraint(name = "UQ_Img_URL", columnNames = "img_url") // TEXT 유니크는
// 테이블 레벨로
})
public class Item {

   @Id
   @Column(name = "item_code", nullable = false, length = 100)
   private String itemCode; // PK(이미 유니크)

   @Column(name = "name", nullable = false, length = 255)
   private String name;

   @Column(name = "title", nullable = false, length = 255)
   private String title;

   @Column(name = "manufacturer", length = 255)
   private String manufacturer;

   @Column(name = "material", length = 255)
   private String material;

   @Column(name = "release_month")
   private LocalDate releaseMonth;

   @Column(name = "size", length = 100)
   private String size;

   @Lob
   @Column(name = "information")
   private String information;

   @Column(name = "img_url", columnDefinition = "TEXT")
   private String imgUrl;

   @Column(name = "storage_fees", nullable = false)
   private Long storageFees;

   @Column(name = "genre", nullable = false, length = 100)
   private String genre;

   @Column(name = "cost", precision = 19, scale = 2)
   private BigDecimal cost;

   // 양방향
   @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = false)
   private List<ItemDetails> itemDetails = new ArrayList<>();

   // @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval =
   // false)
   // private Set<CollectionMapping> collections = new HashSet<>();
}
