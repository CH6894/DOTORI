package com.pingu.DOTORI.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "Item")
@Table(name = "Item", uniqueConstraints = { @UniqueConstraint(name = "UQ_Img_URL", columnNames = "Img_URL") // TEXT 유니크는
																											// 테이블 레벨로
})
public class Item {

	@Id
	@Column(name = "Item_Code", nullable = false, length = 100)
	private String itemCode; // PK(이미 유니크)

	@Column(name = "Name", nullable = false, length = 255)
	private String name;

	@Column(name = "Title", nullable = false, length = 255)
	private String title;

	@Column(name = "Manufacturer", length = 255)
	private String manufacturer;

	@Column(name = "Texture", length = 255)
	private String texture;

	@Column(name = "Release_Date")
	private LocalDate releaseDate;

	@Column(name = "Size", nullable = false, length = 100)
	private String size;

	@Lob
	@Column(name = "Information")
	private String information;

	@Column(name = "Img_URL", columnDefinition = "TEXT")
	private String imgUrl;

	@Column(name = "Storage Fees", nullable = false)
	private Long storageFees;

	@Column(name = "Genre", nullable = false, length = 100)
	private String genre;

	@Column(name = "Cost", precision = 19, scale = 2)
	private BigDecimal cost;

	// 양방향
	@OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = false)
	private List<ItemDetails> itemDetails = new ArrayList<>();

//	@OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = false)
//	private Set<Collections> collections = new HashSet<>();
}

