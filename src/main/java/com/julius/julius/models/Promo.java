package com.julius.julius.models;

import java.util.Date;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "promo")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Promo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "promo_id")
    private Long id;

    @Column(name = "copy_promo")
    private String copyPromo;

    @Column(name = "url_imagem")
    private String urlImagem;

    @Column(name = "data_criacao", updatable = false)
    @CreationTimestamp
    private Date dataCriacao;

    @Column(name = "data_atualizacao")
    @UpdateTimestamp
    private Date dataAtualizacao;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "produtos_promo", joinColumns = @JoinColumn(name = "promo_id"), inverseJoinColumns = @JoinColumn(name = "produto_id"))
    private List<Produto> produtos;
}
