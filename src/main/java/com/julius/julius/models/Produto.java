package com.julius.julius.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonManagedReference;

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
import jakarta.persistence.PrePersist;
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

    @Column(name = "copy")
    private String copy;

    @Column(name = "preco")
    private String preco;

    @Column(name = "preco_parcelado")
    private String precoParcelado;

    @Column(name = "descricao")
    private String descricao;

    @Column(name = "cupom")
    private String cupom;

    @Column(name = "frete_variacoes")
    private String freteVariacoes;

    @Column(name = "link")
    private String link;

    @Column(name = "mensagem_adicional")
    private String mensagemAdicional;

    @Column(name = "url_imagem", nullable = true)
    private String urlImagem;

    @Column(name = "imagem_social", nullable = true)
    private String imagemSocial;

    @OneToMany(mappedBy = "produto")
    private List<Report> reports;

    @ManyToOne
    @JoinColumn(name = "fk_categoria")
    private Categoria categoria;

    @ManyToOne
    @JoinColumn(name = "fk_loja")
    private Loja loja;

    @Column(name = "promocao_encerrada")
    private Boolean promocaoEncerrada;

    @JsonManagedReference
    @ManyToMany
    @JoinTable(name = "produto_link", joinColumns = @JoinColumn(name = "produto_id"), inverseJoinColumns = @JoinColumn(name = "link_produto_id"))
    private List<LinksProdutos> linksProdutos = new ArrayList<>();

    @ManyToMany(mappedBy = "produtos", fetch = FetchType.EAGER)
    private List<Promo> promos;

    @Column(name = "data_criacao", updatable = false)
    @CreationTimestamp
    private Date dataCriacao;

    @Column(name = "data_atualizacao")
    @UpdateTimestamp
    private Date dataAtualizacao;

    @PrePersist
    public void prePersiste() {
        if (promocaoEncerrada == null) {
            promocaoEncerrada = false;
        }
    }

    public Produto duplicar() {
        Produto copia = Produto.builder()
                .titulo(this.titulo)
                .copy(this.copy)
                .preco(this.preco)
                .precoParcelado(this.precoParcelado)
                .descricao(this.descricao)
                .cupom(this.cupom)
                .freteVariacoes(this.freteVariacoes)
                .link(this.link)
                .mensagemAdicional(this.mensagemAdicional)
                .urlImagem(this.urlImagem)
                .imagemSocial(this.imagemSocial)
                .categoria(this.categoria) // Assumindo que você quer copiar a referência para a mesma categoria
                .loja(this.loja) // Assumindo que você quer copiar a referência para a mesma loja
                .promocaoEncerrada(this.promocaoEncerrada)
                .dataCriacao(this.dataCriacao) // Pode ou não querer duplicar esta data
                .dataAtualizacao(this.dataAtualizacao) // Pode ou não querer duplicar esta data
                .build();
    
        // Fazendo uma cópia das listas para evitar compartilhamento de referências
        if (this.reports != null) {
            copia.setReports(new ArrayList<>(this.reports));
        }
    
        if (this.linksProdutos != null) {
            copia.setLinksProdutos(new ArrayList<>(this.linksProdutos));
        }
    
        if (this.promos != null) {
            copia.setPromos(new ArrayList<>(this.promos));
        }
    
        return copia;
    }
    
}
