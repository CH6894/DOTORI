package com.pingu.DOTORI.entity;

//src/main/java/com/example/domain/entity/Users.java

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity @Table(name = "Users")
public class Users {

 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 @Column(name = "User_ID")
 @EqualsAndHashCode.Include
 private Long id;

 @Column(name = "User_Name", length = 50)
 private String userName;

 @Column(name = "NickName", length = 100, unique = true)
 private String nickName;

 @Column(name = "Email", length = 255, nullable = false, unique = true)
 private String email;

 @Column(name = "Phone", length = 100, nullable = false, unique = true)
 private String phone;

 @Column(name = "Gender", nullable = false)
 private Byte gender; // TINYINT

 @Column(name = "BirthDate", nullable = false)
 private LocalDate birthDate;
 
 @Column(name = "BirthYear", nullable = false)
 private LocalDate birthYear;

 @Column(name = "Sign_in_Date", nullable = false)
 private LocalDateTime signInDate;

 @Column(name = "UserType", nullable = false)
 private Integer userType;

 @Column(name = "UserImg")
 private String userImg
 
 // Relations (양방향 컬렉션은 필요시만 유지)
 @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
 private List<Address> addresses;

 @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
 private List<Cart> carts;

 @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
 private List<ItemDetails> itemDetails;

 @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
 private List<Orders> orders;

 @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
 private List<WishList> wishLists;

 @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
 private List<Search> searches;
}
