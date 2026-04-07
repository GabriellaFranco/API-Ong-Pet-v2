package com.enterprise.ong_pet2.service;

import com.enterprise.ong_pet2.entity.Usuario;
import com.enterprise.ong_pet2.enums.PerfilUsuario;
import com.enterprise.ong_pet2.enums.TipoAutoridade;
import com.enterprise.ong_pet2.exception.BusinessException;
import com.enterprise.ong_pet2.exception.ResourceNotFoundException;
import com.enterprise.ong_pet2.mapper.UsuarioMapper;
import com.enterprise.ong_pet2.model.dto.usuario.UsuarioRequestDTO;
import com.enterprise.ong_pet2.model.dto.usuario.UsuarioResponseDTO;
import com.enterprise.ong_pet2.model.dto.usuario.UsuarioUpdateDTO;
import com.enterprise.ong_pet2.repository.AutoridadeRepository;
import com.enterprise.ong_pet2.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final AutoridadeRepository autoridadeRepository;
    private final UsuarioMapper usuarioMapper;
    private final PasswordEncoder passwordEncoder;

    @PreAuthorize("hasRole('ADMIN')")
    public Page<UsuarioResponseDTO> getAllUsuarios(Pageable pageable) {
        return usuarioRepository.findAll(pageable)
                .map(usuarioMapper::toResponseDTO);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Page<UsuarioResponseDTO> getUsuariosByFilter(String nome, String cpf,
                                                        String cidade, PerfilUsuario perfil,
                                                        Pageable pageable) {
        return usuarioRepository.findByFilter(nome, cpf, cidade, perfil, pageable)
                .map(usuarioMapper::toResponseDTO);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public UsuarioResponseDTO getUsuarioById(Long id) {
        return usuarioRepository.findById(id)
                .map(usuarioMapper::toResponseDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + id));
    }

    @Transactional
    public UsuarioResponseDTO createUsuario(UsuarioRequestDTO dto) {
        if (usuarioRepository.existsByEmail(dto.email())) {
            throw new BusinessException("Email já cadastrado: " + dto.email());
        }
        if (usuarioRepository.existsByCpf(dto.cpf())) {
            throw new BusinessException("CPF já cadastrado: " + dto.cpf());
        }

        var autoridade = autoridadeRepository.findByName(TipoAutoridade.ROLE_PADRAO.name())
                .orElseThrow(() -> new ResourceNotFoundException("Autoridade não encontrada"));

        var usuario = usuarioMapper.toUsuario(dto);
        usuario.setPerfil(PerfilUsuario.PADRAO);
        usuario.setAutoridades(List.of(autoridade));
        usuario.setSenha(passwordEncoder.encode(dto.senha()));

        return usuarioMapper.toResponseDTO(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponseDTO updateUsuario(Long id, UsuarioUpdateDTO dto) {
        var usuarioLogado = getUsuarioLogado();
        var usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + id));

        boolean isAdmin = usuarioLogado.getPerfil().equals(PerfilUsuario.ADMIN);
        boolean isProprioUsuario = usuarioLogado.getId().equals(id);

        if (!isAdmin && !isProprioUsuario) {
            throw new BusinessException("Você não tem permissão para alterar este usuário");
        }

        if (dto.email() != null && !dto.email().equals(usuario.getEmail())) {
            if (usuarioRepository.existsByEmail(dto.email())) {
                throw new BusinessException("Email já cadastrado: " + dto.email());
            }
        }

        usuarioMapper.updateFromDTO(dto, usuario);
        return usuarioMapper.toResponseDTO(usuarioRepository.save(usuario));
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUsuario(Long id) {
        var usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + id));
        usuarioRepository.delete(usuario);
    }

    public Usuario getUsuarioLogado() {
        var principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof String email)) {
            throw new BadCredentialsException("Usuário não autenticado");
        }
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + email));
    }
}