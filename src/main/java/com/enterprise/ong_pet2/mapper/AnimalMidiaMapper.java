package com.enterprise.ong_pet2.mapper;

import com.enterprise.ong_pet2.entity.AnimalMidia;
import com.enterprise.ong_pet2.model.dto.midia.AnimalMidiaResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class AnimalMidiaMapper {

    public AnimalMidiaResponseDTO toResponseDTO(AnimalMidia midia) {
        return new AnimalMidiaResponseDTO(
                midia.getId(),
                midia.getUrl(),
                midia.getTipo(),
                midia.getPrincipal(),
                midia.getOrdem(),
                midia.getNomeOriginal(),
                midia.getTamanhoBytes(),
                midia.getMimeType()
        );
    }
}