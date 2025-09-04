package com.pingu.DOTORI.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "notification")
public class Notification {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "notification_id")
	@EqualsAndHashCode.Include
	private Long id;

	@Column(name = "notification_title", nullable = false, length = 255)
	private String title;

	@Lob
	@Column(name = "notification_content", nullable = false)
	private String content;

	@Column(name = "notification_type", nullable = false)
	private Byte type; // 0/1
}
