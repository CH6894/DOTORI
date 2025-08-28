package com.pingu.DOTORI.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity(name = "Item_details")
@Table(name = "Item_details")
public class ItemDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Item_ID", nullable = false)
    private Long id;

    @Column(name = "Cost", precision = 19, scale = 2)
    private BigDecimal cost;

    @Column(name = "Storage_Date")
    private LocalDateTime storageDate;

    @Column(name = "Delivery_Date")
    private LocalDateTime deliveryDate;

    @Column(name = "Status", nullable = false)
    private Boolean status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "User_ID", nullable = false,
                foreignKey = @ForeignKey(name = "FK_Users_TO_Item_details"))
    private Users user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "Item_Code", nullable = false,
                foreignKey = @ForeignKey(name = "FK_Item_TO_Item_details"))
    private Item item;
}
