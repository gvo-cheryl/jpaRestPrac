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
public class Image {

    @Id @Column(name = "image_id")
    private Long imageId;
    private String position;
    @Column(name = "i_createdAt")
    private LocalDateTime created_at;
    @Column(name = "i_updatedAt")
    private LocalDateTime  updated_at;
    private String imagePath;

    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "product_id")
    private Product product;

    @OneToMany(mappedBy = "image")

    private List<VariantId> variantIds;

}
