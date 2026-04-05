package com.enterprise.ong_pet2.model.dto.auth;

import com.enterprise.ong_pet2.enums.PerfilUsuario;

import java.util.List;

public record AtualizarPerfilEAutoridadesDTO(
        PerfilUsuario novoPerfil,
        List<String> novasAutoridades
) {}
