package com.julius.julius.controller;

import java.awt.FontFormatException;
import java.io.IOException;
import java.util.List;

import com.julius.julius.DTO.response.IProdutoResponseDto;
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
    public ResponseEntity<ProdutoDto> pegarProduto(@PathVariable Long id,
                                                   @RequestParam(value = "r", required = false) Integer r) {
        // return ResponseEntity.ok().body(produtoService.pegarProduto(id));
        if (r != null && r == 1) {
            // Lógica para obter a URL do site oficial do produto baseado no ID
            ProdutoDto officialProductUrl = produtoService.pegarProduto(id);

            // return ResponseEntity.status(302).header("Location", officialProductUrl.link_se()).build();
            return ResponseEntity.status(200).body(officialProductUrl);
        } else {
            ProdutoDto produto = produtoService.pegarProduto(id);
            return ResponseEntity.ok().body(produto);
        }
    }

    @GetMapping("/encerrar-promocao")
    public ResponseEntity<?> encerrarPromocao(@RequestParam Boolean status, @RequestParam Long id) {
        produtoService.encerrarPromocao(status, id);
        return ResponseEntity.ok().build();
    }

    @GetMapping()
    public ResponseEntity<Page<IProdutoResponseDto>> listarProdutosPaginacao(@RequestParam(defaultValue = "0") int page,
                                                                             @RequestParam(defaultValue = "12") int size, @RequestParam(defaultValue = "1", required = false) Long site) {

        Pageable pageable = PageRequest.of(page, size);

        Page<IProdutoResponseDto> produtos = produtoService.getProdutosPaginados(site, pageable);

        return new ResponseEntity<>(produtos, HttpStatus.OK);
    }

    @PostMapping("/salvar")
    public ResponseEntity<ProdutoResponseDto> salvarProduto(@RequestBody @Valid ProdutoSalvarDto produtoSalvarDto) {
        return ResponseEntity.ok().body(produtoService.salvarProduto(produtoSalvarDto));
    }

    @DeleteMapping
    public ResponseEntity<?> deletarProduto(@RequestParam("id") Long id)
            throws FileExistsException {
        Boolean apagado = this.produtoService.apagarProduto(id);
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
    public ResponseEntity<Page<IProdutoResponseDto>> obterProdutosPorCategoria(
            @RequestParam("categoriaId") Long categoriaId,
            @RequestParam("site") Long site,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "12") int size) {

        Pageable pageable = PageRequest.of(page, size);

        Page<IProdutoResponseDto> produtos = produtoService.obterProdutosPorCategoria(site, categoriaId, pageable);

        return new ResponseEntity<>(produtos, HttpStatus.OK);
    }

    @GetMapping("/por-loja")
    public ResponseEntity<Page<ProdutoResponseDto>> obterProdutosPorLoja(
            @RequestParam("lojaId") Long lojaId,
            @RequestParam("site") Long site,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "12") int size) {

        Pageable pageable = PageRequest.of(page, size);

        Page<ProdutoResponseDto> produtos = produtoService.obterProdutosPorLoja(site, lojaId, pageable);

        return new ResponseEntity<>(produtos, HttpStatus.OK);
    }

    @PostMapping("/apagar-varios")
    public ResponseEntity<Integer> apagarVariosProdutos(@RequestBody @Valid List<ProdutoDto> produtosSelecionados) {

        produtoService.apagarVariosProdutos(produtosSelecionados);

        return ResponseEntity.ok().body(produtosSelecionados.size());
    }

    @GetMapping("/pesquisar")
    public ResponseEntity<Page<IProdutoResponseDto>> pesquisarProdutos(@RequestParam String termoPesquisa,
                                                                       @RequestParam(value = "page", defaultValue = "0", required = false) int page,
                                                                       @RequestParam(value = "size", defaultValue = "12", required = false) int size,
                                                                       @RequestParam(value = "site", required = false) Long site) {
        Page<IProdutoResponseDto> resultados = produtoService.pesquisarProdutos(site, termoPesquisa, page, size);

        return ResponseEntity.ok().body(resultados);
    }

    @GetMapping("/download-imagem-real/{imagemSocial}")
    public ResponseEntity<Resource> downloadImagemReal(@PathVariable String imagemSocial) {

        Resource resource = null;
        if (!imagemSocial.equals("null")) {
            resource = produtoService.loadImagemAResourceReal(imagemSocial);
        }

        if (resource != null) {
            return ResponseEntity.status(HttpStatus.OK)
                    .contentType(MediaType.valueOf("image/jpg"))
                    .body(resource);
        }

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/generate-image")
    public ResponseEntity<byte[]> generateImage(@RequestParam(name = "preco", required = false) String preco,
                                                @RequestParam("titulo") String titulo, @RequestParam("urlImagem") String urlImagem,
                                                @RequestParam("frete") String frete, @RequestParam("cupom") String cupom)
            throws FileExistsException, FontFormatException {

        byte[] bytes = produtoService.gerarStory(preco, titulo, urlImagem, frete, cupom);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);
        headers.setContentLength(bytes.length);
        headers.setContentDispositionFormData("attachment", "nome_da_imagem.jpg");
        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    }


    @GetMapping("gerarFeed/{id}")
    public ResponseEntity<byte[]> gerarFeed(@PathVariable Long id)
            throws FontFormatException, IOException {

        byte[] bytes = produtoService.gerarFeed(id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);
        headers.setContentLength(bytes.length);
        headers.setContentDispositionFormData("attachment", "nome_da_imagem.jpg");
        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    }

    @PostMapping("salvar-story")
    public void SalvarStory(@RequestParam("file") MultipartFile file) throws FileUploadException {
        produtoService.salvarStory(file);
    }

    @GetMapping("destaque")
    public ResponseEntity<Page<IProdutoResponseDto>> listarProdutosDestaque(@RequestParam Long site, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "12") int size) {
        return ResponseEntity.ok().body(produtoService.listarProdutosDestaque(site, page, size));
    }

}