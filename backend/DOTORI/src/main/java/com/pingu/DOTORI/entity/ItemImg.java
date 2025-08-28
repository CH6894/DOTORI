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
@Table(name = "Item_img")
public class ItemImg {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "Img_ID")
	@EqualsAndHashCode.Include
	private Long id; // 이미지 아이디

	@Lob
	@Column(name = "Img_URL", nullable = false)
	private String imgUrl; // 개별 이미지 주소

	@Column(name = "Img_Type")
	private Byte imgType; // 대표/보조 구분 (대표:0, 보조:1)

	@Column(name = "Filming_time")
	private LocalDateTime filmingTime; // 촬영 시간

	// --- 양방향 매핑 ---
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "Item_ID", nullable = false, foreignKey = @ForeignKey(name = "FK_Item_details_TO_Item_img"))
	private ItemDetails itemDetails;

	public void setItemDetails(ItemDetails itemDetails) {
		this.itemDetails = itemDetails;
	}
}
