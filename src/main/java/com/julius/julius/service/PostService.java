package com.julius.julius.service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.FileExistsException;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.julius.julius.DTO.request.postDTO.PostRequestDTO;
import com.julius.julius.DTO.response.postDTO.PostResponseDTO;
import com.julius.julius.models.Post;
import com.julius.julius.models.Produto;
import com.julius.julius.repository.PostRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class PostService {
    
    private final PostRepository postRepository;

    private static final String UPLOAD_DIR = "/uploads/post";

    public List<PostResponseDTO> listarPosts(){
        return postRepository.findAll().stream().map(PostResponseDTO::toResponse).toList();
    }

    public PostResponseDTO salvarPost(PostRequestDTO postRequestDTO){

        Post post = Post.builder()
                    .titulo(postRequestDTO.titulo())
                    .conteudo(postRequestDTO.conteudo())
                    .urlImagem("")
                    .build();

        return PostResponseDTO.toResponse(postRepository.save(post));
    }

    public void apagarPost(Long id, String urlImagem) throws FileExistsException {

        String caminhoImagem = UPLOAD_DIR + "/" + urlImagem;

        postRepository.deleteById(id);

        File arquivoImagem = new File(caminhoImagem);
        if (arquivoImagem.exists()) {
            arquivoImagem.delete();
        } else {
            throw new FileExistsException("arquivo não encontrado");
        }
    }

    public PostResponseDTO atualizarPost(PostRequestDTO postRequestDTO) {
        
        Optional<Post> post = postRepository.findById(postRequestDTO.id());

        if (post.isPresent()) {
            post.get().setTitulo(postRequestDTO.titulo());
            post.get().setConteudo(postRequestDTO.conteudo());

            return PostResponseDTO.toResponse(postRepository.save(post.get()));
        }

        return null;
    }

    public String salvarImagemPost(MultipartFile file, Long id) throws FileUploadException {

        Optional<Post> post = postRepository.findById(id);

        try {
            File uploadsDir = new File(UPLOAD_DIR);
            if (!uploadsDir.exists()) {
                uploadsDir.mkdirs();
            }

            Date data = new Date();

            String fileName = file.getOriginalFilename();
            String nomeImagem = data.getTime() + fileName;
            Path filePath = Path.of(uploadsDir.getAbsolutePath(), nomeImagem);

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            String imagemUrl = uploadsDir.getAbsolutePath() + fileName;

            post.get().setUrlImagem(nomeImagem);

            postRepository.save(post.get());

            return imagemUrl;
        } catch (Exception e) {
            throw new FileUploadException();
        }

    }

    public Resource loadImagemAResource(String imagemNome) {

        try {
            File uploadDir = new File(UPLOAD_DIR);

            Path imagemPath = Paths.get(uploadDir.getAbsolutePath()).resolve(imagemNome);
            Resource resource = new UrlResource(imagemPath.toUri());

            if (resource.exists() || resource.isReadable()) {
                return resource;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public PostResponseDTO pegarPost(Long id) {

        Optional<Post> post = postRepository.findById(id);

        return PostResponseDTO.toResponse(post.get());
    }
}
