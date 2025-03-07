package com.julius.julius.controller;

import com.julius.julius.service.ImagemService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileExistsException;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/imagem")
public class ImagemController {

    private final ImagemService imagemService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(@RequestParam(value = "id", required = false) Long id,
                                         @RequestParam(name = "urlImagem", required = false) MultipartFile urlImagem,
                                         @RequestParam(name = "urlImagemReal", required = false) MultipartFile urlImagemReal) {
        imagemService.salvarImagemProduto(id, urlImagem, urlImagemReal);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/salvar-imagem")
    public ResponseEntity<?> salvarImagem(@RequestParam(name = "urlImagem", required = false) MultipartFile urlImagem,
                                         @RequestParam(name = "caminho", required = false) String caminho) {
        imagemService.salvarImagem(urlImagem, caminho);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/download/{imagem}/{caminho}")
    public ResponseEntity<Resource> carregarImagem(@PathVariable String imagem, @PathVariable String caminho) {

        Resource resource = null;
        if (imagem != null) {
            resource = imagemService.carregarImagemNormal(imagem, caminho);
        }

        if (resource.exists()) {
            return ResponseEntity.status(HttpStatus.OK)
                    .contentType(MediaType.valueOf("image/jpg"))
                    .body(resource);
        }

        return ResponseEntity.notFound().build();
    }

    @GetMapping("/baixar-imagem/{imagem}/{caminho}")
    public ResponseEntity<Resource> baixarImagem(@PathVariable String imagem, @PathVariable String caminho) {

        Resource resource = imagemService.carregarImagemNormal(imagem, caminho);
//        byte[] bytes = produtoService.gerarStory(preco, titulo, urlImagem, frete, cupom);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);
//        headers.setContentLength(bytes.length);
        headers.setContentDispositionFormData("attachment", "caminho");
        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }
}
