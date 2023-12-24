package com.julius.julius.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.julius.julius.DTO.ProdutoAtualizarDto;
import com.julius.julius.DTO.ProdutoSalvarDto;
import com.julius.julius.DTO.response.ProdutoDto;
import com.julius.julius.DTO.response.ProdutoResponseDto;
import com.julius.julius.service.ProdutoService;

import lombok.RequiredArgsConstructor;

// @CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/produto")
@RequiredArgsConstructor
public class ProdutoController {

    private final ProdutoService produtoService;

    @GetMapping("{id}")
    public ResponseEntity<ProdutoDto> pegarProduto(@PathVariable Long id){
        return ResponseEntity.ok().body(produtoService.pegarProduto(id));
    }

    @GetMapping()
    public ResponseEntity<Page<ProdutoResponseDto>> listarProdutosPaginacao(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);

        Page<ProdutoResponseDto> produtos = produtoService.getProdutosPaginados(pageable);

        return new ResponseEntity<>(produtos, HttpStatus.OK);
    }

    @PostMapping()
    public ResponseEntity<ProdutoResponseDto> salvarProduto(@RequestBody ProdutoSalvarDto produtoSalvarDto) {
        
        return ResponseEntity.ok().body(produtoService.salvarProduto(produtoSalvarDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletarProduto(@PathVariable Long id){
        this.produtoService.apagarProduto(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping
    public ResponseEntity<ProdutoResponseDto> atualizarProduto(@RequestBody ProdutoAtualizarDto produtoAtualizarDto){
        return ResponseEntity.ok().body(this.produtoService.atualizarProduto(produtoAtualizarDto));
    }

    @GetMapping("/por-categoria")
    public ResponseEntity<List<ProdutoResponseDto>> obterProdutosPorCategoria(
            @RequestParam("categoriaId") Long categoriaId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);

        Page<ProdutoResponseDto> produtos = produtoService.obterProdutosPorCategoria(categoriaId, pageable);

        return new ResponseEntity<>(produtos.getContent(), HttpStatus.OK);
    }

    @PostMapping("/apagar-varios")
    public ResponseEntity<Integer> apagarVariosProdutos(@RequestBody List<Long> produtosSelecionados){

        produtoService.apagarVariosProdutos(produtosSelecionados);

        return ResponseEntity.ok().body(produtosSelecionados.size());
    }
}