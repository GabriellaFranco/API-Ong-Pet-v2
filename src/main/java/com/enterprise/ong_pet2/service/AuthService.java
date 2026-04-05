package com.enterprise.ong_pet2.service;

import com.enterprise.ong_pet2.exception.ResourceNotFoundException;
import com.enterprise.ong_pet2.model.dto.auth.AtualizarPerfilEAutoridadesDTO;
import com.enterprise.ong_pet2.model.dto.auth.UpdateSenhaDTO;
import com.enterprise.ong_pet2.repository.AutoridadeRepository;
import com.enterprise.ong_pet2.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final AutoridadeRepository autoridadeRepository;
    private final UsuarioService usuarioService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void updateSenha(UpdateSenhaDTO dto) {
        var usuario = usuarioService.getUsuarioLogado();

        if (!passwordEncoder.matches(dto.senhaAtual(), usuario.getSenha())) {
            throw new BadCredentialsException("Senha atual incorreta");
        }

        usuario.setSenha(passwordEncoder.encode(dto.novaSenha()));
        usuarioRepository.save(usuario);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void atualizarPerfilEAutoridades(Long idUsuario, AtualizarPerfilEAutoridadesDTO dto) {
        var usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + idUsuario));

        Optional.ofNullable(dto.novoPerfil())
                .ifPresent(usuario::setPerfil);

        Optional.ofNullable(dto.novasAutoridades())
                .filter(lista -> !lista.isEmpty())
                .ifPresent(nomes -> {
                    var autoridades = autoridadeRepository.findByNameIn(nomes);
                    usuario.setAutoridades(autoridades);
                });

        usuarioRepository.save(usuario);
    }
}