package com.pingu.DOTORI.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "orders")
public class Orders {

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   @Column(name = "id", nullable = false)
   private Long id;

   @ManyToOne(fetch = FetchType.LAZY)
   @JoinColumn(name = "item_id", nullable = false, foreignKey = @ForeignKey(name = "FK_Item_details_TO_Orders"))
   private ItemDetails itemDetails;

   @Column(name = "pay_time", nullable = false)
   private LocalDateTime payTime;

   @ManyToOne(fetch = FetchType.LAZY, optional = false)
   @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "FK_Users_TO_Orders"))
   private Users user;

   @Column(name = "depositer_name", length = 50)
   private String depositerName;

   @Column(name = "pay_method", nullable = false, length = 100)
   private String payMethod;

   @Lob
   @Column(name = "pay_message", columnDefinition = "TEXT")
   private String payMessage;

}
