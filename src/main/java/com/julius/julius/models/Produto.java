package com.julius.julius.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "produtos")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Produto {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "produto_id")
    private Long id;

    @Column(name = "titulo")
    private String titulo;

    @Column(name = "preco")
    private String preco;

    @Column(name = "descricao")
    private String descricao;

    @Column(name = "link")
    private String link;

    @Column(name = "cupom")
    private String cupom;

    @Column(name = "titulopequeno")
    private String tituloPequeno;

    @Column(name = "url_imagem")
    private byte[] imagem;

    @OneToMany(mappedBy = "produto")
    private List<Report> reports;

    @ManyToOne
    @JoinColumn(name = "fk_categoria")
    private Categoria categoria;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "produtos_loja", 
                joinColumns = @JoinColumn(name = "fk_produto"),
                inverseJoinColumns = @JoinColumn(name = "fk_loja")
                )
    private List<Loja> lojas = new ArrayList<>(); 

    @Column(name =  "data_criacao", updatable=false)
    @CreationTimestamp
    private Date dataCriacao;

    @Column(name = "data_atualizacao")
    @UpdateTimestamp
    private Date dataAtualizacao;

}
