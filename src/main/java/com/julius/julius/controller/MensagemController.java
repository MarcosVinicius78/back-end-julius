package com.julius.julius.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.julius.julius.DTO.ProdutoSalvarDto;
import com.julius.julius.DTO.request.MensagemRequest;
import com.julius.julius.service.TelegramService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/mensagem")
@RequiredArgsConstructor
public class MensagemController {
    
    private final TelegramService telegramService;

    @PostMapping("/telegram")
    public ResponseEntity<?> enviarTelegram(@RequestBody MensagemRequest mensagem ) {
        telegramService.enviarProdutoParaTelegram(mensagem);
        return ResponseEntity.ok().build();
    }

}
