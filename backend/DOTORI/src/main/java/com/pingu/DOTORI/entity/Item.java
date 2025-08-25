package com.pingu.DOTORI.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity @Table(name = "item")
public class Item {

    @Id
    @Column(name = "ean", length = 13)
    @EqualsAndHashCode.Include
    private String ean;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "manufacturer", length = 255)
    private String manufacturer;

    @Column(name = "texture", length = 255)
    private String texture;

    @Column(name = "release_Date")
    private LocalDate releaseDate;

    @Column(name = "size", nullable = false, length = 100)
    private String size;

    @Lob
    @Column(name = "information")
    private String information;

    @Lob
    @Column(name = "img_url", unique = true)
    private String imgUrl;

    @Column(name = "storage_fees", nullable = false)
    private Long storageFees;

    @Column(name = "genre", nullable = false, length = 100)
    private String genre;

    // Relations
    @OneToMany(mappedBy = "item", fetch = FetchType.LAZY)
    private List<ItemDetails> itemDetails;
}