package com.enterprise.ong_pet2.service;

import com.enterprise.ong_pet2.entity.Animal;
import com.enterprise.ong_pet2.entity.PerfilAdotante;
import com.enterprise.ong_pet2.entity.Usuario;
import com.enterprise.ong_pet2.enums.Especie;
import com.enterprise.ong_pet2.enums.FaixaRenda;
import com.enterprise.ong_pet2.enums.PorteAnimal;
import com.enterprise.ong_pet2.enums.TipoMoradia;
import com.enterprise.ong_pet2.exception.BusinessException;
import com.enterprise.ong_pet2.exception.ResourceNotFoundException;
import com.enterprise.ong_pet2.model.dto.matching.FatorScoreDTO;
import com.enterprise.ong_pet2.model.dto.matching.MatchingResponseDTO;
import com.enterprise.ong_pet2.model.dto.matching.MatchingResponseDTO.ClassificacaoMatching;
import com.enterprise.ong_pet2.repository.AnimalRepository;
import com.enterprise.ong_pet2.repository.PerfilAdotanteRepository;
import com.enterprise.ong_pet2.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MatchingService {

    private final AnimalRepository animalRepository;
    private final PerfilAdotanteRepository perfilAdotanteRepository;
    private final UsuarioService usuarioService;

    @PreAuthorize("hasAnyRole('ADMIN', 'VOLUNTARIO')")
    public Page<MatchingResponseDTO> getAdotantesParaAnimal(Long animalId, Pageable pageable) {
        var animal = animalRepository.findById(animalId)
                .orElseThrow(() -> new ResourceNotFoundException("Animal não encontrado: " + animalId));

        if (!animal.getDisponivel()) {
            throw new BusinessException("Animal não está disponível para adoção");
        }

        var resultados = perfilAdotanteRepository.findAll().stream()
                .map(perfil -> calcularMatching(animal, perfil))
                .sorted(Comparator.comparingInt(MatchingResponseDTO::scoreTotal).reversed())
                .toList();

        return paginar(resultados, pageable);
    }

    public Page<MatchingResponseDTO> getAnimaisSugeridosParaAdotante(Pageable pageable) {
        var usuario = usuarioService.getUsuarioLogado();
        var perfil = perfilAdotanteRepository.findByUsuario(usuario)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Perfil de adotante não encontrado. Acesse /usuarios/me/perfil-adotante"));

        var resultados = animalRepository.findAll().stream()
                .filter(Animal::getDisponivel)
                .map(animal -> calcularMatching(animal, perfil))
                .sorted(Comparator.comparingInt(MatchingResponseDTO::scoreTotal).reversed())
                .toList();

        return paginar(resultados, pageable);
    }

    private MatchingResponseDTO calcularMatching(Animal animal, PerfilAdotante perfil) {
        List<FatorScoreDTO> fatores = new ArrayList<>();

        boolean ehCanino = animal.getEspecie() == Especie.CANINA;
        boolean ehFelino = animal.getEspecie() == Especie.FELINA;
        boolean porteGrande = animal.getPorte() == PorteAnimal.GRANDE;
        boolean portePequeno = animal.getPorte() == PorteAnimal.PEQUENO;

        fatores.add(calcularFatorEspecie(animal, perfil));
        fatores.add(calcularFatorPorte(animal, perfil, ehFelino));
        fatores.add(calcularFatorMoradia(perfil, ehCanino, ehFelino, porteGrande));
        fatores.add(calcularFatorHorasEmCasa(perfil, ehFelino));
        fatores.add(calcularFatorExperiencia(perfil, ehFelino));
        fatores.add(calcularFatorOutrosPets(perfil, ehFelino, ehCanino));
        fatores.add(calcularFatorRenda(perfil, porteGrande, portePequeno));

        int scoreTotal = fatores.stream().mapToInt(FatorScoreDTO::pontos).sum();

        ClassificacaoMatching classificacao;
        if (scoreTotal >= 70)      classificacao = ClassificacaoMatching.ALTA;
        else if (scoreTotal >= 50) classificacao = ClassificacaoMatching.MEDIA;
        else                       classificacao = ClassificacaoMatching.BAIXA;

        Usuario adotante = perfil.getUsuario();

        return new MatchingResponseDTO(
                animal.getId(),
                animal.getNome(),
                animal.getEspecie(),
                animal.getPorte(),
                adotante.getId(),
                adotante.getNome(),
                scoreTotal,
                classificacao,
                fatores
        );
    }

    // Fator 1 — Espécie preferida (25 pts)
    private FatorScoreDTO calcularFatorEspecie(Animal animal, PerfilAdotante perfil) {
        if (perfil.getEspeciePreferida() == null) {
            return new FatorScoreDTO("Espécie preferida", 15, 25,
                    "Sem preferência de espécie declarada");
        }
        if (perfil.getEspeciePreferida() == animal.getEspecie()) {
            return new FatorScoreDTO("Espécie preferida", 25, 25,
                    "Espécie preferida corresponde exatamente");
        }
        return new FatorScoreDTO("Espécie preferida", 0, 25,
                "Espécie diferente da preferida pelo adotante");
    }

    // Fator 2 — Porte preferido (20 pts)
    // Para felinos, porte tem menos impacto.
    // Para cães, match exato é mais relevante.
    private FatorScoreDTO calcularFatorPorte(Animal animal, PerfilAdotante perfil, boolean ehFelino) {
        if (ehFelino) {
            // Porte tem pouca relevância para felinos
            if (perfil.getPortePreferido() == null || perfil.getPortePreferido() == animal.getPorte()) {
                return new FatorScoreDTO("Porte preferido", 20, 20,
                        "Felinos adaptam-se bem independentemente do porte");
            }
            return new FatorScoreDTO("Porte preferido", 15, 20,
                    "Porte diferente do preferido, mas felinos se adaptam bem");
        }

        if (perfil.getPortePreferido() == null) {
            return new FatorScoreDTO("Porte preferido", 10, 20,
                    "Sem preferência de porte declarada");
        }
        if (perfil.getPortePreferido() == animal.getPorte()) {
            return new FatorScoreDTO("Porte preferido", 20, 20,
                    "Porte preferido corresponde exatamente");
        }
        return new FatorScoreDTO("Porte preferido", 0, 20,
                "Porte diferente do preferido pelo adotante");
    }

    // Fator 3 — Tipo de moradia (20 pts)
    // Felinos adaptam-se bem a qualquer moradia
    // Cães grandes precisam de espaço, apartamento é inadequado
    // Cães pequenos/médios casa sem quintal e apartamento são aceitáveis
    private FatorScoreDTO calcularFatorMoradia(PerfilAdotante perfil,
                                               boolean ehCanino, boolean ehFelino,
                                               boolean porteGrande) {
        TipoMoradia moradia = perfil.getTipoMoradia();

        if (moradia == TipoMoradia.CASA_COM_QUINTAL) {
            return new FatorScoreDTO("Tipo de moradia", 20, 20,
                    "Casa com quintal — ideal para qualquer espécie e porte");
        }

        if (ehFelino) {
            return switch (moradia) {
                case CASA_SEM_QUINTAL -> new FatorScoreDTO("Tipo de moradia", 20, 20,
                        "Casa sem quintal — adequado para felinos");
                case APARTAMENTO -> new FatorScoreDTO("Tipo de moradia", 18, 20,
                        "Apartamento — felinos adaptam-se muito bem a ambientes fechados");
                default -> new FatorScoreDTO("Tipo de moradia", 15, 20, "Moradia adequada para felinos");
            };
        }

        if (ehCanino && porteGrande) {
            return switch (moradia) {
                case CASA_SEM_QUINTAL -> new FatorScoreDTO("Tipo de moradia", 8, 20,
                        "Casa sem quintal — espaço limitado para cão de grande porte");
                case APARTAMENTO -> new FatorScoreDTO("Tipo de moradia", 0, 20,
                        "Apartamento — inadequado para cão de grande porte");
                default -> new FatorScoreDTO("Tipo de moradia", 5, 20, "Moradia com restrições para este porte");
            };
        }

        return switch (moradia) {
            case CASA_SEM_QUINTAL -> new FatorScoreDTO("Tipo de moradia", 15, 20,
                    "Casa sem quintal — adequado para este porte");
            case APARTAMENTO -> new FatorScoreDTO("Tipo de moradia", 8, 20,
                    "Apartamento — aceitável para cão de pequeno ou médio porte");
            default -> new FatorScoreDTO("Tipo de moradia", 10, 20, "Moradia aceitável");
        };
    }

    // Fator 4 — Horas em casa por dia (15 pts)
    //   Felinos são mais independentes, penalização menor para quem fica menos tempo em casa.
    //   Cães são mais dependentes de companhia e rotina.
    private FatorScoreDTO calcularFatorHorasEmCasa(PerfilAdotante perfil, boolean ehFelino) {
        int horas = perfil.getHorasEmCasaPorDia();

        if (ehFelino) {
            if (horas >= 6) return new FatorScoreDTO("Horas em casa por dia", 15, 15,
                    "Passa bastante tempo em casa — ótimo para felinos");
            if (horas >= 3) return new FatorScoreDTO("Horas em casa por dia", 12, 15,
                    "Felinos toleram bem períodos sozinhos");
            return new FatorScoreDTO("Horas em casa por dia", 7, 15,
                    "Passa pouco tempo em casa, mas felinos são independentes");
        }

        if (horas >= 8) return new FatorScoreDTO("Horas em casa por dia", 15, 15,
                "Passa muito tempo em casa — excelente para cães");
        if (horas >= 4) return new FatorScoreDTO("Horas em casa por dia", 8, 15,
                "Tempo razoável em casa para cuidar do animal");
        return new FatorScoreDTO("Horas em casa por dia", 0, 15,
                "Passa pouco tempo em casa — risco para o bem-estar do cão");
    }

    // Fator 5 — Experiência com animais (10 pts)
    // Felinos são mais independentes, então a falta de experiência pesa um pouco menos do que para cães.
    private FatorScoreDTO calcularFatorExperiencia(PerfilAdotante perfil, boolean ehFelino) {
        boolean temExp = Boolean.TRUE.equals(perfil.getExperienciaAnimais());

        if (temExp) {
            return new FatorScoreDTO("Experiência com animais", 10, 10,
                    "Já teve animais de estimação antes");
        }
        if (ehFelino) {
            return new FatorScoreDTO("Experiência com animais", 7, 10,
                    "Primeiro pet — felinos são mais independentes e adaptáveis");
        }
        return new FatorScoreDTO("Experiência com animais", 5, 10,
                "Primeiro pet — atenção redobrada no processo de adaptação");
    }

    // Fator 6 — Outros pets (5 pts)
    //   Felinos: outros gatos podem disputar território (pontua menos)
    //   Cães: outros cães podem indicar socialização ou conflito
    //   Sem outros pets: sempre positivo
    private FatorScoreDTO calcularFatorOutrosPets(PerfilAdotante perfil,
                                                  boolean ehFelino, boolean ehCanino) {
        boolean temOutrosPets = Boolean.TRUE.equals(perfil.getTemOutrosPets());

        if (!temOutrosPets) {
            return new FatorScoreDTO("Outros pets", 5, 5,
                    "Sem outros pets — sem risco de conflito territorial");
        }

        if (ehFelino) {
            return new FatorScoreDTO("Outros pets", 2, 5,
                    "Possui outros pets — gatos podem ter conflito de território");
        }

        if (ehCanino) {
            return new FatorScoreDTO("Outros pets", 3, 5,
                    "Possui outros pets — verificar socialização do cão");
        }

        return new FatorScoreDTO("Outros pets", 3, 5,
                "Possui outros pets — avaliar compatibilidade");
    }

    // Fator 7 — Faixa de renda (5 pts)
    // Animais de grande porte têm custos maiores, então renda pesa um pouco mais para eles.
    private FatorScoreDTO calcularFatorRenda(PerfilAdotante perfil,
                                             boolean porteGrande, boolean portePequeno) {
        FaixaRenda renda = perfil.getRendaMensalFaixa();

        if (renda == FaixaRenda.ACIMA_5SM) {
            return new FatorScoreDTO("Faixa de renda", 5, 5,
                    "Renda acima de 5 salários mínimos — excelente capacidade financeira");
        }

        if (renda == FaixaRenda.DE_2_A_5SM) {
            return new FatorScoreDTO("Faixa de renda", porteGrande ? 2 : 3, 5,
                    porteGrande
                            ? "Renda moderada — animais grandes têm custos maiores"
                            : "Renda adequada para manter o animal");
        }

        // ATE_2SM
        if (portePequeno) {
            return new FatorScoreDTO("Faixa de renda", 2, 5,
                    "Renda baixa — animais pequenos têm custo mais acessível");
        }
        return new FatorScoreDTO("Faixa de renda", 1, 5,
                "Renda baixa — atenção aos custos de manutenção do animal");
    }

    private <T> Page<T> paginar(List<T> lista, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), lista.size());
        if (start > lista.size()) return new PageImpl<>(List.of(), pageable, lista.size());
        return new PageImpl<>(lista.subList(start, end), pageable, lista.size());
    }
}