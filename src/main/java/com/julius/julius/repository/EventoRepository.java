package com.julius.julius.repository;

import com.julius.julius.DTO.ProdutosCliquesDto;
import com.julius.julius.DTO.evento.EventoQuantidadePorTipo;
import com.julius.julius.DTO.evento.TotalDeAcessosPorCategoria;
import com.julius.julius.DTO.evento.TotalDeAcessosPorLoja;
import com.julius.julius.DTO.evento.TotalDeEventosDto;
import com.julius.julius.models.Evento;
import org.springframework.cglib.core.Local;
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
            WHERE DATE(e.data_evento) = TO_DATE(:data, 'YYYY-MM-DD')
            AND (:termo IS NULL OR lower(p.titulo) like lower(concat('%', :termo, '%')))
            GROUP BY p.produto_id , p.titulo
            ORDER BY totalEventos DESC;
    """)
    Page<ProdutosCliquesDto> listarProdutosComMaisAcessos(@Param("termo") String termo, @Param("data") LocalDate data, Pageable pageable);

    @Query(nativeQuery = true ,value = """
        select
        	 count(*) as totalDeEventos,
        	 MIN(e.data_evento) as primeiroAcesso
        from
        	eventos e
        where
        	e.tipo_evento like '%ACESSO_SISTEMA%'
    """)
    TotalDeEventosDto totalDeAcessosNoSistema();

    @Query(nativeQuery = true ,value = """
        SELECT
            c.categoria_id as categoriaId,
        	c.nome_categoria as nomeCategoria,
        	COUNT(e.id) as totalAcessos
        FROM eventos e
        JOIN produtos p ON e.produto_id = p.produto_id
        JOIN categorias c ON p.fk_categoria = c.categoria_id
        WHERE e.tipo_evento = 'ACESSO_PRODUTO' and DATE(e.data_evento) = TO_DATE(:data, 'YYYY-MM-DD')
        GROUP BY c.nome_categoria, c.categoria_id
        ORDER BY totalAcessos DESC
    """)
    List<TotalDeAcessosPorCategoria> totalDeAcessosPorCategoria(@Param("data")LocalDate data);

    @Query(nativeQuery = true, value = """
        SELECT
        	l.nome_loja as nomeLoja,
        	COUNT(e.id) as totalAcessos
        FROM eventos e
        JOIN produtos p ON e.produto_id = p.produto_id
        JOIN lojas l ON p.fk_loja = l.loja_id
        WHERE e.tipo_evento = 'ACESSO_PRODUTO' and DATE(e.data_evento) = TO_DATE(:data, 'YYYY-MM-DD')
        GROUP BY l.nome_loja
        ORDER BY totalAcessos desc;
    """)
    List<TotalDeAcessosPorLoja> totalDeAcessosPorLoja(@Param("data") LocalDate data);
}