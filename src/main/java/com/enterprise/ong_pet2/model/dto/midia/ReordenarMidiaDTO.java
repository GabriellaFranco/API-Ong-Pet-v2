package com.enterprise.ong_pet2.model.dto.midia;

import jakarta.validation.constraints.NotNull;

public record ReordenarMidiaDTO(
        @NotNull Long midiaId,
        @NotNull Integer novaOrdem
) {}