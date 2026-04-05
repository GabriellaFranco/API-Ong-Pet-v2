package com.enterprise.ong_pet2.mapper;

import com.enterprise.ong_pet2.entity.PerfilAdotante;
import com.enterprise.ong_pet2.entity.Usuario;
import com.enterprise.ong_pet2.model.dto.perfil_adotante.PerfilAdotanteRequestDTO;
import com.enterprise.ong_pet2.model.dto.perfil_adotante.PerfilAdotanteResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class PerfilAdotanteMapper {

    public PerfilAdotante toPerfilAdotante(PerfilAdotanteRequestDTO dto, Usuario usuario) {
        return PerfilAdotante.builder()
                .usuario(usuario)
                .tipoMoradia(dto.tipoMoradia())
                .areaM2(dto.areaM2())
                .temOutrosPets(dto.temOutrosPets())
                .descricaoOutrosPets(dto.descricaoOutrosPets())
                .rotinaDiaria(dto.rotinaDiaria())
                .horasEmCasaPorDia(dto.horasEmCasaPorDia())
                .rendaMensalFaixa(dto.rendaMensalFaixa())
                .experienciaAnimais(dto.experienciaAnimais())
                .motivacaoAdocao(dto.motivacaoAdocao())
                .especiePreferida(dto.especiePreferida())
                .portePreferido(dto.portePreferido())
                .aceitaCriancas(dto.aceitaCriancas())
                .build();
    }

    public PerfilAdotanteResponseDTO toResponseDTO(PerfilAdotante perfil) {
        return new PerfilAdotanteResponseDTO(
                perfil.getId(),
                perfil.getTipoMoradia(),
                perfil.getAreaM2(),
                perfil.getTemOutrosPets(),
                perfil.getDescricaoOutrosPets(),
                perfil.getRotinaDiaria(),
                perfil.getHorasEmCasaPorDia(),
                perfil.getRendaMensalFaixa(),
                perfil.getExperienciaAnimais(),
                perfil.getMotivacaoAdocao(),
                perfil.getEspeciePreferida(),
                perfil.getPortePreferido(),
                perfil.getAceitaCriancas(),
                perfil.getScoreRisco()
        );
    }

    public void updateFromDTO(PerfilAdotanteRequestDTO dto, PerfilAdotante perfil) {
        perfil.setTipoMoradia(dto.tipoMoradia());
        perfil.setAreaM2(dto.areaM2());
        perfil.setTemOutrosPets(dto.temOutrosPets());
        perfil.setDescricaoOutrosPets(dto.descricaoOutrosPets());
        perfil.setRotinaDiaria(dto.rotinaDiaria());
        perfil.setHorasEmCasaPorDia(dto.horasEmCasaPorDia());
        perfil.setRendaMensalFaixa(dto.rendaMensalFaixa());
        perfil.setExperienciaAnimais(dto.experienciaAnimais());
        perfil.setMotivacaoAdocao(dto.motivacaoAdocao());
        perfil.setEspeciePreferida(dto.especiePreferida());
        perfil.setPortePreferido(dto.portePreferido());
        perfil.setAceitaCriancas(dto.aceitaCriancas());
    }
}
