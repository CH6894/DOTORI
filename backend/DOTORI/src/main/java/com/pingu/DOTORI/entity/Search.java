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
@Table(name = "Search")
public class Search {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "Search_ID")
	private Long id;

	@Column(name = "Key_Word", nullable = false, length = 255)
	private String keyWord;

	@Column(name = "Created_at", nullable = false)
	private LocalDateTime createdAt;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "User_ID", nullable = false, foreignKey = @ForeignKey(name = "FK_Users_TO_Search"))
	private Users user;
}
