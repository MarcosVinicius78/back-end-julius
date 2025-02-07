package com.julius.julius.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileExistsException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.julius.julius.DTO.response.LojaResponseDto;
import com.julius.julius.models.Loja;
import com.julius.julius.repository.LojaRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LojaService {

    private static final String UPLOAD_DIR = "/uploads/lojas";

    private final LojaRepository lojaRepository;

    private final ImagemService imagemService;

    @PersistenceContext
    private EntityManager entityManager;

    public List<LojaResponseDto> listarLojas() {
        return lojaRepository.findAllByOrderByNomeLojaAsc().stream().map(LojaResponseDto::toResonse).toList();
    }

    private String salvarImagemLoja(MultipartFile file) {
        return imagemService.salvarImagem(file, "lojas");
    }

    public LojaResponseDto salvarLoja(String nomeLoja, MultipartFile file) {

        String nomeImagem = salvarImagemLoja(file);

        if (nomeImagem != null) {
            Loja lojaSalva = Loja.builder()
                    .nomeLoja(nomeLoja)
                    .urlImagem(nomeImagem)
                    .build();

            return LojaResponseDto.toResonse(this.lojaRepository.save(lojaSalva));
        }

        return null;
    }

    public LojaResponseDto pegarLoja(Long id) {
        return LojaResponseDto.toResonse(lojaRepository.findById(id).get());
    }

    @Transactional
    public Boolean apagarLoja(Long id, String urlImagem) throws FileExistsException {

        String caminhoImagem = UPLOAD_DIR + "/" + urlImagem;
        entityManager.createNativeQuery("DELETE FROM produtos WHERE fk_loja = :idLoja")
                .setParameter("idLoja", id)
                .executeUpdate();
        this.lojaRepository.deleteById(id);

        File arquivoImagem = new File(caminhoImagem);
        if (arquivoImagem.exists()) {
            arquivoImagem.delete();
        } else {
            throw new FileExistsException("arquivo não encontrado");
        }

        return true;
    }

    public Boolean atualizarLoja(String nomeLoja, MultipartFile file, Long id) {

        Loja loja = lojaRepository.findById(id).orElse(null);

        if (file != null) {
            String urlImagem = salvarImagemLoja(file);
            loja.setUrlImagem(urlImagem);
        }
    
        loja.setNomeLoja(nomeLoja);
        
        lojaRepository.save(loja);
    
        return true;
    }
}
