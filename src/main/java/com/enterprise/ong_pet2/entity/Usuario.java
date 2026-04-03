package com.enterprise.ong_pet2.entity;

import com.enterprise.ong_pet2.enums.PerfilUsuario;
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
@Table(name = "usuarios")
public class Usuario extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true)
    private String cpf;

    @Column(nullable = false)
    private String senha;

    @Column(nullable = false)
    private String cep;

    @Column(nullable = false)
    private String cidade;

    @Column(nullable = false)
    private String bairro;

    @Column(nullable = false)
    private String rua;

    @Column(nullable = false)
    private Long numEndereco;

    private String telefone;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PerfilUsuario perfil;

    @ManyToMany(fetch = FetchType.EAGER, cascade = {
            CascadeType.DETACH, CascadeType.MERGE,
            CascadeType.PERSIST, CascadeType.REFRESH
    })
    @JoinTable(
            name = "autoridades_usuario",
            joinColumns = @JoinColumn(name = "id_usuario"),
            inverseJoinColumns = @JoinColumn(name = "id_autoridade")
    )
    private List<Autoridade> autoridades = new ArrayList<>();

    @OneToMany(mappedBy = "responsavel", cascade = CascadeType.ALL)
    private List<Animal> animaisCadastrados = new ArrayList<>();

    @OneToMany(mappedBy = "adotante", cascade = CascadeType.ALL)
    private List<PedidoAdocao> pedidosComoAdotante = new ArrayList<>();

    @OneToMany(mappedBy = "voluntarioResponsavel", cascade = CascadeType.ALL)
    private List<PedidoAdocao> pedidosAnalisados = new ArrayList<>();

    @OneToMany(mappedBy = "doador", cascade = CascadeType.ALL)
    private List<Doacao> doacoes = new ArrayList<>();

    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL)
    private PerfilAdotante perfilAdotante;
}
