package com.julius.julius.service;

import com.github.dockerjava.api.exception.NotFoundException;
import com.julius.julius.models.Produto;
import com.julius.julius.repository.ProdutoRepository;
import lombok.AllArgsConstructor;
import org.apache.commons.io.FileExistsException;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ImagemService {

    private final ProdutoRepository produtoRepository;

    private static final String UPLOAD_DIR = "/uploads/";

    public String salvarImagem(MultipartFile file, String caminho) {
        // Criar o diretório de uploads se não existir
        File uploadsDir = new File(UPLOAD_DIR.concat(caminho));
        if (!uploadsDir.exists()) {
            uploadsDir.mkdirs();
        }

        Date data = new Date();
        String fileName = file.getOriginalFilename();

        // Gerar nome único para a imagem (evitar conflito)
        String nomeImagem = fileName.contains("feed") ? fileName : data.getTime() + ".jpg";

        // Definir o caminho final para salvar a imagem convertida
        Path filePath = Path.of(uploadsDir.getAbsolutePath(), nomeImagem);

        try {
            // Converter a imagem recebida para o formato desejado
            BufferedImage bufferedImage = ImageIO.read(file.getInputStream());

            // Se a imagem não puder ser lida, lançar uma exceção
            if (bufferedImage == null) {
                throw new IOException("Não foi possível processar a imagem.");
            }

            // Salvar a imagem convertida no formato desejado (jpg)
            boolean isSuccess = ImageIO.write(bufferedImage, "jpg", new File(filePath.toString()));

            if (!isSuccess) {
                throw new IOException("Falha ao salvar a imagem convertida.");
            }

        } catch (IOException e) {
            e.printStackTrace();
            return null; // Se ocorrer um erro, retorna null
        }

        return nomeImagem; // Retorna o nome da imagem salva
    }


    public String salvarImagemUrl(String url, String caminho) {

        try {

            URL file = new URL(url);

            File uploadsDir = new File(UPLOAD_DIR.concat(caminho));
            if (!uploadsDir.exists()) {
                uploadsDir.mkdirs();
            }

            Date data = new Date();

            String fileName = url.toString().substring(url.lastIndexOf("/") + 1);

            String nomeImagem = data.getTime() + fileName;
            Path filePath = Path.of(uploadsDir.getAbsolutePath(), nomeImagem);

            Files.copy(file.openStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return nomeImagem;

        } catch (Exception e) {
            throw new NotFoundException("Imagem não foi salva");
        }
    }

    public String salvarImagemProduto(Long id, MultipartFile imagemPadrao, MultipartFile imagemReal) {

        Produto produto = produtoRepository.findById(id).orElseThrow(() -> new RuntimeException("Produto nao encontrado"));

        if (imagemPadrao != null) {
            apagarImagem(UPLOAD_DIR.concat("/produtos/" + produto.getUrlImagem()));
            produto.setUrlImagem(salvarImagem(imagemPadrao, "produtos"));
        }

        if (imagemReal != null) {
            apagarImagem(UPLOAD_DIR.concat("/produtos-real/" + produto.getImagemSocial()));
            produto.setImagemSocial(salvarImagem(imagemReal, "produtos-real"));
        }

        return produtoRepository.save(produto).getUrlImagem();
    }

    public void apagarImagem(String caminhoImagem) {
        try {
            if (!caminhoImagem.isEmpty()) {
                File arquivoImagem = new File(caminhoImagem);
                if (arquivoImagem.exists()) {
                    arquivoImagem.delete();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void apagarImagensProduto(String urlImagem, String imagemSocial) {
        try {
            String caminhoImagem = "";
            if (urlImagem != null && !urlImagem.isEmpty()) {
                caminhoImagem = UPLOAD_DIR.concat("/produtos/" + urlImagem);
                apagarImagem(caminhoImagem);
            }

            if (imagemSocial != null && !imagemSocial.equals("null") && !imagemSocial.isEmpty()) {
                caminhoImagem = UPLOAD_DIR.concat("/produtos/" + urlImagem);
                apagarImagem(caminhoImagem);
                ;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Resource carregarImagemNormal(String imagemNome, String caminho) {

        try {
            File uploadDir = new File(UPLOAD_DIR.concat("/"+caminho));

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
}
