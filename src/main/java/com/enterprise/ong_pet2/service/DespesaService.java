package com.enterprise.ong_pet2.service;

import com.enterprise.ong_pet2.enums.CategoriaEstoque;
import com.enterprise.ong_pet2.exception.ResourceNotFoundException;
import com.enterprise.ong_pet2.mapper.DespesaMapper;
import com.enterprise.ong_pet2.model.dto.despesa.DespesaRequestDTO;
import com.enterprise.ong_pet2.model.dto.despesa.DespesaResponseDTO;
import com.enterprise.ong_pet2.repository.AnimalRepository;
import com.enterprise.ong_pet2.repository.DespesaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class DespesaService {

    private final DespesaRepository despesaRepository;
    private final AnimalRepository animalRepository;
    private final DespesaMapper despesaMapper;
    private final UsuarioService usuarioService;

    @PreAuthorize("hasAnyRole('ADMIN', 'VOLUNTARIO')")
    public Page<DespesaResponseDTO> getDespesasByFilter(CategoriaEstoque categoria,
                                                        LocalDate dataInicio, LocalDate dataFim,
                                                        Long idAnimal, Pageable pageable) {
        return despesaRepository.findByFilter(categoria, dataInicio, dataFim, idAnimal, pageable)
                .map(despesaMapper::toResponseDTO);
    }

    public DespesaResponseDTO getDespesaById(Long id) {
        return despesaRepository.findById(id)
                .map(despesaMapper::toResponseDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Despesa não encontrada: " + id));
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'VOLUNTARIO')")
    public DespesaResponseDTO createDespesa(DespesaRequestDTO dto) {
        var responsavel = usuarioService.getUsuarioLogado();

        var animal = dto.idAnimal() != null
                ? animalRepository.findById(dto.idAnimal())
                .orElseThrow(() -> new ResourceNotFoundException("Animal não encontrado: " + dto.idAnimal()))
                : null;

        var despesa = despesaMapper.toDespesa(dto, responsavel, animal);
        return despesaMapper.toResponseDTO(despesaRepository.save(despesa));
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteDespesa(Long id) {
        var despesa = despesaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Despesa não encontrada: " + id));
        despesaRepository.delete(despesa);
    }
}