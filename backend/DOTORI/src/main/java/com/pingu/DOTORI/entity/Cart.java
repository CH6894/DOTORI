package com.pingu.DOTORI.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "Cart")
@Table(name = "Cart", uniqueConstraints = { @UniqueConstraint(name = "UQ_Cart_ID", columnNames = "Cart_ID"), // DDL 반영(사실 PK로 충분)
		@UniqueConstraint(name = "UQ_Cart_User_Item", columnNames = { "User_ID", "Item_ID" }) // 판단에 따른 복합 유니크
})
public class Cart {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "Cart_ID", nullable = false)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "User_ID", nullable = false, foreignKey = @ForeignKey(name = "FK_Users_TO_Cart"))
	private Users user;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "Item_ID", nullable = false, foreignKey = @ForeignKey(name = "FK_Item_details_TO_Cart"))
	private ItemDetails itemDetails;
}
