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
import java.util.Optional;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.julius.julius.DTO.response.LinksBannersDto;
import com.julius.julius.models.Banner;
import com.julius.julius.models.Link;
import com.julius.julius.repository.BannerRepository;
import com.julius.julius.repository.LinksRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BannersService {

    private static final String UPLOAD_DIR = "/uploads/publicidade";

    private final LinksRepository linksRepository;

    private final BannerRepository bannerRepository;

    public String salvarBnnersImagem(MultipartFile file) {

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

            // String imageUrl = uploadsDir.getAbsolutePath() + fileName;

            return nomeImagem;
        } catch (Exception e) {
            return null;
        }

    }

    public String salvarBnners(MultipartFile file, MultipartFile fileMobile, String nome, String link) {

        Banner banner = Banner.builder()
                .nome(nome)
                .link(link)
                .urlImagem(salvarBnnersImagem(file))
                .urlImagemMobile(salvarBnnersImagem(fileMobile))
                .build();

        bannerRepository.save(banner);

        return "";
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

    public Link salvarLinks(Link link) {

        if (link.getId() != null && link.getSiteId() == 1) {

            Optional<Link> links2 = linksRepository.findById(link.getId());

            links2.get().setWhatsapp(link.getWhatsapp());
            links2.get().setTelegram(link.getTelegram());
            links2.get().setInstagram(link.getInstagram());
            links2.get().setEmail(link.getEmail());
            links2.get().setSiteId(1L);

            return linksRepository.save(links2.get());
        } else if (link.getId() != null && link.getSiteId() == 2) {
            Optional<Link> links2 = linksRepository.findById(link.getId());

            links2.get().setWhatsapp(link.getWhatsapp());
            links2.get().setTelegram(link.getTelegram());
            links2.get().setInstagram(link.getInstagram());
            links2.get().setEmail(link.getEmail());
            links2.get().setSiteId(2L);

            return linksRepository.save(links2.get());
        }

        return linksRepository.save(link);
    }

    public LinksBannersDto listarLinksEbanners(Long siteId) {

        Link links = linksRepository.pegarLinkeBannersSiteId(siteId);

        List<Banner> banners = bannerRepository.findAll();

        return LinksBannersDto.toResonse(links, banners);
    }

    // Método para editar um banner existente
public String editarBanner(Long bannerId, MultipartFile file, MultipartFile fileMobile, String nome, String link) {
    try {
        // Buscar o banner no banco de dados
        Banner banner = bannerRepository.findById(bannerId)
                .orElseThrow(() -> new RuntimeException("Banner não encontrado"));

        // Atualizar o nome e o link do banner
        banner.setNome(nome);
        banner.setLink(link);

        // Se uma nova imagem foi enviada, substitua a anterior
        if (file != null && !file.isEmpty()) {
            String novaImagem = salvarBnnersImagem(file);
            if (novaImagem != null) {
                // Excluir a imagem antiga, se existir
                Path oldImagePath = Paths.get(UPLOAD_DIR, banner.getUrlImagem());
                Files.deleteIfExists(oldImagePath);

                // Atualizar a nova URL da imagem no banner
                banner.setUrlImagem(novaImagem);
            }
        }

        // Se uma nova imagem mobile foi enviada, substitua a anterior
        if (fileMobile != null && !fileMobile.isEmpty()) {
            String novaImagemMobile = salvarBnnersImagem(fileMobile);
            if (novaImagemMobile != null) {
                // Excluir a imagem mobile antiga, se existir
                Path oldMobileImagePath = Paths.get(UPLOAD_DIR, banner.getUrlImagemMobile());
                Files.deleteIfExists(oldMobileImagePath);

                // Atualizar a nova URL da imagem mobile no banner
                banner.setUrlImagemMobile(novaImagemMobile);
            }
        }

        // Salvar as alterações no banco de dados
        bannerRepository.save(banner);

        return "Banner atualizado com sucesso";
    } catch (Exception e) {
        return "Erro ao atualizar o banner";
    }
}

}
