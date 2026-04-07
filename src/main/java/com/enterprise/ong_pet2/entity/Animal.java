package com.enterprise.ong_pet2.entity;

import com.enterprise.ong_pet2.enums.Especie;
import com.enterprise.ong_pet2.enums.Genero;
import com.enterprise.ong_pet2.enums.PorteAnimal;
import jakarta.persistence.*;
import lombok.*;
import jakarta.persistence.Id;

import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "animais")
public class Animal extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Especie especie;

    @Column(nullable = false)
    private Long idade;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Genero genero;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PorteAnimal porte;

    @Column(nullable = false)
    private String descricao;

    @Column(nullable = false, columnDefinition = "boolean default true")
    @Builder.Default
    private Boolean disponivel = true;

    @Column(nullable = false, columnDefinition = "boolean default false")
    @Builder.Default
    private Boolean vacinado = false;

    @Column(nullable = false, columnDefinition = "boolean default false")
    @Builder.Default
    private Boolean castrado = false;

    @Column(nullable = false, columnDefinition = "boolean default false")
    @Builder.Default
    private Boolean microchipado = false;

    @ManyToOne(cascade = {
            CascadeType.DETACH, CascadeType.MERGE,
            CascadeType.PERSIST, CascadeType.REFRESH
    })
    @JoinColumn(name = "id_responsavel")
    private Usuario responsavel;

    @OneToMany(mappedBy = "animal", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordem ASC")
    private List<AnimalMidia> midias = new ArrayList<>();
}
