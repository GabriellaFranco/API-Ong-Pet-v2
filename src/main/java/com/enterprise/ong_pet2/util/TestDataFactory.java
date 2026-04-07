package com.enterprise.ong_pet2.util;

import com.enterprise.ong_pet2.entity.*;
import com.enterprise.ong_pet2.enums.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TestDataFactory {

    public static Usuario umUsuarioPadrao() {
        return Usuario.builder()
                .id(1L)
                .nome("João Silva")
                .email("joao@email.com")
                .cpf("123.456.789-00")
                .senha("senha123")
                .cep("89000-000")
                .cidade("Blumenau")
                .bairro("Centro")
                .rua("Rua XV")
                .numEndereco(100L)
                .perfil(PerfilUsuario.PADRAO)
                .autoridades(new ArrayList<>())
                .build();
    }

    public static Usuario umVoluntario() {
        return Usuario.builder()
                .id(2L)
                .nome("Maria Voluntária")
                .email("maria@email.com")
                .cpf("987.654.321-00")
                .senha("senha123")
                .cep("89000-000")
                .cidade("Blumenau")
                .bairro("Centro")
                .rua("Rua XV")
                .numEndereco(200L)
                .perfil(PerfilUsuario.VOLUNTARIO)
                .autoridades(new ArrayList<>())
                .build();
    }

    public static Usuario umAdmin() {
        return Usuario.builder()
                .id(3L)
                .nome("Admin")
                .email("admin@email.com")
                .cpf("111.222.333-44")
                .senha("senha123")
                .cep("89000-000")
                .cidade("Blumenau")
                .bairro("Centro")
                .rua("Rua XV")
                .numEndereco(300L)
                .perfil(PerfilUsuario.ADMIN)
                .autoridades(new ArrayList<>())
                .build();
    }

    public static Autoridade umaAutoridade(TipoAutoridade tipo) {
        return new Autoridade(null, tipo.name(), new ArrayList<>());
    }

    public static Animal umAnimal(Usuario responsavel) {
        return Animal.builder()
                .id(1L)
                .nome("Rex")
                .especie(Especie.CANINA)
                .idade(3L)
                .genero(Genero.MASCULINO)
                .porte(PorteAnimal.MEDIO)
                .descricao("Cão dócil e brincalhão")
                .disponivel(true)
                .vacinado(true)
                .castrado(false)
                .microchipado(false)
                .responsavel(responsavel)
                .midias(new ArrayList<>())
                .build();
    }

    public static Animal umAnimalIndisponivel(Usuario responsavel) {
        var animal = umAnimal(responsavel);
        animal.setDisponivel(false);
        return animal;
    }

    public static PedidoAdocao umPedidoAdocao(Animal animal, Usuario adotante, Usuario voluntario) {
        return PedidoAdocao.builder()
                .id(1L)
                .dataPedido(LocalDate.now())
                .status(StatusAdocao.SOLICITADA)
                .observacoes("Tenho experiência com cães")
                .animal(animal)
                .adotante(adotante)
                .voluntarioResponsavel(voluntario)
                .build();
    }

    public static Doacao umaDoacao(Usuario doador) {
        return Doacao.builder()
                .id(1L)
                .valor(new BigDecimal("50.00"))
                .categoria(TipoDoacao.DINHEIRO)
                .descricao("Doação mensal")
                .data(LocalDateTime.now())
                .doador(doador)
                .build();
    }

    public static PerfilAdotante umPerfilAdotante(Usuario usuario) {
        return PerfilAdotante.builder()
                .id(1L)
                .usuario(usuario)
                .tipoMoradia(TipoMoradia.CASA_COM_QUINTAL)
                .areaM2(80)
                .temOutrosPets(false)
                .rotinaDiaria("Trabalho em casa")
                .horasEmCasaPorDia(10)
                .rendaMensalFaixa(FaixaRenda.DE_2_A_5SM)
                .experienciaAnimais(true)
                .motivacaoAdocao("Quero dar um lar a um animal")
                .especiePreferida(Especie.CANINA)
                .portePreferido(PorteAnimal.MEDIO)
                .aceitaCriancas(true)
                .build();
    }

    public static ItemEstoque umItemEstoque() {
        return ItemEstoque.builder()
                .id(1L)
                .nome("Ração Premium")
                .categoria(CategoriaEstoque.RACAO)
                .unidadeMedida(UnidadeMedida.KG)
                .quantidadeAtual(new BigDecimal("10.0"))
                .quantidadeMinima(new BigDecimal("2.0"))
                .ativo(true)
                .movimentacoes(new ArrayList<>())
                .build();
    }
}