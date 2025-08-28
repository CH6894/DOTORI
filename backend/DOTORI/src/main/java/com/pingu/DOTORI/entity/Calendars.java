package com.pingu.DOTORI.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "Calendars")
@Table(name = "Calendars")
public class Calendars {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "Schedule_ID", nullable = false)
	private Long id;

	@Column(name = "Schedule_Date", nullable = false)
	private LocalDateTime scheduleDate;

	@Column(name = "Schedule_Name", nullable = false, length = 255)
	private String scheduleName;

	@Lob
	@Column(name = "Schedule_Info")
	private String scheduleInfo;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "User_ID", nullable = false, foreignKey = @ForeignKey(name = "FK_Users_TO_Calendar"))
	private Users user;
}
