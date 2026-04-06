package com.enterprise.ong_pet2.model.dto.midia;

import com.enterprise.ong_pet2.enums.TipoMidia;

public record AnimalMidiaResponseDTO(
        Long id,
        String url,
        TipoMidia tipo,
        Boolean principal,
        Integer ordem,
        String nomeOriginal,
        Long tamanhoBytes,
        String mimeType
) {}