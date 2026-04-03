package com.enterprise.ong_pet2.entity;

import com.enterprise.ong_pet2.enums.CategoriaEstoque;
import com.enterprise.ong_pet2.enums.UnidadeMedida;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "itens_estoque")
public class ItemEstoque extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CategoriaEstoque categoria;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UnidadeMedida unidadeMedida;

    @Column(nullable = false)
    @Builder.Default
    private BigDecimal quantidadeAtual = BigDecimal.ZERO;

    @Column(nullable = false)
    @Builder.Default
    private BigDecimal quantidadeMinima = BigDecimal.ZERO;

    private String descricao;

    @Column(nullable = false, columnDefinition = "boolean default true")
    @Builder.Default
    private Boolean ativo = true;

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL)
    private List<MovimentacaoEstoque> movimentacoes = new ArrayList<>();
}
