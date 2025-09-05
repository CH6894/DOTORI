package com.pingu.DOTORI.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "Cart")
@Table(name = "cart", uniqueConstraints = { @UniqueConstraint(name = "UQ_Cart_ID", columnNames = "cart_id"), // DDL
																												// 반영(사실
																												// PK로
																												// 충분)
		@UniqueConstraint(name = "UQ_Cart_User_Item", columnNames = { "user_id", "item_id" }) // 판단에 따른 복합 유니크
})
public class Cart {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "cart_id", nullable = false)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "FK_Users_TO_Cart"))
	private Users user;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "item_id", nullable = false, foreignKey = @ForeignKey(name = "FK_Item_details_TO_Cart"))
	private ItemDetails itemDetails;

	// 장바구니 수량 필드 추가
	@Column(name = "quantity", nullable = false)
	private int quantity;
}
