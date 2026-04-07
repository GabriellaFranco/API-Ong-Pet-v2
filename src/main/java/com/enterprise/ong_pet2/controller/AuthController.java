package com.enterprise.ong_pet2.controller;

import com.enterprise.ong_pet2.config.jwt.JwtUtil;
import com.enterprise.ong_pet2.model.dto.auth.AtualizarPerfilEAutoridadesDTO;
import com.enterprise.ong_pet2.model.dto.auth.LoginRequestDTO;
import com.enterprise.ong_pet2.model.dto.auth.LoginResponseDTO;
import com.enterprise.ong_pet2.model.dto.auth.UpdateSenhaDTO;
import com.enterprise.ong_pet2.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Tag(name = "Autenticação", description = "Login, alteração de senha e gestão de perfis")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final AuthService authService;
    private final JwtUtil jwtUtil;

    @Operation(
            summary = "Login",
            description = "Autentica o usuário e retorna o token JWT. " +
                    "Use o token no botão 'Authorize' no topo da página.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Login realizado com sucesso"),
                    @ApiResponse(responseCode = "401", description = "Credenciais inválidas")
            }
    )
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto) {
        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.email(), dto.senha())
        );
        List<String> authorities = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        var token = jwtUtil.generateToken(authentication.getName(), authorities);
        return ResponseEntity.ok(new LoginResponseDTO("Login realizado com sucesso", token));
    }

    @Operation(
            summary = "Alterar senha",
            description = "Altera a senha do usuário autenticado. Requer a senha atual para confirmação.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Senha alterada com sucesso"),
                    @ApiResponse(responseCode = "401", description = "Senha atual incorreta")
            }
    )
    @PatchMapping("/alterar-senha")
    public ResponseEntity<String> updateSenha(@Valid @RequestBody UpdateSenhaDTO dto) {
        authService.updateSenha(dto);
        return ResponseEntity.ok("Senha alterada com sucesso");
    }

    @Operation(
            summary = "Atualizar perfil e autoridades",
            description = "Atualiza o perfil e as autoridades de um usuário. Requer role ADMIN.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Perfil atualizado com sucesso"),
                    @ApiResponse(responseCode = "403", description = "Sem permissão de administrador"),
                    @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
            }
    )
    @PatchMapping("/usuarios/{id}/perfil-autoridades")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> atualizarPerfilEAutoridades(
            @PathVariable Long id,
            @Valid @RequestBody AtualizarPerfilEAutoridadesDTO dto) {
        authService.atualizarPerfilEAutoridades(id, dto);
        return ResponseEntity.ok("Perfil e autoridades atualizados com sucesso");
    }
}