package com.enterprise.ong_pet2.mapper;

import com.enterprise.ong_pet2.entity.Animal;
import com.enterprise.ong_pet2.entity.Doacao;
import com.enterprise.ong_pet2.entity.Usuario;
import com.enterprise.ong_pet2.model.dto.doacao.DoacaoRequestDTO;
import com.enterprise.ong_pet2.model.dto.doacao.DoacaoResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class DoacaoMapper {

    public Doacao toDoacao(DoacaoRequestDTO dto, Usuario doador, Animal animal) {
        return Doacao.builder()
                .valor(dto.valor())
                .categoria(dto.categoria())
                .descricao(dto.descricao())
                .doador(doador)
                .animal(animal)
                .build();
    }

    public DoacaoResponseDTO toResponseDTO(Doacao doacao) {
        return new DoacaoResponseDTO(
                doacao.getId(),
                doacao.getValor(),
                doacao.getCategoria(),
                doacao.getDescricao(),
                doacao.getData(),
                new DoacaoResponseDTO.DoadorDTO(
                        doacao.getDoador().getId(),
                        doacao.getDoador().getNome()
                ),
                doacao.getAnimal() != null
                        ? new DoacaoResponseDTO.AnimalDTO(
                        doacao.getAnimal().getId(),
                        doacao.getAnimal().getNome())
                        : null
        );
    }
}
