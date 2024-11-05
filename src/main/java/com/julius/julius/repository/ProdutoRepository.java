package com.julius.julius.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.julius.julius.DTO.response.ProdutoPesquisa;
import com.julius.julius.models.Produto;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {

        void deleteByIdIn(List<Long> prod);

        // @Query("SELECT p FROM Produto p ORDER BY p.dataCriacao DESC")
        // Page<Produto> findFirstByOrderByDataCadastroDesc(Pageable pageable);

        // @Query("SELECT DISTINCT p FROM Produto p JOIN p.linksProdutos lp WHERE lp.site = :site ORDER BY p.dataCriacao DESC")
        // Page<Produto> findProdutosOfm(@Param("site") Long site, Pageable pageable);

        // @Query("SELECT DISTINCT p FROM Produto p JOIN p.linksProdutos lp WHERE
        // lp.site != :site ORDER BY p.dataCriacao DESC")
        // Page<Produto> dsfindFirstByOrderByDataCadastro(@Param("site") Long
        // site,Pageable pageable);

        @Query(value = "SELECT new com.julius.julius.DTO.response.ProdutoPesquisa(" +
                        "p.id, p.titulo, p.copy, p.preco, p.precoParcelado, " +
                        "p.cupom, pl.url, p.freteVariacoes, p.mensagemAdicional, p.promocaoEncerrada , " + 
                        "p.dataCriacao, p.urlImagem, p.imagemSocial, l.urlImagem, l.nomeLoja, p.descricao) " +
                        "FROM Produto p " +
                        "JOIN linksProdutos pl " +
                        "JOIN p.loja l " +
                        "WHERE pl.site = :site "+
                        "ORDER BY p.dataCriacao DESC")
        Page<ProdutoPesquisa> listarProdutos(@Param("site") Long site, Pageable pageable);

        // @Query(value = "SELECT * FROM produtos p WHERE p.produto_id IN (SELECT
        // pl.produto_id FROM produto_link pl JOIN links_produtos lp ON
        // pl.link_produto_id = lp.id WHERE lp.site = 1) OR p.produto_id NOT IN (SELECT
        // pl.produto_id FROM produto_link pl JOIN links_produtos lp ON
        // pl.link_produto_id = lp.id WHERE lp.site = 2) ORDER BY p.data_criacao DESC",
        // nativeQuery = true)
        // Page<Produto> findProdutosSe(@Param("site") Long site, Pageable pageable);

        // @Query(value = "SELECT * FROM produtos p WHERE p.produto_id NOT IN (SELECT pl.produto_id FROM produto_link pl JOIN links_produtos lp ON pl.link_produto_id = lp.id WHERE lp.site = :site)", nativeQuery = true)
        // Page<Produto> findAllExcludingSiteTwo(@Param("site") Long site, Pageable pageable);

        @Query(value = "SELECT p.* FROM produtos p " +
                        "LEFT JOIN produto_link pl ON p.produto_id = pl.produto_id " +
                        "LEFT JOIN links_produtos lp ON pl.link_produto_id = lp.id " +
                        "WHERE p.fk_categoria = :categoriaId AND " +
                        "(lp.site = 1 OR lp.site IS NULL OR p.produto_id NOT IN (" +
                        "    SELECT pl.produto_id FROM produto_link pl " +
                        "    JOIN links_produtos lp ON pl.link_produto_id = lp.id " +
                        "    WHERE lp.site = 2" +
                        ")) " +
                        "ORDER BY p.data_criacao DESC", nativeQuery = true)
        Page<Produto> findByCategoriIdOrderByDataCriacaoDesc(Long categoriaId, Pageable pageable);

        @Query(value = "SELECT p.* FROM produtos p " +
                        "JOIN produto_link pl ON p.produto_id = pl.produto_id " +
                        "JOIN links_produtos lp ON pl.link_produto_id = lp.id " +
                        "WHERE p.fk_categoria = :categoriaId " +
                        "AND p.produto_id IN (" +
                        "    SELECT DISTINCT pc.produto_id FROM produtos pc " +
                        "    JOIN produto_link pcl ON pc.produto_id = pcl.produto_id " +
                        "    JOIN links_produtos lpc ON pcl.link_produto_id = lpc.id " +
                        "    WHERE lpc.site = 2" +
                        ") " +
                        "ORDER BY p.data_criacao DESC", nativeQuery = true)
        Page<Produto> findCategoriIdOrderByDataCriacaoDesc(Long categoriaId, Pageable pageable);

        @Query("SELECT new com.julius.julius.DTO.response.ProdutoPesquisa(" +
                        "p.id, p.titulo, p.copy, p.preco, p.precoParcelado, " +
                        "p.cupom, pl.url, p.freteVariacoes, p.mensagemAdicional, p.promocaoEncerrada, " +
                        "p.dataCriacao, p.urlImagem, p.imagemSocial ,l.urlImagem, l.nomeLoja, p.descricao) " +
                        "FROM Produto p " +
                        "JOIN linksProdutos pl " +
                        "JOIN p.loja l " +
                        "WHERE LOWER(p.titulo) LIKE LOWER(CONCAT('%', :termoPesquisa, '%')) AND pl.site = :site " +
                        "ORDER BY p.dataCriacao DESC")
        Page<ProdutoPesquisa> procurarProdutos(@Param("termoPesquisa") String termoPesquisa, @Param("site") Long site,
                        Pageable pageable);

        // @Query(value = "SELECT DISTINCT p.* FROM produtos p " +
        // "JOIN produto_link pl ON p.produto_id = pl.produto_id " +
        // "JOIN links_produtos lp ON pl.link_produto_id = lp.id " +
        // "WHERE LOWER(p.titulo) LIKE LOWER(CONCAT('%', :termoPesquisa, '%')) " +
        // "AND lp.site = :site " +
        // "ORDER BY p.data_criacao DESC",
        // countQuery = "SELECT COUNT(DISTINCT p.produto_id) FROM produtos p " +
        // "JOIN produto_link pl ON p.produto_id = pl.produto_id " +
        // "JOIN links_produtos lp ON pl.link_produto_id = lp.id " +
        // "WHERE LOWER(p.titulo) LIKE LOWER(CONCAT('%', :termoPesquisa, '%')) " +
        // "AND lp.site = :site",
        // nativeQuery = true)
        // Page<Produto> procurarProdutos(
        // @Param("termoPesquisa") String termoPesquisa, @Param("site") Long site,
        // Pageable pageable);

        @Query("SELECT p FROM Produto p WHERE p.dataCriacao <= :dataLimite")
        List<Produto> findProdutosComMaisDe7Dias(LocalDateTime dataLimite);

        @Query(value = "SELECT l.url FROM produtos p JOIN produto_link pl ON p.produto_id = pl.produto_id JOIN links_Produtos l ON pl.link_produto_id = l.id WHERE p.produto_id = :produtoId AND l.site = :site", nativeQuery = true)
        String sfindByProdutoBySite(@Param("produtoId") Long produtoId, @Param("site") Long site);

        @Query(value = "SELECT * FROM produtos WHERE fk_categoria = :categoriaId", nativeQuery = true)
        List<Produto> findByProdutoPorCategoriaId(@Param("categoriaId") Long categoriaId);

        @Modifying
        @Transactional
        @Query(value = "DELETE FROM produtos_promo WHERE produto_id = :idProduto", nativeQuery = true)
        void deleteByProdutoPromos(@Param("idProduto") Long idProduto);

        @Query(value = "SELECT p.* FROM produtos p WHERE p.data_criacao >= CURRENT_DATE - INTERVAL '7 days' ORDER BY EXTRACT(EPOCH FROM (CURRENT_DATE - p.data_criacao)) DESC, produto_id", nativeQuery = true)
        Page<Produto> listarProdutosDestaque(Pageable pageable);

        long countByPromosId(Long promoId);

        boolean existsByTitulo(String titulo);
}
