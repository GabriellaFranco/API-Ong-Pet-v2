package com.enterprise.ong_pet2.mapper;

import com.enterprise.ong_pet2.entity.Animal;
import com.enterprise.ong_pet2.entity.Despesa;
import com.enterprise.ong_pet2.entity.Usuario;
import com.enterprise.ong_pet2.model.dto.despesa.DespesaRequestDTO;
import com.enterprise.ong_pet2.model.dto.despesa.DespesaResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class DespesaMapper {

    public Despesa toDespesa(DespesaRequestDTO dto, Usuario responsavel, Animal animal) {
        return Despesa.builder()
                .descricao(dto.descricao())
                .valor(dto.valor())
                .data(dto.data())
                .categoria(dto.categoria())
                .observacoes(dto.observacoes())
                .animal(animal)
                .responsavel(responsavel)
                .build();
    }

    public DespesaResponseDTO toResponseDTO(Despesa despesa) {
        return new DespesaResponseDTO(
                despesa.getId(),
                despesa.getDescricao(),
                despesa.getValor(),
                despesa.getData(),
                despesa.getCategoria(),
                despesa.getObservacoes(),
                despesa.getAnimal() != null
                        ? new DespesaResponseDTO.AnimalDTO(
                        despesa.getAnimal().getId(),
                        despesa.getAnimal().getNome())
                        : null,
                new DespesaResponseDTO.ResponsavelDTO(
                        despesa.getResponsavel().getId(),
                        despesa.getResponsavel().getNome()
                )
        );
    }
}
