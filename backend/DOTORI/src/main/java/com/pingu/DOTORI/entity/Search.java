package com.pingu.DOTORI.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "Search")
@Table(name = "search")
public class Search {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "search_id")
	private Long id;

	@Column(name = "key_word", nullable = false, length = 255)
	private String keyWord;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "FK_Users_TO_Search"))
	private Users user;
}
