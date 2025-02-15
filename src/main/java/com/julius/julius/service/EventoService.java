package com.julius.julius.service;

import com.julius.julius.DTO.ProdutosCliquesDto;
import com.julius.julius.DTO.evento.EventoQuantidadePorTipo;
import com.julius.julius.DTO.evento.TotalDeAcessosPorCategoria;
import com.julius.julius.DTO.evento.TotalDeAcessosPorLoja;
import com.julius.julius.DTO.evento.TotalDeEventosDto;
import com.julius.julius.models.Evento;
import com.julius.julius.models.Produto;
import com.julius.julius.repository.EventoRepository;
import com.julius.julius.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    private final ProdutoRepository produtoRepository;

    public void registrarEvento(String tipoEvento, String detalhes) {
        Evento evento = new Evento();
        evento.setTipoEvento(tipoEvento);
        evento.setDetalhes(detalhes);
        evento.setDataEvento(LocalDateTime.now());
        eventoRepository.save(evento);
    }

    public Page<ProdutosCliquesDto> listarProdutosComMaisCliques(Pageable pageable) {
        return eventoRepository.listarProdutosComMaisAcessos(pageable);
    }

    public void registrarEventoDoProduto(Long id, String tipoEvento, String detalhes) {

        Produto produto = produtoRepository.findById(id).orElseThrow(() -> new RuntimeException("Produto nao encontrado"));

        Evento evento = new Evento();
        evento.setTipoEvento(tipoEvento);
        evento.setDetalhes(detalhes);
        evento.setDataEvento(LocalDateTime.now());
        evento.setProduto(produto);

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

    public EventoQuantidadePorTipo buscarEventosPorDia(LocalDate dataSelecionada, String tipoEvento) {

        if (dataSelecionada == null) {
            dataSelecionada = LocalDate.now();
        }

        EventoQuantidadePorTipo dados = eventoRepository.contarEventosPorTipo(dataSelecionada, tipoEvento);

        return dados;
    }

    public TotalDeEventosDto totalDeAcessos() {
        return eventoRepository.totalDeAcessosNoSistema();
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

    public List<TotalDeAcessosPorCategoria> totalDeAcessosPorCategoria() {
        return eventoRepository.totalDeAcessosPorCategoria();
    }

    public List<TotalDeAcessosPorLoja> totalDeAcessosPorLoja() {
        return eventoRepository.totalDeAcessosPorLoja();
    }

}