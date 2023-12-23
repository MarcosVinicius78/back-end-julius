package com.julius.julius.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.julius.julius.DTO.CategoriaSalvar;
import com.julius.julius.DTO.response.CategoriaResponseDto;
import com.julius.julius.models.Categoria;
import com.julius.julius.service.CategoriaService;

import lombok.RequiredArgsConstructor;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequiredArgsConstructor
@RequestMapping("/categoria")
public class CategoriaController {

    private final CategoriaService categoriaService;

    @GetMapping
    public ResponseEntity<List<CategoriaResponseDto>> listarCategorias() {
        return ResponseEntity.ok().body(this.categoriaService.listarCategoria());
    }

    @PostMapping
    public ResponseEntity<Categoria> salvarCategoria(@RequestBody CategoriaSalvar categoriaSalvar) {
        return ResponseEntity.ok().body(this.categoriaService.salvarCategoria(categoriaSalvar));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoriaResponseDto> pegarCategoria(@PathVariable Long id) {
        return ResponseEntity.ok().body(categoriaService.pegarCategoria(id));
    }

    @DeleteMapping("{id}")
    public void apagarCategoria(@PathVariable Long id) {
        this.categoriaService.apagarCategoria(id);
    }
    


    @PutMapping
    public ResponseEntity<?> atualizarCategoria(@RequestBody CategoriaResponseDto categoriaResponseDto){

        this.categoriaService.atualizarCategoria(categoriaResponseDto);

        return ResponseEntity.ok().build();
    }
}
