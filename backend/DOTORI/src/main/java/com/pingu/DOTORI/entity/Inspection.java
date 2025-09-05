package com.pingu.DOTORI.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "Inspection")
@Table(name = "inspection")
public class Inspection {

	@Id
	@Column(name = "inspection_standard_id", nullable = false, length = 100)
	private String inspectionStandardId;

	@Lob
	@Column(name = "inspection_content", nullable = false)
	private String inspectionContent;

	@Column(name = "category", nullable = false)
	private Long category;
}
