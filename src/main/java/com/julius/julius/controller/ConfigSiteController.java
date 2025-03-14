package com.julius.julius.controller;

import com.julius.julius.service.ConfigSiteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("config-site")
@RequiredArgsConstructor
public class ConfigSiteController {

    private final ConfigSiteService configSiteService;

    @GetMapping("/ativar-link-sem-dominio-omc")
    public ResponseEntity<Void> ativarLinkSemDominioOmc(@RequestParam Boolean valor) {
        configSiteService.mudarSemDominioOmc(valor);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/status-link-sem-dominio-omc")
    public ResponseEntity<Boolean> statusLinkSemDominioOmc() {
        return ResponseEntity.ok().body(configSiteService.buscarLinkSemDominioOmc());
    }
}
