package com.enterprise.ong_pet2.service;

import com.enterprise.ong_pet2.exception.BusinessException;
import com.enterprise.ong_pet2.exception.ResourceNotFoundException;
import com.enterprise.ong_pet2.mapper.AnimalMapper;
import com.enterprise.ong_pet2.messaging.publisher.EventPublisher;
import com.enterprise.ong_pet2.model.dto.animal.AnimalRequestDTO;
import com.enterprise.ong_pet2.model.dto.animal.AnimalUpdateDTO;
import com.enterprise.ong_pet2.repository.AnimalRepository;
import com.enterprise.ong_pet2.util.TestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnimalServiceTest {

    @InjectMocks
    private AnimalService animalService;

    @Mock private AnimalRepository animalRepository;
    @Mock private AnimalMapper animalMapper;
    @Mock private UsuarioService usuarioService;
    @Mock private EventPublisher eventPublisher;

    @Test
    @DisplayName("Deve criar animal com sucesso")
    void deveCriarAnimalComSucesso() {
        var usuario = TestDataFactory.umUsuarioPadrao();
        var animal = TestDataFactory.umAnimal(usuario);
        var dto = new AnimalRequestDTO(
                "Rex", animal.getEspecie(), animal.getIdade(),
                animal.getGenero(), animal.getPorte(),
                animal.getDescricao(), true, false, false
        );

        when(usuarioService.getUsuarioLogado()).thenReturn(usuario);
        when(animalRepository.existsByNomeAndEspecieAndResponsavel(any(), any(), any()))
                .thenReturn(false);
        when(animalMapper.toAnimal(dto, usuario)).thenReturn(animal);
        when(animalRepository.save(any())).thenReturn(animal);
        when(animalMapper.toResponseDTO(animal)).thenReturn(null);

        assertThatNoException().isThrownBy(() -> animalService.createAnimal(dto));
        verify(animalRepository).save(any());
        verify(eventPublisher).publish(any(), eq("animal.cadastrado"));
    }

    @Test
    @DisplayName("Deve lançar exceção ao cadastrar animal duplicado")
    void deveLancarExcecaoAnimalDuplicado() {
        var usuario = TestDataFactory.umUsuarioPadrao();
        var animal = TestDataFactory.umAnimal(usuario);
        var dto = new AnimalRequestDTO(
                "Rex", animal.getEspecie(), animal.getIdade(),
                animal.getGenero(), animal.getPorte(),
                animal.getDescricao(), true, false, false
        );

        when(usuarioService.getUsuarioLogado()).thenReturn(usuario);
        when(animalRepository.existsByNomeAndEspecieAndResponsavel(any(), any(), any()))
                .thenReturn(true);

        assertThatThrownBy(() -> animalService.createAnimal(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Já existe um animal");

        verify(animalRepository, never()).save(any());
        verify(eventPublisher, never()).publish(any(), any());
    }

    @Test
    @DisplayName("Deve lançar exceção ao editar animal já adotado")
    void deveLancarExcecaoAnimalAdotado() {
        var usuario = TestDataFactory.umUsuarioPadrao();
        var animal = TestDataFactory.umAnimalIndisponivel(usuario);
        var dto = new AnimalUpdateDTO(true, false, false, false, null);

        when(animalRepository.findById(1L)).thenReturn(Optional.of(animal));

        assertThatThrownBy(() -> animalService.updateAnimal(1L, dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("já foi adotado");
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar animal inexistente")
    void deveLancarExcecaoAnimalNaoEncontrado() {
        when(animalRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> animalService.getAnimalById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }
}