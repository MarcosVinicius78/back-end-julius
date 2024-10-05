package com.julius.julius.controller;

import java.util.List;

import org.apache.commons.io.FileExistsException;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.julius.julius.DTO.request.promosDTO.PromosSalvarDTO;
import com.julius.julius.DTO.response.PromoResponseDTO;
import com.julius.julius.service.PromoService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/promos")
@RequiredArgsConstructor
public class PromoController {
    
    private final PromoService promoService;

    @GetMapping
    public ResponseEntity<Page<PromoResponseDTO>> pegarPromos(@RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "12") int size){

        Pageable pageable = PageRequest.of(page, size);

        return ResponseEntity.ok().body(promoService.listarPromos(pageable));
    }

    @GetMapping("{id}")
    public ResponseEntity<PromoResponseDTO> pegarPromo(@PathVariable Long id){
        return ResponseEntity.ok().body(promoService.pegarPromo(id));
    }

    @PostMapping("/salvar")
    public ResponseEntity<?> salvarPromo(@RequestBody PromosSalvarDTO promosSalvarDTO){

        return ResponseEntity.ok().body(promoService.salvarPromo(promosSalvarDTO));
    }

    @DeleteMapping
    public ResponseEntity<?> apagarPromo(@RequestParam(name = "id") Long id, @RequestParam(name = "urlImagem") String urlImagem){

        promoService.apagarPromo(id, urlImagem);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/produto")
    public ResponseEntity<?> apagarProdutoPromo(@RequestParam(name = "id") Long id, @RequestParam(name = "idEditar") Long idEditar){
        
        promoService.apagarPromoProduto(id, idEditar);
        
        return ResponseEntity.ok().build();
    }

    @PutMapping
    public ResponseEntity<?> atualizarPromos(@RequestBody PromosSalvarDTO promosSalvarDTO){
        promoService.atualizarPromo(promosSalvarDTO);
        return null;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(@RequestParam(name = "file", required = false) MultipartFile file, @RequestParam("id") Long id)
            throws FileUploadException, FileExistsException {
        
                promoService.salvarImagemProduto(file, id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/download-imagem-promo/{imagemSocial}")
    public ResponseEntity<Resource> downloadImagemReal(@PathVariable String imagemSocial) {

        Resource resource = null;
        if (!imagemSocial.equals("null")) {
            resource = promoService.loadImagemAResourceReal(imagemSocial);
        }

        if (resource != null) {
            return ResponseEntity.status(HttpStatus.OK)
                    .contentType(MediaType.valueOf("image/jpg"))
                    .body(resource);
        }

        return ResponseEntity.noContent().build();
    }

}
