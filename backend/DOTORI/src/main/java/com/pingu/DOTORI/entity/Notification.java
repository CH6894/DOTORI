package com.pingu.DOTORI.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity @Table(name = "Notification")
public class Notification {

 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 @Column(name = "Notification_ID")
 @EqualsAndHashCode.Include
 private Long id;

 @Column(name = "Notification_Title", nullable = false, length = 255)
 private String title;

 @Lob
 @Column(name = "Notification_Content", nullable = false)
 private String content;

 @Column(name = "Notification_Type", nullable = false)
 private Byte type; // 0/1
}
