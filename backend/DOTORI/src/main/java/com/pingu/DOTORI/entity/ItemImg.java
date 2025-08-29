package com.pingu.DOTORI.entity;


import jakarta.persistence.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity @Table(name = "Item_img")
public class ItemImg {

 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 @Column(name = "Img_ID")
 @EqualsAndHashCode.Include
 private Long id;

 @Lob
 @Column(name = "Img_URL", unique = true)
 private String imgUrl;

 @ManyToOne(fetch = FetchType.LAZY)
 @JoinColumn(name = "Item_ID", nullable = false)
 private ItemDetails itemDetails;
}
