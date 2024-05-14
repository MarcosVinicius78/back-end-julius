package com.julius.julius.controller;

import java.util.List;

import org.apache.commons.io.FileExistsException;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.core.io.Resource;
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

import com.julius.julius.DTO.request.postDTO.PostRequestDTO;
import com.julius.julius.DTO.response.postDTO.PostResponseDTO;
import com.julius.julius.service.PostService;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/post")
@AllArgsConstructor
public class PostController {
    
    private final PostService postService;

    @GetMapping
    public ResponseEntity<List<PostResponseDTO>> listarPosts(){
        return ResponseEntity.ok().body(postService.listarPosts());
    }
    
    @GetMapping("{id}")
    public ResponseEntity<PostResponseDTO> pegarPost(@PathVariable Long id){
        return ResponseEntity.ok().body(postService.pegarPost(id));
    }


    @PostMapping("salvar")
    public ResponseEntity<PostResponseDTO> salvarPost(@RequestBody PostRequestDTO postRequestDTO){

        return ResponseEntity.ok().body(postService.salvarPost(postRequestDTO));
    }

    @PostMapping("upload")
    public ResponseEntity<?> salvarImagemPost(@RequestParam("file") MultipartFile file, @RequestParam("id") Long id) throws FileUploadException{

        postService.salvarImagemPost(file, id);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public void apagarPost(@RequestParam("id") Long id, @RequestParam("urlImagem") String urlImagem) throws FileExistsException{
        postService.apagarPost(id, urlImagem);
    }

    @PutMapping
    public ResponseEntity<PostResponseDTO> atualizarPost(@RequestBody PostRequestDTO postRequestDTO){
        return ResponseEntity.ok().body(postService.atualizarPost(postRequestDTO));
    }

    @GetMapping("/download/{imagem}")
    public ResponseEntity<Resource> downloadImagem(@PathVariable String imagem) {

        Resource resource = null;
        if (imagem != null) {
            resource = postService.loadImagemAResource(imagem);
        }

        if (resource.exists()) {
            return ResponseEntity.status(HttpStatus.OK)
                    .contentType(MediaType.valueOf("image/jpg"))
                    .body(resource);
        }

        return ResponseEntity.notFound().build();
    }
}
