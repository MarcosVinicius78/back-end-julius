package com.julius.julius.service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.julius.julius.DTO.LojaSalvarDto;
import com.julius.julius.DTO.response.CategoriaResponseDto;
import com.julius.julius.DTO.response.LojaResponseDto;
import com.julius.julius.models.Loja;
import com.julius.julius.repository.LojaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LojaService {

    private static final String UPLOAD_DIR = "uploads/lojas";

    private final LojaRepository lojaRepository;

    public List<LojaResponseDto> listarLojas() {
        return lojaRepository.findAll().stream().map(LojaResponseDto::toResonse).toList();
    }

    public Resource loadImagemAResource(String imagemNome){
        try {
            File uploadDir = new File(UPLOAD_DIR);

            Path imagemPath = Paths.get(uploadDir.getAbsolutePath()).resolve(imagemNome);
            Resource resource = new UrlResource(imagemPath.toUri());

            if (resource.exists() || resource.isReadable()) {
                return resource;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public LojaResponseDto salvarLoja(String nomeLoja, MultipartFile file) {

        try {
            File uploadsDir = new File(UPLOAD_DIR);
            if (!uploadsDir.exists()) {
                uploadsDir.mkdirs();
            }

            Date data = new Date();

            String fileName = file.getOriginalFilename();
            String nomeImagem = data.getTime() + fileName;
            Path filePath = Path.of(uploadsDir.getAbsolutePath(), nomeImagem);

            Files.copy(file.getInputStream(), filePath,
                    StandardCopyOption.REPLACE_EXISTING);

            Loja lojaSalva = Loja.builder()
                    .nomeLoja(nomeLoja)
                    .urlImagem(nomeImagem)
                    .build();

            return LojaResponseDto.toResonse(this.lojaRepository.save(lojaSalva));

        } catch (Exception e) {
            return null;
        }

    }
}
