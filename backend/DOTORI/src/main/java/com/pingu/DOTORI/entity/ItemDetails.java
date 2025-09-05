package com.pingu.DOTORI.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "item_details")
public class ItemDetails {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "item_id")
	@EqualsAndHashCode.Include
	private Long itemId; // 아이템 고유 번호

	@Column(name = "cost")
	private BigDecimal cost; // 상품 가격

	@Column(name = "storage_date")
	private LocalDateTime storageDate; // 입고 날짜

	@Column(name = "delivery_date")
	private LocalDateTime deliveryDate; // 출고 날짜

	@Column(name = "status", nullable = false)
	private Boolean status; // 판매 상태

	@Column(name = "unpacked") // 0/1
	private Boolean unpacked;

	@Lob
	@Column(name = "product_condition")
	private String productCondition; // 상품 상태 상세 메모

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "item_code", nullable = false)
	private Item item;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private Users user; // 유저 고유 번호 (단방향으로 충분

	// --- 양방향 매핑 ---
	@OneToMany(mappedBy = "itemDetails", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ItemImg> images = new ArrayList<>();

	@OneToMany(mappedBy = "itemDetails", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Admin> admins = new ArrayList<>();

	@OneToMany(mappedBy = "itemDetails", cascade = CascadeType.ALL, orphanRemoval = false)
	private List<Cart> carts = new ArrayList<>();

	@OneToMany(mappedBy = "itemDetails", cascade = CascadeType.ALL, orphanRemoval = false)
	private List<Orders> orders = new ArrayList<>();

	// --- 편의 메서드 ---
	public void addImage(ItemImg image) {
		images.add(image);
		image.setItemDetails(this);
	}

	public void addAdmin(Admin admin) {
		admins.add(admin);
		admin.setItemDetails(this);
	}
}