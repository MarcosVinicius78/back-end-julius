package com.julius.julius.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.julius.julius.DTO.response.LinksBannersDto;
import com.julius.julius.models.Banner;
import com.julius.julius.models.Link;
import com.julius.julius.models.Loja;
import com.julius.julius.repository.BannerRepository;
import com.julius.julius.repository.LinksRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BannersService {

    private static final String UPLOAD_DIR = "uploads/publicidade";

    private final LinksRepository linksRepository;

    private final BannerRepository bannerRepository;

    public String salvarBnners(MultipartFile file, String nome) {

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

            String imageUrl = uploadsDir.getAbsolutePath() + fileName;

            Banner banner = Banner.builder()
                            .nome(nome)
                            .urlImagem(nomeImagem)
                            .build();

            bannerRepository.save(banner);
            return imageUrl;
        } catch (Exception e) {
            return null;
        }

    }

    // Novo método para excluir banners
    public String excluirBanner(Long bannerId) {
        try {
            // Buscar o banner no banco de dados
            Banner banner = bannerRepository.findById(bannerId)
                    .orElseThrow(() -> new RuntimeException("Banner não encontrado"));

            // Obter o nome do arquivo da imagem associada ao banner
            String nomeImagem = banner.getUrlImagem();

            // Criar o caminho do arquivo no diretório de upload
            Path filePath = Paths.get(UPLOAD_DIR, nomeImagem);

            // Excluir a imagem do sistema de arquivos
            Files.deleteIfExists(filePath);

            // Excluir o banner do banco de dados
            bannerRepository.deleteById(bannerId);

            return "Banner excluído com sucesso";
        } catch (Exception e) {
            return "Erro ao excluir o banner";
        }
    }

    public Resource loadImageAsResource(String imageName) throws FileNotFoundException {
        try {
            File uploadsDir = new File(UPLOAD_DIR);

            Path imagePath = Paths.get(uploadsDir.getAbsolutePath()).resolve(imageName);
            Resource resource = new UrlResource(imagePath.toUri());

            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new FileNotFoundException("Could not read file: " + imageName);
            }
        } catch (MalformedURLException e) {
            throw new FileNotFoundException("Could not read file: " + imageName);
        }
    }

    public Link salvarLinks(Link link){

        if (link.getId() != null) {    
            return linksRepository.save(link);
        }
        
        return linksRepository.save(link);
    }

    public LinksBannersDto listarLinksEbanners(){

        List<Link> links = linksRepository.findAll();
        
        List<Banner> banners = bannerRepository.findAll();

        return LinksBannersDto.toResonse(links, banners);
    }
}
