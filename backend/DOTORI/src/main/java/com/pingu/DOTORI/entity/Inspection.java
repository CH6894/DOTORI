package com.pingu.DOTORI.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity @Table(name = "Inspection")
public class Inspection {

 @Id
 @Column(name = "Inspection_Standard_ID", length = 100)
 @EqualsAndHashCode.Include
 private String inspectionStandardId;

 @Lob
 @Column(name = "Inspection_Content", nullable = false)
 private String inspectionContent;

 @Column(name = "Category", nullable = false)
 private Long category;
}
