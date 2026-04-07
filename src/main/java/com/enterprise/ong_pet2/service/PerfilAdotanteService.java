package com.enterprise.ong_pet2.service;

import com.enterprise.ong_pet2.entity.PerfilAdotante;
import com.enterprise.ong_pet2.enums.FaixaRenda;
import com.enterprise.ong_pet2.enums.TipoMoradia;
import com.enterprise.ong_pet2.exception.ResourceNotFoundException;
import com.enterprise.ong_pet2.mapper.PerfilAdotanteMapper;
import com.enterprise.ong_pet2.model.dto.perfil_adotante.PerfilAdotanteRequestDTO;
import com.enterprise.ong_pet2.model.dto.perfil_adotante.PerfilAdotanteResponseDTO;
import com.enterprise.ong_pet2.repository.PerfilAdotanteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PerfilAdotanteService {

    private final PerfilAdotanteRepository perfilAdotanteRepository;
    private final PerfilAdotanteMapper perfilAdotanteMapper;
    private final UsuarioService usuarioService;

    public PerfilAdotanteResponseDTO getPerfilDoUsuarioLogado() {
        var usuario = usuarioService.getUsuarioLogado();
        var perfil = perfilAdotanteRepository.findByUsuario(usuario)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil de adotante não encontrado"));
        return perfilAdotanteMapper.toResponseDTO(perfil);
    }

    @Transactional
    public PerfilAdotanteResponseDTO salvarPerfil(PerfilAdotanteRequestDTO dto) {
        var usuario = usuarioService.getUsuarioLogado();

        PerfilAdotante perfil = perfilAdotanteRepository.findByUsuario(usuario)
                .orElse(null);

        if (perfil == null) {
            perfil = perfilAdotanteMapper.toPerfilAdotante(dto, usuario);
        } else {
            perfilAdotanteMapper.updateFromDTO(dto, perfil);
        }

        perfil.setScoreRisco(calcularScoreRisco(perfil));
        return perfilAdotanteMapper.toResponseDTO(perfilAdotanteRepository.save(perfil));
    }

    private int calcularScoreRisco(PerfilAdotante perfil) {
        int score = 0;

        // Tipo de moradia (25 pts)
        if (perfil.getTipoMoradia() == TipoMoradia.CASA_COM_QUINTAL) score += 25;
        else if (perfil.getTipoMoradia() == TipoMoradia.CASA_SEM_QUINTAL) score += 15;
        else score += 8; // APARTAMENTO

        // Horas em casa por dia (20 pts)
        if (perfil.getHorasEmCasaPorDia() >= 8) score += 20;
        else if (perfil.getHorasEmCasaPorDia() >= 4) score += 12;
        else score += 0;

        // Experiência com animais (20 pts)
        if (Boolean.TRUE.equals(perfil.getExperienciaAnimais())) score += 20;
        else score += 8;

        // Outros pets (15 pts)
        if (Boolean.FALSE.equals(perfil.getTemOutrosPets())) score += 15;
        else score += 6;

        // Faixa de renda (10 pts)
        if (perfil.getRendaMensalFaixa() == FaixaRenda.ACIMA_5SM) score += 10;
        else if (perfil.getRendaMensalFaixa() == FaixaRenda.DE_2_A_5SM) score += 6;
        else score += 2;

        // Área da residência (10 pts)
        if (perfil.getAreaM2() != null && perfil.getAreaM2() >= 60) score += 10;
        else if (perfil.getAreaM2() != null && perfil.getAreaM2() >= 30) score += 5;
        else score += 2;

        return Math.min(score, 100);
    }
}