package com.julius.julius.controller;

import com.julius.julius.service.EventoService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/eventos")
public class EventoController {

    private final EventoService eventoService;

    @PostMapping("/registrar")
    public ResponseEntity<String> registrarEvento(@RequestParam String tipo, @RequestParam(required = false) String detalhes) {
        eventoService.registrarEvento(tipo, detalhes);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/acessos-semana")
    public ResponseEntity<Map<String, Long>> contarAcessos(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicioSemana,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fimSemana) {

        Map<String, Long> acessos = eventoService.contarAcessosPorPeriodo("ACESSO_SISTEMA", inicioSemana, fimSemana);
        return ResponseEntity.ok().body(acessos);
    }

    @GetMapping("/buscar-por-dia")
    public ResponseEntity<Map<String, Long>> buscarEventosPorDia(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return ResponseEntity.ok().body(eventoService.buscarEventosPorDia(data));
    }

    @GetMapping("/estatisticas")
    public ResponseEntity<Map<String, Long>> obterEstatisticas() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("totalAcessosSistema", eventoService.contarEventosPorTipo("ACESSO_SISTEMA"));
        stats.put("totalCliquesBotao", eventoService.contarEventosPorTipo("CLIQUE_BOTAO"));
        stats.put("totalAcessosOfertas", eventoService.contarEventosPorTipo("ACESSO_OFERTAS"));
        stats.put("totalEventos", eventoService.contarTotalEventos());

        return ResponseEntity.ok().body(stats);
    }

    @GetMapping("/porcentagem-cliques-nao-cliques")
    public ResponseEntity<Map<String, Double>> obterPorcentagemCliquesNaoCliques() {
        return ResponseEntity.ok().body(eventoService.calcularPorcentagemCliquesNaoCliques());
    }

}