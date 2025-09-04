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
@Table(name = "Orders")
public class Orders {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	private Long id;

	@Column(name = "Pay_Method", nullable = false, length = 100)
	private String payMethod;

	@Column(name = "Pay_Time", nullable = false)
	private LocalDateTime payTime;

	@Lob
	@Column(name = "Pay_Message")
	private String payMessage;

	@Column(name = "Depositer_Name", length = 50)
	private String depositerName;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "Item_ID", nullable = false, foreignKey = @ForeignKey(name = "FK_Item_details_TO_Orders"))
	private ItemDetails itemDetails;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "User_ID", nullable = false, foreignKey = @ForeignKey(name = "FK_Users_TO_Orders"))
	private Users user;
}
