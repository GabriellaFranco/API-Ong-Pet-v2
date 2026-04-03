package com.enterprise.ong_pet2.entity;

import com.enterprise.ong_pet2.enums.Especie;
import com.enterprise.ong_pet2.enums.FaixaRenda;
import com.enterprise.ong_pet2.enums.PorteAnimal;
import com.enterprise.ong_pet2.enums.TipoMoradia;
import jakarta.persistence.*;
import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "perfis_adotante")
public class PerfilAdotante extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "id_usuario", nullable = false, unique = true)
    private Usuario usuario;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TipoMoradia tipoMoradia;

    private Integer areaM2;

    @Column(nullable = false)
    private Boolean temOutrosPets;

    private String descricaoOutrosPets;

    @Column(nullable = false)
    private String rotinaDiaria;

    @Column(nullable = false)
    private Integer horasEmCasaPorDia;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private FaixaRenda rendaMensalFaixa;

    @Column(nullable = false)
    private Boolean experienciaAnimais;

    @Column(nullable = false)
    private String motivacaoAdocao;

    @Enumerated(EnumType.STRING)
    private Especie especiePreferida;

    @Enumerated(EnumType.STRING)
    private PorteAnimal portePreferido;

    @Column(nullable = false)
    private Boolean aceitaCriancas;

    private Integer scoreRisco;
}
