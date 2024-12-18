package com.julius.julius.service;

import com.julius.julius.models.Evento;
import com.julius.julius.repository.EventoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EventoService {

    private final EventoRepository eventoRepository;

    public void registrarEvento(String tipoEvento, String detalhes) {
        Evento evento = new Evento();
        evento.setTipoEvento(tipoEvento);
        evento.setDetalhes(detalhes);
        evento.setDataEvento(LocalDateTime.now());
        eventoRepository.save(evento);
    }

    public long contarEventosPorTipo(String tipoEvento) {
        return eventoRepository.findAll()
                .stream()
                .filter(e -> e.getTipoEvento().equals(tipoEvento))
                .count();
    }

    public long contarTotalEventos() {
        return eventoRepository.count();
    }

    public List<Evento> listarTodosEventos() {
        return eventoRepository.findAll();
    }

    public Map<String, Double> calcularPorcentagemCliquesNaoCliques() {
        long totalAcessosOfertas = contarEventosPorTipo("ACESSO_OFERTAS");
        long totalCliquesBotao = contarEventosPorTipo("CLIQUE_BOTAO");

        double porcentagemCliques = 0.0;
        double porcentagemNaoCliques = 0.0;

        if (totalAcessosOfertas > 0) {
            porcentagemCliques = ((double) (totalCliquesBotao * 100) / totalAcessosOfertas);
            porcentagemNaoCliques = 100 - porcentagemCliques; // Complemento
        }

        Map<String, Double> resultado = new HashMap<>();
        resultado.put("porcentagemCliques", porcentagemCliques);
        resultado.put("porcentagemNaoCliques", porcentagemNaoCliques);

        return resultado;
    }

}