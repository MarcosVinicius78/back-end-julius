package com.julius.julius.controller;

import java.io.FileNotFoundException;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
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

import com.julius.julius.DTO.response.LinksBannersDto;
import com.julius.julius.models.Link;
import com.julius.julius.service.BannersService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("banners")
@RequiredArgsConstructor
public class BannersController {
    
    private final BannersService bannersService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file,@RequestParam("fileMobile") MultipartFile fileMobile,@RequestParam("nome") String nome, @RequestParam(name = "link", required = false) String link) {
        return ResponseEntity.ok().body(bannersService.salvarBnners(file, fileMobile, nome, link));
    }

    @GetMapping("/{imageName}")
    public ResponseEntity<Resource> downloadImage(@PathVariable String imageName) throws FileNotFoundException {
        Resource resource = bannersService.loadImageAsResource(imageName);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @PutMapping
    public ResponseEntity<Link> salvarLinks(@RequestBody Link link){
        return ResponseEntity.ok().body(bannersService.salvarLinks(link));
    }

    @GetMapping("links-site/{siteId}")
    public LinksBannersDto listarLinksEBanners(@PathVariable Long siteId){
        return bannersService.listarLinksEbanners(siteId);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<?> apagarBanner(@PathVariable Long id){
        bannersService.excluirBanner(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{bannerId}")
    public ResponseEntity<String> editarBanner(@PathVariable Long bannerId,
                                               @RequestParam(value = "file", required = false) MultipartFile file,
                                               @RequestParam(value = "fileMobile", required = false) MultipartFile fileMobile,
                                               @RequestParam("nome") String nome,
                                               @RequestParam("link") String link) {
        String resultado = bannersService.editarBanner(bannerId, file, fileMobile, nome, link);
        return ResponseEntity.ok(resultado);
    }
}

