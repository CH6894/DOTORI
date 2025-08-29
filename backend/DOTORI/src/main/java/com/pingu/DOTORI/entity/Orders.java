package com.pingu.DOTORI.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity @Table(name = "Orders")
public class Orders {

 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 @Column(name = "Order_ID")
 @EqualsAndHashCode.Include
 private Long id;

 @Column(name = "Pay_Method", nullable = false, length = 100)
 private String payMethod;

 @Column(name = "Pay_Time", nullable = false)
 private LocalDateTime payTime;

 @ManyToOne(fetch = FetchType.LAZY)
 @JoinColumn(name = "Item_ID", nullable = false)
 private ItemDetails itemDetails;

 @ManyToOne(fetch = FetchType.LAZY)
 @JoinColumn(name = "User_ID", nullable = false)
 private Users user;
}
