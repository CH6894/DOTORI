package com.pingu.DOTORI.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity @Table(name = "Item")
public class Item {

    @Id
    @Column(name = "ean", length = 255)
    @EqualsAndHashCode.Include
    private String ean;

    @Column(name = "Name", nullable = false, length = 255)
    private String name;

    @Column(name = "Title", nullable = false, length = 255)
    private String title;

    @Column(name = "Manufacturer", length = 255)
    private String manufacturer;

    @Column(name = "Texture", length = 255)
    private String texture;

    @Column(name = "Release_Date")
    private LocalDate releaseDate;

    @Column(name = "Size", length = 100)
    private String size;

    @Lob
    @Column(name = "Information")
    private String information;

    @Lob
    @Column(name = "Img_URL", unique = true)
    private String imgUrl;

    @Column(name = "StorageFees", nullable = false)
    private Long storageFees;

    @Column(name = "Genre", nullable = false, length = 100)
    private String genre;
    
    @column(name = "Cost")
    private BigDecimal cost;
    // Relations
    @OneToMany(mappedBy = "item", fetch = FetchType.LAZY)
    private List<ItemDetails> itemDetails;
}