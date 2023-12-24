package com.julius.julius.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.julius.julius.DTO.LojaSalvarDto;
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
    public void cadastrarLoja(@RequestBody LojaSalvarDto loja){

        lojaService.salvarLoja(loja);
    }

    // @GetMapping
    // public ResponseEntity<Resource> listarLojas(){
    //     return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(this.lojaService.listarLojas());
    // }

    @GetMapping
    public ResponseEntity<List<LojaResponseDto>> listarLojas(){
        return ResponseEntity.ok().body(this.lojaService.listarLojas());
    }
}
