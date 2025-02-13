package com.julius.julius.repository;

import com.julius.julius.DTO.ProdutosCliquesDto;
import com.julius.julius.DTO.evento.EventoQuantidadePorTipo;
import com.julius.julius.models.Evento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface EventoRepository extends JpaRepository<Evento, Long> {

    @Query(value = """
            SELECT TO_CHAR(e1_0.data_evento, 'Day') AS dia_semana, COUNT(e1_0.id) AS total
        FROM
            eventos e1_0
            WHERE e1_0.tipo_evento = :tipoEvento
          AND e1_0.data_evento >= :inicioSemana
          AND e1_0.data_evento <= :fimSemana
            GROUP BY TO_CHAR(e1_0.data_evento, 'Day')        
                   """, nativeQuery = true)
    List<Object[]> contarAcessosPorDiaSemana(@Param("tipoEvento") String tipoEvento,
                                             @Param("inicioSemana") LocalDateTime inicioSemana,
                                             @Param("fimSemana") LocalDateTime fimSemana);

    @Query(nativeQuery = true, value = """
        SELECT
            SUM(CASE WHEN DATE(e.data_evento) = TO_DATE(:dataEspecifica, 'YYYY-MM-DD') THEN 1 ELSE 0 END) AS quantidadeNoDia,
            SUM(CASE WHEN DATE(e.data_evento) = TO_DATE(:dataEspecifica, 'YYYY-MM-DD') - INTERVAL '1 day' THEN 1 ELSE 0 END) AS quantidadeDiaAnterior
        FROM eventos e
        WHERE e.tipo_evento LIKE :tipoEvento
    """)
    EventoQuantidadePorTipo contarEventosPorTipo(@Param("dataEspecifica") LocalDate dataEspecifica, @Param("tipoEvento") String tipoEvento);

    @Query(nativeQuery = true, value = """
            SELECT
                p.url_imagem as urlImagem,
                p.produto_id AS id,
                p.titulo AS nomeProduto,
                COUNT(e.id) AS totalEventos
            FROM eventos e
            JOIN produtos p ON e.produto_id = p.produto_id
            GROUP BY p.produto_id , p.titulo
            ORDER BY totalEventos DESC;
    """)
    Page<ProdutosCliquesDto> listarProdutosComMaisAcessos(Pageable pageable);
}