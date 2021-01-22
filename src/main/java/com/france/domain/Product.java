package com.france.domain;

import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Product {

    @Id @Column(name = "product_id")
    private Long id;
    private String title;
    private String vendor;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    private List<Image> productImagesList;

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    private List<Variant> variantList;

}
