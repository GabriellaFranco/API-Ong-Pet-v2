package com.enterprise.ong_pet2.service;

import com.enterprise.ong_pet2.entity.Animal;
import com.enterprise.ong_pet2.entity.Usuario;
import com.enterprise.ong_pet2.enums.Especie;
import com.enterprise.ong_pet2.enums.Genero;
import com.enterprise.ong_pet2.enums.PorteAnimal;
import com.enterprise.ong_pet2.exception.BusinessException;
import com.enterprise.ong_pet2.exception.ResourceNotFoundException;
import com.enterprise.ong_pet2.mapper.AnimalMapper;
import com.enterprise.ong_pet2.messaging.publisher.EventPublisher;
import com.enterprise.ong_pet2.model.dto.animal.AnimalRequestDTO;
import com.enterprise.ong_pet2.model.dto.animal.AnimalResponseDTO;
import com.enterprise.ong_pet2.model.dto.animal.AnimalUpdateDTO;
import com.enterprise.ong_pet2.model.event.AnimalCadastradoEvent;
import com.enterprise.ong_pet2.repository.AnimalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AnimalService {

    private final AnimalRepository animalRepository;
    private final AnimalMapper animalMapper;
    private final UsuarioService usuarioService;
    private final EventPublisher eventPublisher;

    public Page<AnimalResponseDTO> getAllAnimais(Pageable pageable) {
        return animalRepository.findAll(pageable)
                .map(animalMapper::toResponseDTO);
    }

    public Page<AnimalResponseDTO> getAnimalsByFilter(String nome, Especie especie,
                                                      Genero genero, PorteAnimal porte,
                                                      Boolean disponivel, Pageable pageable) {
        return animalRepository.findByFilter(nome, especie, genero, porte, disponivel, pageable)
                .map(animalMapper::toResponseDTO);
    }

    public AnimalResponseDTO getAnimalById(Long id) {
        return animalRepository.findById(id)
                .map(animalMapper::toResponseDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Animal não encontrado: " + id));
    }

    @Transactional
    public AnimalResponseDTO createAnimal(AnimalRequestDTO dto) {
        var usuarioLogado = usuarioService.getUsuarioLogado();
        validarRegistroUnico(dto, usuarioLogado);
        var animal = animalMapper.toAnimal(dto, usuarioLogado);

        var salvo = animalRepository.save(animal);

        eventPublisher.publish(
                new AnimalCadastradoEvent(
                        salvo.getId(),
                        usuarioLogado.getId(),
                        salvo.getNome(),
                        salvo.getEspecie(),
                        salvo.getPorte(),
                        LocalDateTime.now()
                ),
                "animal.cadastrado"
        );

        return animalMapper.toResponseDTO(salvo);
    }

    @Transactional
    @PreAuthorize("hasRole('VOLUNTARIO')")
    public AnimalResponseDTO updateAnimal(Long id, AnimalUpdateDTO dto) {
        var animal = animalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Animal não encontrado: " + id));
        validarAnimalNaoAdotado(animal);
        animalMapper.updateFromDTO(dto, animal);
        return animalMapper.toResponseDTO(animalRepository.save(animal));
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteAnimal(Long id) {
        var animal = animalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Animal não encontrado: " + id));
        validarAnimalNaoAdotado(animal);
        animalRepository.delete(animal);
    }

    public Animal getAnimalEntityById(Long id) {
        return animalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Animal não encontrado: " + id));
    }

    private void validarRegistroUnico(AnimalRequestDTO dto, Usuario responsavel) {
        if (animalRepository.existsByNomeAndEspecieAndResponsavel(dto.nome(), dto.especie(), responsavel)) {
            throw new BusinessException("Já existe um animal com esse nome e espécie para este usuário");
        }
    }

    private void validarAnimalNaoAdotado(Animal animal) {
        if (!animal.getDisponivel()) {
            throw new BusinessException("Não é possível editar um animal que já foi adotado");
        }
    }
}