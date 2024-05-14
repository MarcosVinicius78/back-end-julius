package com.julius.julius.controller;

import java.util.List;

import org.apache.commons.io.FileExistsException;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
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

import com.julius.julius.DTO.ProdutoAtualizarDto;
import com.julius.julius.DTO.ProdutoSalvarDto;
import com.julius.julius.DTO.response.ProdutoDto;
import com.julius.julius.DTO.response.ProdutoResponseDto;
import com.julius.julius.service.ProdutoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/produto")
@RequiredArgsConstructor
public class ProdutoController {

    private final ProdutoService produtoService;

    @GetMapping("{id}")
    public ResponseEntity<ProdutoDto> pegarProduto(@PathVariable Long id) {
        return ResponseEntity.ok().body(produtoService.pegarProduto(id));
    }

    @GetMapping()
    public ResponseEntity<Page<ProdutoResponseDto>> listarProdutosPaginacao(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);

        Page<ProdutoResponseDto> produtos = produtoService.getProdutosPaginados(pageable);

        return new ResponseEntity<>(produtos, HttpStatus.OK);
    }

    @PostMapping("/salvar")
    public ResponseEntity<ProdutoResponseDto> salvarProduto(@RequestBody @Valid ProdutoSalvarDto produtoSalvarDto) {

        if (produtoSalvarDto != null) {
            return ResponseEntity.ok().body(produtoService.salvarProduto(produtoSalvarDto));
        }

        return ResponseEntity.notFound().build();
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file, @RequestParam("id") Long id)
            throws FileUploadException {
        produtoService.salvarImagemProduto(file, id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<?> deletarProduto(@RequestParam("id") Long id, @RequestParam("urlImagem") String urlImagem)
            throws FileExistsException {
        Boolean apagado = this.produtoService.apagarProduto(id, urlImagem);
        if (apagado) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }

    @PutMapping
    public ResponseEntity<ProdutoResponseDto> atualizarProduto(@RequestBody ProdutoAtualizarDto produtoAtualizarDto) {
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
    public ResponseEntity<Integer> apagarVariosProdutos(@RequestBody @Valid List<ProdutoDto> produtosSelecionados) {

        produtoService.apagarVariosProdutos(produtosSelecionados);

        return ResponseEntity.ok().body(produtosSelecionados.size());
    }

    @GetMapping("/pesquisar")
    public ResponseEntity<List<ProdutoResponseDto>> pesquisarProdutos(@RequestParam String termoPesquisa) {
        List<ProdutoResponseDto> resultados = produtoService.pesquisarProdutos(termoPesquisa);
        return ResponseEntity.ok().body(resultados);
    }

    @GetMapping("/download/{imagem}")
    public ResponseEntity<Resource> downloadImagem(@PathVariable String imagem) {

        Resource resource = null;
        if (imagem != null) {
            resource = produtoService.loadImagemAResource(imagem);
        }

        if (resource.exists()) {
            return ResponseEntity.status(HttpStatus.OK)
                    .contentType(MediaType.valueOf("image/jpg"))
                    .body(resource);
        }

        return ResponseEntity.notFound().build();
    }

    @GetMapping("/generate-image")
    public ResponseEntity<byte[]> generateImage(@RequestParam(name = "preco", required = false) String preco,
            @RequestParam("titulo") String titulo, @RequestParam("urlImagem") String urlImagem,
            @RequestParam("frete") String frete, @RequestParam("cupom") String cupom) {

        byte[] bytes = produtoService.gerarStory(preco, titulo, urlImagem, frete, cupom);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);
        headers.setContentLength(bytes.length);
        headers.setContentDispositionFormData("attachment", "nome_da_imagem.jpg");
        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    }

}