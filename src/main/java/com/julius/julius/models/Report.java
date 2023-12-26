package com.julius.julius.models;

import java.util.Date;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "Reports")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reports")
    private Long id;

    @Column(name = "total")
    private Long total;

    @Column(name = "tipo")
    private String tipo;

    @ManyToOne
    @JoinColumn(name = "fk_produtos")
    private Produto produto;

    @Column(name =  "data_criacao", updatable=false)
    @CreationTimestamp
    private Date dataCriacao;

    @Column(name = "data_atualizacao")
    @UpdateTimestamp
    private Date dataAtualizacao;

}