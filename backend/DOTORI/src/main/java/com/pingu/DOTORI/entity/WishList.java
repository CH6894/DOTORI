package com.pingu.DOTORI.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity @Table(name = "WishList")
public class WishList {

 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 @Column(name = "WishList_ID")
 @EqualsAndHashCode.Include
 private Long id;

 @ManyToOne(fetch = FetchType.LAZY)
 @JoinColumn(name = "User_ID", nullable = false)
 private Users user;

 @ManyToOne(fetch = FetchType.LAZY)
 @JoinColumn(name = "Item_ID", nullable = false)
 private ItemDetails itemDetails;

 @Column(name = "Is_Liked", nullable = false)
 private Boolean isLiked;
}
