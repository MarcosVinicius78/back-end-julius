package com.julius.julius.controller;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.julius.julius.DTO.ProdutoAtualizarDto;
import com.julius.julius.DTO.ProdutoSalvarDto;
import com.julius.julius.DTO.response.ProdutoDto;
import com.julius.julius.DTO.response.ProdutoResponseDto;
import com.julius.julius.service.ProdutoService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/produto")
@RequiredArgsConstructor
public class ProdutoController {

    private final ProdutoService produtoService;

    private static final String UPLOAD_DIR = "uploads/produtos";

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

    @PostMapping("/salvar")
    public ResponseEntity<ProdutoResponseDto> salvarProduto(@RequestBody ProdutoSalvarDto produtoSalvarDto) {
        
        return ResponseEntity.ok().body(produtoService.salvarProduto(produtoSalvarDto));
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file, @RequestParam("id") Long id) throws FileUploadException {
        produtoService.salvarImagemProduto(file, id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<?> deletarProduto(@RequestParam("id") Long id, @RequestParam("urlImagem") String urlImagem) throws FileNotFoundException{
        this.produtoService.apagarProduto(id,urlImagem);
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
				.contentType(MediaType.valueOf("image/png"))
				.body(resource);
        }

        return ResponseEntity.notFound().build();
    }




    @GetMapping("/generate-image")
    public ResponseEntity<byte[]> generateImage() {
        try {
            // Carregar a imagem
            BufferedImage image = ImageIO.read(new File(UPLOAD_DIR+"/Screenshot_20240423-213333.jpg"));

            // Desenhar texto na imagem
            Graphics2D g = image.createGraphics();
            g.setColor(Color.BLACK);
            
            g.setFont(new Font("Arial", Font.BOLD, 80));
            g.drawString("R$ 500,00", 400, 1510);

            g.setFont(new Font("Arial", Font.BOLD, 60));
            drawText(g,String.valueOf("Acer Nitro 5 i5, 256GB SSD \n+ 5GB RAM"), 80, 1200);

            g.dispose();

            // Converter a imagem para um array de bytes
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "jpg", baos);
            byte[] bytes = baos.toByteArray();

            // Retornar a imagem gerada
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_JPEG);
            return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void drawText(Graphics2D g, String text, int x, int y) {
        String[] lines = text.split("\n");
        int lineHeight = g.getFontMetrics().getHeight();
        for (String line : lines) {
            g.drawString(line, x, y);
            y += lineHeight;
        }
    }
}