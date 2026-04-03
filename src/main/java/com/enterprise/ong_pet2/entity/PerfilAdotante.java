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

    @Column(name = "area_m2")
    private Integer areaM2;

    @Column(name = "tem_outros_pets", nullable = false)
    private Boolean temOutrosPets;

    @Column(name = "descricao_outros_pets")
    private String descricaoOutrosPets;

    @Column(name = "rotina_diaria", nullable = false)
    private String rotinaDiaria;

    @Column(name = "horas_em_casa_por_dia", nullable = false)
    private Integer horasEmCasaPorDia;

    @Column(name = "renda_mensal_faixa", nullable = false)
    @Enumerated(EnumType.STRING)
    private FaixaRenda rendaMensalFaixa;

    @Column(name = "experiencia_animais", nullable = false)
    private Boolean experienciaAnimais;

    @Column(name = "motivacao_adocao", nullable = false)
    private String motivacaoAdocao;

    @Column(name = "especie_preferida")
    @Enumerated(EnumType.STRING)
    private Especie especiePreferida;

    @Column(name = "porte_preferido")
    @Enumerated(EnumType.STRING)
    private PorteAnimal portePreferido;

    @Column(name = "aceita_criancas", nullable = false)
    private Boolean aceitaCriancas;

    @Column(name = "score_risco")
    private Integer scoreRisco;
}
