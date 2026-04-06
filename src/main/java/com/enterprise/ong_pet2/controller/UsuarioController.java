package com.enterprise.ong_pet2.controller;

import com.enterprise.ong_pet2.enums.PerfilUsuario;
import com.enterprise.ong_pet2.model.dto.usuario.UsuarioRequestDTO;
import com.enterprise.ong_pet2.model.dto.usuario.UsuarioResponseDTO;
import com.enterprise.ong_pet2.model.dto.usuario.UsuarioUpdateDTO;
import com.enterprise.ong_pet2.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequiredArgsConstructor
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UsuarioResponseDTO>> getAllUsuarios(
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        var usuarios = usuarioService.getAllUsuarios(pageable);
        return usuarios.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(usuarios);
    }

    @GetMapping("/results")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UsuarioResponseDTO>> getUsuariosByFilter(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String cpf,
            @RequestParam(required = false) String cidade,
            @RequestParam(required = false) PerfilUsuario perfil,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        var usuarios = usuarioService.getUsuariosByFilter(nome, cpf, cidade, perfil, pageable);
        return usuarios.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(usuarios);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioResponseDTO> getUsuarioById(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.getUsuarioById(id));
    }

    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> createUsuario(@Valid @RequestBody UsuarioRequestDTO dto) {
        var usuario = usuarioService.createUsuario(dto);
        var uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(usuario.id()).toUri();
        return ResponseEntity.created(uri).body(usuario);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> updateUsuario(@PathVariable Long id,
                                                            @Valid @RequestBody UsuarioUpdateDTO dto) {
        return ResponseEntity.ok(usuarioService.updateUsuario(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteUsuario(@PathVariable Long id) {
        usuarioService.deleteUsuario(id);
        return ResponseEntity.ok("Usuário excluído com sucesso: " + id);
    }
}