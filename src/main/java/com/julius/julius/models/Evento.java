package com.julius.julius.models;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "eventos")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Evento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tipo_evento")
    private String tipoEvento;

    @Column(name = "detalhes")
    private String detalhes;

    @Column(name = "data_evento")
    private LocalDateTime dataEvento;

    @ManyToOne
    @JoinColumn(name = "produto_id")
    private Produto produto;
}
