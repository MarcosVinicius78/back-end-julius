package com.julius.julius.controller;

import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.julius.julius.DTO.LojaSalvarDto;
import com.julius.julius.DTO.response.LojaResponseDto;
import com.julius.julius.service.LojaService;

import lombok.RequiredArgsConstructor;

// @CrossOrigin(origins =  "*")
@RestController
@RequiredArgsConstructor
@RequestMapping("/loja")
public class LojaController {

    private final LojaService lojaService;
    
    @PostMapping
    public void cadastrarLoja(@RequestParam("file") MultipartFile file ,@RequestParam("nomeLoja") String nomeLoja){
        lojaService.salvarLoja(nomeLoja, file);
    }

    @GetMapping
    public ResponseEntity<List<LojaResponseDto>> listarLojas(){
        return ResponseEntity.ok().body(this.lojaService.listarLojas());
    }

    @GetMapping("/mostar-imagem/{nomeImagem}")
    public ResponseEntity<Resource> mostrarImagem(@PathVariable String nomeImagem){
        
        Resource resource = null;
        if (nomeImagem != null) {
            resource = lojaService.loadImagemAResource(nomeImagem);
        }

        if (resource.exists()) {
            return ResponseEntity.ok()
                    .contentType(MediaType.valueOf("image/png"))
                    .body(resource);
        }

        return ResponseEntity.notFound().build();
    }
}
