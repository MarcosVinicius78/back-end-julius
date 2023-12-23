package com.julius.julius.models;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "lojas")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Loja {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "loja_id")
    private Long id;

    @Column(name = "nome_loja")
    private String nome_loja;
 
    @Column(name = "imagem")
    private byte[] imagem;

    @ManyToMany(mappedBy = "lojas", fetch = FetchType.EAGER)
    private List<Produto> produtos = new ArrayList<>();
}
