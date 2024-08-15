package com.julius.julius.service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.FileExistsException;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.julius.julius.DTO.request.promosDTO.PromosSalvarDTO;
import com.julius.julius.DTO.response.PromoResponseDTO;
import com.julius.julius.models.Produto;
import com.julius.julius.models.Promo;
import com.julius.julius.repository.ProdutoRepository;
import com.julius.julius.repository.PromoRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PromoService {

    private final PromoRepository promoRepository;

    private final ProdutoRepository produtoRepository;

    private static final String UPLOAD_DIR = "/uploads/promos";

    public List<PromoResponseDTO> listarPromos() {

        List<Promo> promo = promoRepository.lsitarPromos();

        return promo.stream().map(PromoResponseDTO::toResponse).toList();
    }

    public PromoResponseDTO salvarPromo(PromosSalvarDTO promosSalvarDTO) {

        Promo promo = Promo.builder()
                .copyPromo(promosSalvarDTO.copyPromo())
                .produtos(produtoRepository.findAllById(promosSalvarDTO.idProdutos()))
                .build();

        Promo promoSalva = promoRepository.save(promo);

        return PromoResponseDTO.toResponse(promoSalva);
    }

    public Resource loadImagemAResourceReal(String imagemNome) {
        if (!imagemNome.equals("null")) {
            
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

        }
        return null;
    }

    private void apagarImagemReal(String urlImagem) throws FileExistsException {

        String caminhoImagem = UPLOAD_DIR + "/" + urlImagem;

        if (!urlImagem.isEmpty()) {
            File arquivoImagem = new File(caminhoImagem);
            if (arquivoImagem.exists()) {
                arquivoImagem.delete();
            } else {
                throw new FileExistsException("Imagem não existe");
            }
        }

    }

    public void apagarPromo(Long id,String urlImagem) {
        try {
            apagarImagemReal(urlImagem);
        } catch (FileExistsException e) {
            
            e.printStackTrace();
        }
        promoRepository.deleteById(id);
    }

    @Transactional
    public void apagarPromoProduto(Long id, Long idEditar) {
        promoRepository.apagarPromoProduto(id, idEditar);
    }

    public void atualizarPromo(PromosSalvarDTO promosSalvarDTO) {
        Promo promo = promoRepository.findById(promosSalvarDTO.id())
                .orElseThrow(() -> new EntityNotFoundException("Entidade Não Encontrada"));

        promo.setCopyPromo(promosSalvarDTO.copyPromo());

        for (Long i : promosSalvarDTO.idProdutos()) {
            if (promo.getProdutos().stream().noneMatch(item -> item.getId().equals(i))) {
                promo.getProdutos().add(produtoRepository.findById(i).get());
            }

        }

        promoRepository.save(promo);
    }

    public PromoResponseDTO pegarPromo(Long id){

        return PromoResponseDTO.toResponse(promoRepository.findById(id).get());
    }

    public String salvarImagemProduto(MultipartFile file, Long id)
            throws FileUploadException, FileExistsException {

        Promo promo = promoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Entidade Não Encontrada"));

        try {
            if (file != null) {
                
                File uploadsDir = new File(UPLOAD_DIR);
                if (!uploadsDir.exists()) {
                    uploadsDir.mkdirs();
                }

                Date data = new Date();

                String fileName = file.getOriginalFilename();
                fileName = data.getTime() + fileName;
                Path filePath = Path.of(uploadsDir.getAbsolutePath(), fileName);

                Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                promo.setUrlImagem(fileName);
        
            }

            promoRepository.save(promo);
            
            return "salvou";
        } catch (Exception e) {
            throw new FileUploadException();
        }
    }

}
