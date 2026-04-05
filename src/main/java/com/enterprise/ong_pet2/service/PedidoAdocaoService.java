package com.enterprise.ong_pet2.service;

import com.enterprise.ong_pet2.entity.Animal;
import com.enterprise.ong_pet2.entity.Usuario;
import com.enterprise.ong_pet2.enums.PerfilUsuario;
import com.enterprise.ong_pet2.enums.StatusAdocao;
import com.enterprise.ong_pet2.exception.BusinessException;
import com.enterprise.ong_pet2.exception.ResourceNotFoundException;
import com.enterprise.ong_pet2.mapper.PedidoAdocaoMapper;
import com.enterprise.ong_pet2.model.dto.pedido_adocao.PedidoAdocaoRequestDTO;
import com.enterprise.ong_pet2.model.dto.pedido_adocao.PedidoAdocaoResponseDTO;
import com.enterprise.ong_pet2.model.dto.pedido_adocao.PedidoAdocaoUpdateDTO;
import com.enterprise.ong_pet2.repository.AnimalRepository;
import com.enterprise.ong_pet2.repository.PedidoAdocaoRepository;
import com.enterprise.ong_pet2.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;

@Service
@RequiredArgsConstructor
public class PedidoAdocaoService {

    private final PedidoAdocaoRepository pedidoAdocaoRepository;
    private final AnimalRepository animalRepository;
    private final UsuarioRepository usuarioRepository;
    private final PedidoAdocaoMapper pedidoAdocaoMapper;
    private final UsuarioService usuarioService;

    @PreAuthorize("hasAnyRole('ADMIN', 'VOLUNTARIO')")
    public Page<PedidoAdocaoResponseDTO> getAllPedidos(Pageable pageable) {
        return pedidoAdocaoRepository.findAll(pageable)
                .map(pedidoAdocaoMapper::toResponseDTO);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'VOLUNTARIO')")
    public Page<PedidoAdocaoResponseDTO> getPedidosByFilter(StatusAdocao status, LocalDate dataPedido,
                                                            String adotante, String voluntario,
                                                            Pageable pageable) {
        return pedidoAdocaoRepository.findByFilter(status, dataPedido, adotante, voluntario, pageable)
                .map(pedidoAdocaoMapper::toResponseDTO);
    }

    public PedidoAdocaoResponseDTO getPedidoById(Long id) {
        return pedidoAdocaoRepository.findById(id)
                .map(pedidoAdocaoMapper::toResponseDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido de adoção não encontrado: " + id));
    }

    @Transactional
    @PreAuthorize("hasRole('PADRAO')")
    public PedidoAdocaoResponseDTO createPedido(PedidoAdocaoRequestDTO dto) {
        var adotante = usuarioService.getUsuarioLogado();
        validarLimitePedidosPendentes(adotante);

        var animal = animalRepository.findById(dto.idAnimal())
                .filter(Animal::getDisponivel)
                .orElseThrow(() -> new BusinessException("Animal não disponível para adoção"));

        validarDuplicidade(adotante, animal);

        var voluntario = distribuirParaVoluntarioComMenorCarga();

        var pedido = pedidoAdocaoMapper.toPedidoAdocao(dto, animal, adotante, voluntario);
        pedido.setDataPedido(LocalDate.now());
        pedido.setStatus(StatusAdocao.SOLICITADA);

        return pedidoAdocaoMapper.toResponseDTO(pedidoAdocaoRepository.save(pedido));
    }

    @Transactional
    @PreAuthorize("hasRole('VOLUNTARIO')")
    public PedidoAdocaoResponseDTO updateStatusPedido(Long id, PedidoAdocaoUpdateDTO dto) {
        var pedido = pedidoAdocaoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido de adoção não encontrado: " + id));
        pedidoAdocaoMapper.updateFromDTO(dto, pedido);
        return pedidoAdocaoMapper.toResponseDTO(pedidoAdocaoRepository.save(pedido));
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deletePedido(Long id) {
        var pedido = pedidoAdocaoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido de adoção não encontrado: " + id));
        if (!pedido.getStatus().equals(StatusAdocao.SOLICITADA)) {
            throw new BusinessException("Não é possível excluir um pedido já avaliado");
        }
        pedidoAdocaoRepository.delete(pedido);
    }

    private void validarLimitePedidosPendentes(Usuario adotante) {
        if (pedidoAdocaoRepository.countByAdotanteAndStatus(adotante, StatusAdocao.SOLICITADA) >= 3) {
            throw new BusinessException("Você já possui 3 pedidos pendentes. Aguarde a avaliação antes de criar novos");
        }
    }

    private void validarDuplicidade(Usuario adotante, Animal animal) {
        if (pedidoAdocaoRepository.existsByAdotanteAndAnimalAndStatus(adotante, animal, StatusAdocao.SOLICITADA)) {
            throw new BusinessException("Já existe um pedido pendente para este animal");
        }
    }

    private Usuario distribuirParaVoluntarioComMenorCarga() {
        var voluntarios = usuarioRepository.findByPerfil(PerfilUsuario.VOLUNTARIO);
        if (voluntarios.isEmpty()) {
            throw new BusinessException("Não há voluntários disponíveis para analisar o pedido");
        }
        return voluntarios.stream()
                .min(Comparator.comparingLong(v ->
                        pedidoAdocaoRepository.countByVoluntarioResponsavelAndStatus(v, StatusAdocao.SOLICITADA)))
                .orElseThrow();
    }
}