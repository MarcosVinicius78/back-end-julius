package com.julius.julius.models;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "links_produtos")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class LinksProdutos {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "url")
    private String url;

    @Column(name = "site")
    private Long site;

    @JsonBackReference
    @ManyToMany(mappedBy = "linksProdutos", cascade = CascadeType.REMOVE)
    private List<Produto> produtos = new ArrayList<>();
}
