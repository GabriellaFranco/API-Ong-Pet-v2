package com.enterprise.ong_pet2.service;

import com.enterprise.ong_pet2.entity.Animal;
import com.enterprise.ong_pet2.enums.TipoDoacao;
import com.enterprise.ong_pet2.exception.BusinessException;
import com.enterprise.ong_pet2.exception.ResourceNotFoundException;
import com.enterprise.ong_pet2.mapper.DoacaoMapper;
import com.enterprise.ong_pet2.messaging.publisher.EventPublisher;
import com.enterprise.ong_pet2.model.dto.doacao.DoacaoRequestDTO;
import com.enterprise.ong_pet2.model.dto.doacao.DoacaoResponseDTO;
import com.enterprise.ong_pet2.model.event.DoacaoCriadaEvent;
import com.enterprise.ong_pet2.repository.AnimalRepository;
import com.enterprise.ong_pet2.repository.DoacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DoacaoService {

    private final DoacaoRepository doacaoRepository;
    private final AnimalRepository animalRepository;
    private final DoacaoMapper doacaoMapper;
    private final UsuarioService usuarioService;
    private final EventPublisher eventPublisher;

    private static final BigDecimal VALOR_MINIMO = new BigDecimal("1.00");

    @PreAuthorize("hasRole('ADMIN')")
    public Page<DoacaoResponseDTO> getAllDoacoes(Pageable pageable) {
        return doacaoRepository.findAll(pageable)
                .map(doacaoMapper::toResponseDTO);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Page<DoacaoResponseDTO> getDoacoesByFilter(String doador, TipoDoacao categoria,
                                                      LocalDateTime dataInicio, LocalDateTime dataFim,
                                                      Pageable pageable) {
        return doacaoRepository.findByFilter(doador, categoria, dataInicio, dataFim, pageable)
                .map(doacaoMapper::toResponseDTO);
    }

    public DoacaoResponseDTO getDoacaoById(Long id) {
        return doacaoRepository.findById(id)
                .map(doacaoMapper::toResponseDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Doação não encontrada: " + id));
    }

    @Transactional
    public DoacaoResponseDTO createDoacao(DoacaoRequestDTO dto) {
        var doador = usuarioService.getUsuarioLogado();

        if (dto.valor() == null || dto.valor().compareTo(VALOR_MINIMO) < 0) {
            throw new BusinessException("O valor mínimo da doação é R$1,00");
        }

        validarIntervaloMinimoEntreDoacoes(doador);

        Animal animal = null;
        if (dto.idAnimal() != null) {
            animal = animalRepository.findById(dto.idAnimal())
                    .orElseThrow(() -> new ResourceNotFoundException("Animal não encontrado: " + dto.idAnimal()));
        }

        var doacao = doacaoMapper.toDoacao(dto, doador, animal);
        doacao.setData(LocalDateTime.now());

        var salva = doacaoRepository.save(doacao);

        eventPublisher.publish(
                new DoacaoCriadaEvent(
                        salva.getId(),
                        doador.getId(),
                        salva.getValor(),
                        salva.getCategoria(),
                        salva.getData()
                ),
                "doacao.criada"
        );

        return doacaoMapper.toResponseDTO(salva);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'PADRAO')")
    public void deleteDoacao(Long id) {
        var doacao = doacaoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doação não encontrada: " + id));

        var usuarioLogado = usuarioService.getUsuarioLogado();
        boolean isAdmin = usuarioLogado.getPerfil().name().equals("ADMIN");
        boolean isProprioDoador = doacao.getDoador().getId().equals(usuarioLogado.getId());

        if (!isAdmin && !isProprioDoador) {
            throw new BusinessException("Você não tem permissão para excluir esta doação");
        }

        doacaoRepository.delete(doacao);
    }

    private void validarIntervaloMinimoEntreDoacoes(com.enterprise.ong_pet2.entity.Usuario doador) {
        var agora = LocalDateTime.now();
        if (doacaoRepository.existsByDoadorAndDataBetween(doador, agora.minusMinutes(1), agora)) {
            throw new BusinessException("Aguarde 1 minuto antes de registrar outra doação");
        }
    }
}