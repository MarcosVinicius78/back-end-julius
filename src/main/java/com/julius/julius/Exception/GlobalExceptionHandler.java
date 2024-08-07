package com.julius.julius.Exception;

import java.awt.FontFormatException;

import javax.imageio.IIOException;

import org.apache.commons.io.FileExistsException;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import com.github.dockerjava.api.exception.NotFoundException;

import jakarta.validation.UnexpectedTypeException;
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationExceptions(MethodArgumentNotValidException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getBindingResult().getAllErrors());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest().body("O corpo da requisição está ausente ou não está no formato esperado.");
    }
    
    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<String> missingServletRequestPartException(MissingServletRequestPartException ex) {
        return ResponseEntity.badRequest().body("O não pode estar vazio.");
    }
    
    @ExceptionHandler(FileExistsException.class)
    public ResponseEntity<String> fileExistsException(FileExistsException ex) {
        return ResponseEntity.badRequest().body("Imagem não existe.");
    }
    
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<String> notFoundException(NotFoundException ex) {
        return ResponseEntity.badRequest().body("Erro ao acessar url.");
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> illegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body("Não pode ter parametro null.");
    }
    
    @ExceptionHandler(FileUploadException.class)
    public ResponseEntity<String> fileUploadException(FileUploadException ex) {
        return ResponseEntity.badRequest().body("Não pode ter parametro null.");
    }
    
    @ExceptionHandler(UnexpectedTypeException.class)
    public ResponseEntity<String> uexpectedTypeException(UnexpectedTypeException ex) {
        return ResponseEntity.badRequest().body("Não pode ter parametro null.");
    }
    
    @ExceptionHandler(IndexOutOfBoundsException.class)
    public ResponseEntity<String> indexOutOfBoundsException(IndexOutOfBoundsException ex) {
        return ResponseEntity.badRequest().body("Não pode ter parametro null.");
    }
    
    @ExceptionHandler(FontFormatException.class)
    public ResponseEntity<String> fontFormatException(FontFormatException ex) {
        return ResponseEntity.badRequest().body("Font não encontrada.");
    }
    
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<String> NullPointerException(NullPointerException ex) {
        return ResponseEntity.badRequest().body("ERRO.");
    }

}
