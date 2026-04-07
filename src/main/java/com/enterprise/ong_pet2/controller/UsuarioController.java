package com.enterprise.ong_pet2.controller;

import com.enterprise.ong_pet2.enums.PerfilUsuario;
import com.enterprise.ong_pet2.model.dto.usuario.UsuarioRequestDTO;
import com.enterprise.ong_pet2.model.dto.usuario.UsuarioResponseDTO;
import com.enterprise.ong_pet2.model.dto.usuario.UsuarioUpdateDTO;
import com.enterprise.ong_pet2.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Usuários", description = "Cadastro e gestão de usuários")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @Operation(
            summary = "Listar todos os usuários",
            description = "Retorna todos os usuários paginados. Requer role ADMIN.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
                    @ApiResponse(responseCode = "204", description = "Nenhum usuário encontrado"),
                    @ApiResponse(responseCode = "403", description = "Sem permissão")
            }
    )
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UsuarioResponseDTO>> getAllUsuarios(
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        var usuarios = usuarioService.getAllUsuarios(pageable);
        return usuarios.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(usuarios);
    }

    @Operation(
            summary = "Buscar usuários por filtro",
            description = "Filtra usuários por nome, CPF, cidade ou perfil. Requer role ADMIN.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Resultado retornado com sucesso"),
                    @ApiResponse(responseCode = "204", description = "Nenhum resultado encontrado")
            }
    )
    @GetMapping("/results")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UsuarioResponseDTO>> getUsuariosByFilter(
            @Parameter(description = "Parte do nome do usuário")
            @RequestParam(required = false) String nome,
            @Parameter(description = "CPF do usuário")
            @RequestParam(required = false) String cpf,
            @Parameter(description = "Cidade do usuário")
            @RequestParam(required = false) String cidade,
            @Parameter(description = "Perfil do usuário: ADMIN, VOLUNTARIO ou PADRAO")
            @RequestParam(required = false) PerfilUsuario perfil,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        var usuarios = usuarioService.getUsuariosByFilter(nome, cpf, cidade, perfil, pageable);
        return usuarios.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(usuarios);
    }

    @Operation(
            summary = "Buscar usuário por ID",
            description = "Retorna um usuário pelo ID. Requer role ADMIN.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Usuário encontrado"),
                    @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
            }
    )
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioResponseDTO> getUsuarioById(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.getUsuarioById(id));
    }

    @Operation(
            summary = "Cadastrar usuário",
            description = "Cria um novo usuário com perfil PADRAO. Endpoint público.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Usuário criado com sucesso"),
                    @ApiResponse(responseCode = "400", description = "Dados inválidos"),
                    @ApiResponse(responseCode = "422", description = "Email ou CPF já cadastrado")
            }
    )
    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> createUsuario(
            @Valid @RequestBody UsuarioRequestDTO dto) {
        var usuario = usuarioService.createUsuario(dto);
        var uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(usuario.id()).toUri();
        return ResponseEntity.created(uri).body(usuario);
    }

    @Operation(
            summary = "Atualizar usuário",
            description = "Atualiza dados do usuário. Usuário só pode alterar o próprio perfil. ADMIN pode alterar qualquer um.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Usuário atualizado com sucesso"),
                    @ApiResponse(responseCode = "403", description = "Sem permissão para alterar este usuário"),
                    @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
            }
    )
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> updateUsuario(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioUpdateDTO dto) {
        return ResponseEntity.ok(usuarioService.updateUsuario(id, dto));
    }

    @Operation(
            summary = "Excluir usuário",
            description = "Remove um usuário pelo ID. Requer role ADMIN.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Usuário excluído com sucesso"),
                    @ApiResponse(responseCode = "403", description = "Sem permissão"),
                    @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
            }
    )
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteUsuario(@PathVariable Long id) {
        usuarioService.deleteUsuario(id);
        return ResponseEntity.ok("Usuário excluído com sucesso: " + id);
    }
}