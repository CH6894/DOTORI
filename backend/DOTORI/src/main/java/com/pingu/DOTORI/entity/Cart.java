package com.pingu.DOTORI.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity @Table(name = "Cart")
public class Cart {

 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 @Column(name = "Cart_ID")
 @EqualsAndHashCode.Include
 private Long id;

 @ManyToOne(fetch = FetchType.LAZY)
 @JoinColumn(name = "User_ID", nullable = false)
 private Users user;

 @ManyToOne(fetch = FetchType.LAZY)
 @JoinColumn(name = "Item_ID", nullable = false)
 private ItemDetails itemDetails;
}
