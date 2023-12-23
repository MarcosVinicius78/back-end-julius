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

    // private final String caminho =
    // "C:\\Users\\marco\\OneDrive\\Documentos\\Julius da promo back
    // end\\julius\\src\\main\\resources\\static\\lojas";

    private static final String UPLOAD_DIR = "uploads";

    private final LojaRepository lojaRepository;

    // public Resource listarLojas() {

    // Resource resource = null;

    // try {

    // Path imagemPath = Path.of(UPLOAD_DIR, "download.png");
    // resource = new UrlResource(imagemPath.toUri());

    // } catch (Exception e) {
    // // TODO: handle exception
    // }

    // return resource;
    // }

    public List<LojaResponseDto> listarLojas() {
        return lojaRepository.findAll().stream().map(LojaResponseDto::toResonse).toList();
    }

    public Loja salvarLoja(LojaSalvarDto loja) {

        if (loja.url_imagem() != null && !loja.url_imagem().isEmpty()) {
            byte[] imageByte = Base64.getDecoder().decode(loja.url_imagem());

            Loja lojaSalvar = Loja.builder()
                    .nome_loja(loja.nome_loja())
                    .imagem(imageByte)
                    .build();
            Loja lojaSalva = lojaRepository.save(lojaSalvar);
            return lojaSalva;
        }

        return null;

    }

    // public Loja salvarLoja(String nome_loja, MultipartFile file){

    // try {
    // File uploadsDir = new File(UPLOAD_DIR);
    // if (!uploadsDir.exists()) {
    // uploadsDir.mkdirs();
    // }

    // Date data = new Date();

    // String fileName = file.getOriginalFilename();
    // Path filePath = Path.of(uploadsDir.getAbsolutePath(),
    // data.getTime()+fileName);

    // Files.copy(file.getInputStream(), filePath,
    // StandardCopyOption.REPLACE_EXISTING);

    // String imageUrl = uploadsDir.getAbsolutePath() + fileName;

    // Loja lojaSalva = Loja.builder()
    // .nome_loja(nome_loja)
    // .url_imagem(imageUrl)
    // .build();
    // return this.lojaRepository.save(lojaSalva);

    // } catch (Exception e) {
    // return null;
    // }

    // }

    // public Loja salvarLoja(String nome_loja, MultipartFile imagem){

    // String nomeArquivo = StringUtils.cleanPath(imagem.getOriginalFilename());
    // Date date = new Date();
    // Path caminhoDestino = Paths.get(caminho).resolve(date.getTime()+nomeArquivo);
    // String url_imagem = caminhoDestino.toString();
    // try {
    // Files.copy(imagem.getInputStream(), caminhoDestino);

    // } catch (Exception e) {
    // e.printStackTrace();
    // }

    // Loja loja = Loja.builder()
    // .nome_loja(nome_loja)
    // .url_imagem(url_imagem)
    // .build();

    // return lojaRepository.save(loja);
    // }
}
