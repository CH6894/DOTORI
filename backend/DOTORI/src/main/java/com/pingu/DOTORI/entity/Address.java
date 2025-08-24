package com.pingu.DOTORI.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity @Table(name = "Address")
public class Address {

 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 @Column(name = "Address_ID")
 @EqualsAndHashCode.Include
 private Long id;

 @Column(name = "Main_Address", length = 255)
 private String mainAddress;

 @ManyToOne(fetch = FetchType.LAZY)
 @JoinColumn(name = "User_ID", nullable = false)
 private Users user;
}