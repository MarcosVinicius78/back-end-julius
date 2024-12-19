package com.julius.julius.repository;

import com.julius.julius.models.Evento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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


}