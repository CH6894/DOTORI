package com.pingu.DOTORI.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "item_img")
public class ItemImg {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "img_id")
	@EqualsAndHashCode.Include
	private Long id; // 이미지 아이디

	@Lob
	@Column(name = "img_url", nullable = false)
	private String imgUrl; // 개별 이미지 주소

	@Column(name = "img_type")
	private Byte imgType; // 대표/보조 구분 (대표:0, 보조:1)

	@Column(name = "filming_time", nullable = false)
	private LocalDateTime filmingTime; // 촬영 시간

	// --- 양방향 매핑 ---
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "item_id", nullable = false, foreignKey = @ForeignKey(name = "FK_Item_details_TO_Item_img"))
	private ItemDetails itemDetails;

	public void setItemDetails(ItemDetails itemDetails) {
		this.itemDetails = itemDetails;
	}
}
