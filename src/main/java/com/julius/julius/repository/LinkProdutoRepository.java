package com.julius.julius.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.julius.julius.models.LinksProdutos;

import jakarta.transaction.Transactional;

public interface LinkProdutoRepository extends JpaRepository<LinksProdutos, Long> {

    @Modifying
    @Transactional
    @Query(value = "UPDATE links_produtos SET url = :novaUrl WHERE id IN (SELECT lp.id FROM links_produtos lp JOIN produto_link pl ON lp.id = pl.link_produto_id WHERE pl.produto_id = :produtoId)", nativeQuery = true)
    void atualizarUrlsPorProduto(@Param("produtoId") Long produtoId, @Param("novaUrl") String novaUrl);

    @Modifying
    @Transactional
    @Query(value = "UPDATE links_produtos SET url = :urlSe WHERE site = :site AND id = :id", nativeQuery = true)
    void atualizarUrlSe(@Param("urlSe") String url, @Param("site") Long site, @Param("id") Long id);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM produto_link WHERE link_produto_id = :id", nativeQuery = true)
    void deletarChaveEstrangeiraLink(@Param("id") Long id);
}
