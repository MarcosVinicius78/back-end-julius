package com.julius.julius.controller;

import com.julius.julius.DTO.ProdutosCliquesDto;
import com.julius.julius.DTO.evento.EventoQuantidadePorTipo;
import com.julius.julius.DTO.evento.TotalDeAcessosPorCategoria;
import com.julius.julius.DTO.evento.TotalDeAcessosPorLoja;
import com.julius.julius.DTO.evento.TotalDeEventosDto;
import com.julius.julius.service.EventoService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
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

    @PostMapping("/registrar-produto/{id}")
    public ResponseEntity<String> registrarEventoDoProduto(@PathVariable Long id, @RequestParam String tipo, @RequestParam(required = false) String detalhes) {
        eventoService.registrarEventoDoProduto(id, tipo, detalhes);
        return ResponseEntity.ok().build();
    }

    //usando
    @GetMapping("/listar-produtos-com-mais-cliques")
    public ResponseEntity<Page<ProdutosCliquesDto>> listarProdutosComMaisCliques(@RequestParam(required = false) String termo,
                                                                                 @RequestParam LocalDate data) {
        Pageable pageable = PageRequest.of(0, 100);
        System.out.println(data);
        return ResponseEntity.ok().body(eventoService.listarProdutosComMaisCliques(termo ,data, pageable));
    }

    // usando
    @GetMapping("/acessos-semana")
    public ResponseEntity<Map<String, Long>> contarAcessos(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicioSemana,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fimSemana) {

        Map<String, Long> acessos = eventoService.contarAcessosPorPeriodo("ACESSO_SISTEMA", inicioSemana, fimSemana);
        return ResponseEntity.ok().body(acessos);
    }

    //usando
    @GetMapping("/buscar-por-dia")
    public ResponseEntity<EventoQuantidadePorTipo> buscarEventosPorDia(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data, @RequestParam String tipoEvento) {
        return ResponseEntity.ok().body(eventoService.buscarEventosPorDia(data, tipoEvento));
    }

    //usando
    @GetMapping("/total-de-acessos")
    public ResponseEntity<TotalDeEventosDto> totalDeAcessos() {
        return ResponseEntity.ok().body(eventoService.totalDeAcessos());
    }

    //sando
    @GetMapping("/total-de-acessos-por-categoria")
    public ResponseEntity<List<TotalDeAcessosPorCategoria>> totalDeAcessosPorCategoria(@RequestParam LocalDate data) {
        return ResponseEntity.ok().body(eventoService.totalDeAcessosPorCategoria(data));
    }

    //usando
    @GetMapping("/total-de-acessos-por-loja")
    public ResponseEntity<List<TotalDeAcessosPorLoja>> totalDeAcessosPorLoja(@RequestParam LocalDate data) {
        return ResponseEntity.ok().body(eventoService.totalDeAcessosPorLoja(data));
    }
}