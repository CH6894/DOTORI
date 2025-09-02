package com.pingu.DOTORI.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "WishList")
@Table(name = "WishList", uniqueConstraints = @UniqueConstraint(name = "UQ_Wish_User_Item", columnNames = { "User_ID",
		"Item_ID" }))
public class WishList {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "WishList_ID", nullable = false)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "User_ID", nullable = false)
	private Users user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "Item_ID", nullable = false)
	private ItemDetails itemDetails;
}
