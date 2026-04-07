package com.enterprise.ong_pet2.service;

import com.enterprise.ong_pet2.config.s3.S3Properties;
import com.enterprise.ong_pet2.entity.AnimalMidia;
import com.enterprise.ong_pet2.enums.TipoMidia;
import com.enterprise.ong_pet2.exception.BusinessException;
import com.enterprise.ong_pet2.exception.ResourceNotFoundException;
import com.enterprise.ong_pet2.mapper.AnimalMidiaMapper;
import com.enterprise.ong_pet2.model.dto.midia.AnimalMidiaResponseDTO;
import com.enterprise.ong_pet2.model.dto.midia.ReordenarMidiaDTO;
import com.enterprise.ong_pet2.repository.AnimalMidiaRepository;
import com.enterprise.ong_pet2.repository.AnimalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MidiaService {

    private final S3Client s3Client;
    private final S3Properties s3Properties;
    private final AnimalRepository animalRepository;
    private final AnimalMidiaRepository animalMidiaRepository;
    private final AnimalMidiaMapper animalMidiaMapper;

    private static final long MAX_TAMANHO_FOTO  = 10L * 1024 * 1024; // 10MB
    private static final long MAX_TAMANHO_VIDEO = 50L * 1024 * 1024; // 50MB

    private static final Set<String> MIMES_PERMITIDOS = Set.of(
            "image/jpeg", "image/png", "image/webp", "video/mp4"
    );

    @Transactional
    public AnimalMidiaResponseDTO upload(Long animalId, MultipartFile arquivo) {
        var animal = animalRepository.findById(animalId)
                .orElseThrow(() -> new ResourceNotFoundException("Animal não encontrado: " + animalId));

        var mimeType = detectarMimeType(arquivo);
        validarMimeType(mimeType);
        validarTamanho(arquivo, mimeType);

        var tipoMidia = mimeType.startsWith("video/") ? TipoMidia.VIDEO : TipoMidia.FOTO;
        var chaveS3 = gerarChaveS3(animalId, arquivo.getOriginalFilename());
        var url = fazerUploadParaS3(arquivo, chaveS3, mimeType);

        var midias = animalMidiaRepository.findByAnimalOrderByOrdemAsc(animal);
        int proximaOrdem = midias.size();

        boolean isPrincipal = midias.isEmpty();

        var midia = AnimalMidia.builder()
                .animal(animal)
                .url(url)
                .chaveS3(chaveS3)
                .tipo(tipoMidia)
                .principal(isPrincipal)
                .ordem(proximaOrdem)
                .tamanhoBytes(arquivo.getSize())
                .mimeType(mimeType)
                .nomeOriginal(arquivo.getOriginalFilename())
                .build();

        return animalMidiaMapper.toResponseDTO(animalMidiaRepository.save(midia));
    }

    public List<AnimalMidiaResponseDTO> listarMidias(Long animalId) {
        var animal = animalRepository.findById(animalId)
                .orElseThrow(() -> new ResourceNotFoundException("Animal não encontrado: " + animalId));

        return animalMidiaRepository.findByAnimalOrderByOrdemAsc(animal)
                .stream()
                .map(animalMidiaMapper::toResponseDTO)
                .toList();
    }

    @Transactional
    public AnimalMidiaResponseDTO definirPrincipal(Long animalId, Long midiaId) {
        var animal = animalRepository.findById(animalId)
                .orElseThrow(() -> new ResourceNotFoundException("Animal não encontrado: " + animalId));

        var midia = animalMidiaRepository.findById(midiaId)
                .orElseThrow(() -> new ResourceNotFoundException("Mídia não encontrada: " + midiaId));

        if (!midia.getAnimal().getId().equals(animalId)) {
            throw new BusinessException("Esta mídia não pertence ao animal informado");
        }

        if (midia.getTipo() == TipoMidia.VIDEO) {
            throw new BusinessException("Não é possível definir um vídeo como foto principal");
        }

        animalMidiaRepository.clearPrincipalByAnimal(animal);

        midia.setPrincipal(true);
        return animalMidiaMapper.toResponseDTO(animalMidiaRepository.save(midia));
    }

    @Transactional
    public List<AnimalMidiaResponseDTO> reordenar(Long animalId, List<ReordenarMidiaDTO> ordens) {
        var animal = animalRepository.findById(animalId)
                .orElseThrow(() -> new ResourceNotFoundException("Animal não encontrado: " + animalId));

        ordens.forEach(item -> {
            var midia = animalMidiaRepository.findById(item.midiaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Mídia não encontrada: " + item.midiaId()));

            if (!midia.getAnimal().getId().equals(animalId)) {
                throw new BusinessException("Mídia " + item.midiaId() + " não pertence ao animal informado");
            }

            midia.setOrdem(item.novaOrdem());
            animalMidiaRepository.save(midia);
        });

        return animalMidiaRepository.findByAnimalOrderByOrdemAsc(animal)
                .stream()
                .map(animalMidiaMapper::toResponseDTO)
                .toList();
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'VOLUNTARIO', 'PADRAO')")
    public void deletarMidia(Long animalId, Long midiaId) {
        var midia = animalMidiaRepository.findById(midiaId)
                .orElseThrow(() -> new ResourceNotFoundException("Mídia não encontrada: " + midiaId));

        if (!midia.getAnimal().getId().equals(animalId)) {
            throw new BusinessException("Esta mídia não pertence ao animal informado");
        }

        deletarDoS3(midia.getChaveS3());

        // Se era a principal, promove a próxima foto disponível
        if (Boolean.TRUE.equals(midia.getPrincipal())) {
            var animal = midia.getAnimal();
            animalMidiaRepository.delete(midia);

            animalMidiaRepository.findByAnimalOrderByOrdemAsc(animal)
                    .stream()
                    .filter(m -> m.getTipo() == TipoMidia.FOTO)
                    .findFirst()
                    .ifPresent(proxima -> {
                        proxima.setPrincipal(true);
                        animalMidiaRepository.save(proxima);
                    });
        } else {
            animalMidiaRepository.delete(midia);
        }
    }

    private String detectarMimeType(MultipartFile arquivo) {
        try {
            return new Tika().detect(arquivo.getBytes());
        } catch (IOException e) {
            throw new BusinessException("Não foi possível detectar o tipo do arquivo");
        }
    }

    private void validarMimeType(String mimeType) {
        if (!MIMES_PERMITIDOS.contains(mimeType)) {
            throw new BusinessException(
                    "Tipo de arquivo não permitido: " + mimeType +
                            ". Permitidos: JPEG, PNG, WebP e MP4"
            );
        }
    }

    private void validarTamanho(MultipartFile arquivo, String mimeType) {
        long tamanhoMaximo = mimeType.startsWith("video/")
                ? MAX_TAMANHO_VIDEO
                : MAX_TAMANHO_FOTO;

        if (arquivo.getSize() > tamanhoMaximo) {
            String limite = mimeType.startsWith("video/") ? "50MB" : "10MB";
            throw new BusinessException("Arquivo excede o tamanho máximo permitido de " + limite);
        }
    }

    private String gerarChaveS3(Long animalId, String nomeOriginal) {
        String extensao = "";
        if (nomeOriginal != null && nomeOriginal.contains(".")) {
            extensao = nomeOriginal.substring(nomeOriginal.lastIndexOf("."));
        }
        return "animais/" + animalId + "/" + UUID.randomUUID() + extensao;
    }

    private String fazerUploadParaS3(MultipartFile arquivo, String chaveS3, String mimeType) {
        try {
            var request = PutObjectRequest.builder()
                    .bucket(s3Properties.getBucketName())
                    .key(chaveS3)
                    .contentType(mimeType)
                    .contentLength(arquivo.getSize())
                    .build();

            s3Client.putObject(request, RequestBody.fromBytes(arquivo.getBytes()));

            return s3Properties.getEndpoint() + "/"
                    + s3Properties.getBucketName() + "/"
                    + chaveS3;

        } catch (IOException e) {
            throw new BusinessException("Falha ao fazer upload do arquivo: " + e.getMessage());
        }
    }

    private void deletarDoS3(String chaveS3) {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(s3Properties.getBucketName())
                    .key(chaveS3)
                    .build());
        } catch (Exception e) {
            log.error("Falha ao deletar arquivo do S3: chave={} erro={}", chaveS3, e.getMessage());
        }
    }
}
