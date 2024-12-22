package com.julius.julius.service;

import com.julius.julius.models.Evento;
import com.julius.julius.repository.EventoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
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

    public Map<String, Long> contarAcessosPorPeriodo(String tipoEvento, LocalDateTime inicioSemana, LocalDateTime fimSemana) {
        // Busca os dados do repositório
        List<Object[]> resultados = eventoRepository.contarAcessosPorDiaSemana(tipoEvento, inicioSemana, fimSemana);

        // Processa os resultados em um mapa
        Map<String, Long> acessosPorDia = new HashMap<>();
        for (Object[] resultado : resultados) {
            String diaSemana = ((String) resultado[0]).trim();
            Long total = (Long) resultado[1];
            acessosPorDia.put(diaSemana, total);
        }

        return acessosPorDia;
    }

    public Map<String, Long> buscarEventosPorDia(LocalDate dataSelecionada) {
        // Gera o início do dia selecionado
        LocalDateTime dataInicio = dataSelecionada.atStartOfDay();

        List<Object[]> resultados = eventoRepository.contarEventosPorTipo(dataInicio);

        Map<String, Long> contagemEventos = new HashMap<>();
        for (Object[] resultado : resultados) {
            String tipoEvento = (String) resultado[0];
            Long contagem = (Long) resultado[1];
            contagemEventos.put(tipoEvento, contagem);
        }

        return contagemEventos;
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