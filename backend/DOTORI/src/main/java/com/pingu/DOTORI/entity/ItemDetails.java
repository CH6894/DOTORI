package com.pingu.DOTORI.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity @Table(name = "Item_details")
public class ItemDetails {

 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 @Column(name = "Item_ID")
 @EqualsAndHashCode.Include
 private Long id;

 @Column(name = "Cost")
 private Long cost;

 @Column(name = "Storage_Date")
 private LocalDateTime storageDate;

 @Column(name = "Delivery_Date")
 private LocalDateTime deliveryDate;

 @Column(name = "Unpacked", nullable = false)
 private Byte unpacked; // 0/1

 @Column(name = "Quality")
 private Byte quality; // 1,2,3

 @Column(name = "Sell_Status", nullable = false)
 private Boolean sellStatus;
 
 @Lob
 @Column(name = "Item_Explanation")
 private String itemExplanation;

 @ManyToOne(fetch = FetchType.LAZY)
 @JoinColumn(name = "User_ID", nullable = false)
 private Users user;

 @ManyToOne(fetch = FetchType.LAZY)
 @JoinColumn(name = "EAN", nullable = false)
 private Item item;

 // Relations
 @OneToMany(mappedBy = "itemDetails", fetch = FetchType.LAZY)
 private List<ItemImg> images;

 @OneToMany(mappedBy = "itemDetails", fetch = FetchType.LAZY)
 private List<Orders> orders;

 @OneToMany(mappedBy = "itemDetails", fetch = FetchType.LAZY)
 private List<Cart> carts;

 @OneToMany(mappedBy = "itemDetails", fetch = FetchType.LAZY)
 private List<WishList> wishLists;
}
