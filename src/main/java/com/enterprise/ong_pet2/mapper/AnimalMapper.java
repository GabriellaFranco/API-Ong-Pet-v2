package com.enterprise.ong_pet2.mapper;

import com.enterprise.ong_pet2.entity.Animal;
import com.enterprise.ong_pet2.entity.Usuario;
import com.enterprise.ong_pet2.dto.animal.AnimalRequestDTO;
import com.enterprise.ong_pet2.dto.animal.AnimalResponseDTO;
import com.enterprise.ong_pet2.dto.animal.AnimalUpdateDTO;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AnimalMapper {

    public Animal toAnimal(AnimalRequestDTO dto, Usuario responsavel) {
        return Animal.builder()
                .nome(dto.nome())
                .especie(dto.especie())
                .idade(dto.idade())
                .genero(dto.genero())
                .porte(dto.porte())
                .descricao(dto.descricao())
                .vacinado(dto.vacinado() != null ? dto.vacinado() : false)
                .castrado(dto.castrado() != null ? dto.castrado() : false)
                .microchipado(dto.microchipado() != null ? dto.microchipado() : false)
                .responsavel(responsavel)
                .build();
    }

    public AnimalResponseDTO toResponseDTO(Animal animal) {
        return new AnimalResponseDTO(
                animal.getId(),
                animal.getNome(),
                animal.getEspecie(),
                animal.getIdade(),
                animal.getGenero(),
                animal.getPorte(),
                animal.getDescricao(),
                animal.getDisponivel(),
                animal.getVacinado(),
                animal.getCastrado(),
                animal.getMicrochipado(),
                new AnimalResponseDTO.ResponsavelDTO(
                        animal.getResponsavel().getId(),
                        animal.getResponsavel().getNome()
                )
        );
    }

    public void updateFromDTO(AnimalUpdateDTO dto, Animal animal) {
        Optional.ofNullable(dto.disponivel()).ifPresent(animal::setDisponivel);
        Optional.ofNullable(dto.vacinado()).ifPresent(animal::setVacinado);
        Optional.ofNullable(dto.castrado()).ifPresent(animal::setCastrado);
        Optional.ofNullable(dto.microchipado()).ifPresent(animal::setMicrochipado);
        Optional.ofNullable(dto.descricao()).ifPresent(animal::setDescricao);
    }
}