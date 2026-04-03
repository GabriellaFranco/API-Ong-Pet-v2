package com.enterprise.ong_pet2.entity;

import com.enterprise.ong_pet2.enums.TipoMidia;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.Id;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "animal_midias")
public class AnimalMidia extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "animal_id", nullable = false)
    private Animal animal;

    @Column(nullable = false)
    private String url;

    @Column(nullable = false)
    private String chaveS3;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TipoMidia tipo;

    @Column(nullable = false, columnDefinition = "boolean default false")
    @Builder.Default
    private Boolean principal = false;

    @Column(nullable = false)
    @Builder.Default
    private Integer ordem = 0;

    @Column(nullable = false)
    private Long tamanhoBytes;

    @Column(nullable = false)
    private String mimeType;

    @Column(nullable = false)
    private String nomeOriginal;
}
