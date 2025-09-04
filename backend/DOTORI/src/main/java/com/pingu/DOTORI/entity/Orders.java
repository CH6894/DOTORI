package com.pingu.DOTORI.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "Orders")
@Table(name = "orders")
public class Orders {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	private Long id;

	@Column(name = "pay_method", nullable = false, length = 100)
	private String payMethod;

	@Column(name = "pay_time", nullable = false)
	private LocalDateTime payTime;

	@Lob
	@Column(name = "pay_message")
	private String payMessage;

	@Column(name = "depositer_name", length = 50)
	private String depositerName;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "item_id", nullable = false, foreignKey = @ForeignKey(name = "FK_Item_details_TO_Orders"))
	private ItemDetails itemDetails;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "FK_Users_TO_Orders"))
	private Users user;
}
