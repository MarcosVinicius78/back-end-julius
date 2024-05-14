package com.julius.julius.controller;

import java.io.FileNotFoundException;
import java.util.List;

import org.apache.commons.io.FileExistsException;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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
    public ResponseEntity<LojaResponseDto> cadastrarLoja(@RequestParam("file") MultipartFile file ,@RequestParam("nomeLoja") String nomeLoja){
        LojaResponseDto lojaSalva = lojaService.salvarLoja(nomeLoja, file);
        
        if (lojaSalva != null) {
            return ResponseEntity.ok().body(lojaSalva);
        }

        return ResponseEntity.noContent().build();
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

    @GetMapping("{id}")
    public ResponseEntity<LojaResponseDto> pegarLoja(@PathVariable Long id) {

        LojaResponseDto responseDto = lojaService.pegarLoja(id);

        if (responseDto != null) {
            return ResponseEntity.ok().body(responseDto);
        }

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Boolean> apagarLoja(@RequestParam(name = "id") Long id, @RequestParam(name = "urlImagem") String urlImagem) throws FileExistsException{
        Boolean apagado = lojaService.apagarLoja(id, urlImagem);
        if (apagado) {
            return ResponseEntity.ok().build();
        }

        return ResponseEntity.badRequest().build();
    } 
    
    @PutMapping
    public ResponseEntity<Boolean> atualizarLoja(@RequestParam(name = "file", required = false) MultipartFile file ,@RequestParam("nomeLoja") String nomeLoja, @RequestParam("id") Long id){

        Boolean salvo = lojaService.atualizarLoja(nomeLoja, file, id);

        if (salvo) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }
}
