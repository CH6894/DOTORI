package com.pingu.DOTORI.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "Users")
@Table(name = "users")
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false)
    private Long id;

    @Column(name = "user_name", length = 50)
    private String userName;

    @Column(name = "nick_name", length = 100, unique = true)
    private String nickName; // 단일 유니크

    @Column(name = "email", length = 255, unique = true)
    private String email; // 단일 유니크

    @Column(name = "phone", length = 100, unique = true)
    private String phone; // 단일 유니크

    @Column(name = "gender")
    private Byte gender;

    @Column(name = "birth_year")
    private LocalDate birthYear;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "sign_in_date")
    private LocalDateTime signInDate;

    @Column(name = "user_type")
    private Integer userType;

    @Column(name = "user_img")
    private String userImg;

    // ===== 양방향 컬렉션 =====
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<Address> address = new ArrayList<>();
    // new 붙여준 이유 : 초기에 테이블이 형성될 때, 연관된 테이블들이 null 값이 들어가기 때문에
    // [] 처럼 빈 값을 넣어주기 위함
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<Calendars> calendars = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<Cart> cart = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<Orders> orders = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<Search> search = new ArrayList<>();

    // @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval =
    // false)
    // private Set<CollectionMapping> collectionMapping = new HashSet<>();

    // 편의 메서드 예시
    // 얘가 있는 이유 -> 연관된 테이블에서 수정하면 여기서도 수정될 수 있도록
    // ex) 오더 테이블에서 특정 값 수정했을 때 여기서도 수정한다고 => 양방향 불일치 문제를 막는다고 함.
    public void addOrder(Orders order) {
        orders.add(order);
        order.setUser(this);
    }
    // public void addCart(Cart cart){ cart.add(cart); cart.setUser(this); }

}
