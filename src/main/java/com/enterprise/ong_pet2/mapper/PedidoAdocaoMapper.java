package com.enterprise.ong_pet2.mapper;

import com.enterprise.ong_pet2.entity.Animal;
import com.enterprise.ong_pet2.entity.PedidoAdocao;
import com.enterprise.ong_pet2.entity.Usuario;
import com.enterprise.ong_pet2.model.dto.pedido_adocao.PedidoAdocaoRequestDTO;
import com.enterprise.ong_pet2.model.dto.pedido_adocao.PedidoAdocaoResponseDTO;
import com.enterprise.ong_pet2.model.dto.pedido_adocao.PedidoAdocaoUpdateDTO;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class PedidoAdocaoMapper {

    public PedidoAdocao toPedidoAdocao(PedidoAdocaoRequestDTO dto,
                                       Animal animal,
                                       Usuario adotante,
                                       Usuario voluntario) {
        return PedidoAdocao.builder()
                .observacoes(dto.observacoes())
                .animal(animal)
                .adotante(adotante)
                .voluntarioResponsavel(voluntario)
                .build();
    }

    public PedidoAdocaoResponseDTO toResponseDTO(PedidoAdocao pedido) {
        return new PedidoAdocaoResponseDTO(
                pedido.getId(),
                pedido.getDataPedido(),
                pedido.getStatus(),
                pedido.getObservacoes(),
                pedido.getScoreMatching(),
                new PedidoAdocaoResponseDTO.UsuarioDTO(
                        pedido.getAdotante().getId(),
                        pedido.getAdotante().getNome(),
                        pedido.getAdotante().getPerfil()
                ),
                new PedidoAdocaoResponseDTO.UsuarioDTO(
                        pedido.getVoluntarioResponsavel().getId(),
                        pedido.getVoluntarioResponsavel().getNome(),
                        pedido.getVoluntarioResponsavel().getPerfil()
                ),
                new PedidoAdocaoResponseDTO.AnimalDTO(
                        pedido.getAnimal().getId(),
                        pedido.getAnimal().getNome()
                )
        );
    }

    public void updateFromDTO(PedidoAdocaoUpdateDTO dto, PedidoAdocao pedido) {
        Optional.ofNullable(dto.statusAdocao()).ifPresent(pedido::setStatus);
        Optional.ofNullable(dto.observacoes()).ifPresent(pedido::setObservacoes);
    }
}
