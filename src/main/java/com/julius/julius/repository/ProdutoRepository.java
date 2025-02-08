package com.julius.julius.repository;

import java.time.LocalDateTime;
import java.util.List;

import com.julius.julius.DTO.response.IProdutoResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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

    @Query(nativeQuery = true, value = """
             SELECT
                        p.produto_id as id, p.titulo,
                        p.copy, p.preco,
                        p.preco_parcelado as parcelado, p.cupom,
                        lp.url as link, p.frete_variacoes as freteVariacoes,
                        p.mensagem_adicional as mensagemAdicional, 
                        p.promocao_encerrada as promocaoEncerrada,
                        p.data_criacao as dataCriacao, p.url_imagem as imagem,
                        p.imagem_social as imagemSocial , lj.url_imagem as imagemLoja,
                        lj.nome_loja as nomeLoja, p.descricao
                    FROM
                        produtos p
                    JOIN
                        produto_link pl
                    ON
                        pl.produto_id = p.produto_id
                    JOIN
                        links_produtos lp
                    ON
                        pl.link_produto_id = lp.id
                    JOIN
                        lojas lj
                    ON
                       p.fk_loja = lj.loja_id
                    WHERE
                        lp.site = :site and lp.url not like '%onelink%'
                    and lower(p.titulo) like lower(concat('%', :termoPesquisa, '%'))
                    ORDER BY p.data_criacao DESC
            """)
    Page<IProdutoResponseDto> listarProdutos(@Param("termoPesquisa") String termoPesquisa, @Param("site") Long site, Pageable pageable);

    @Query(value = """
            select
            	p.produto_id as id, p.titulo,
                p.copy, p.preco,
                p.preco_parcelado, p.cupom,
                lp.url as link, p.frete_variacoes as freteVariacoes,
                p.mensagem_adicional, p.promocao_encerrada as promocaoEncerrada,
                p.data_criacao as dataCriacao, p.url_imagem as imagem,
                p.imagem_social as imagemSocial , lj.url_imagem as imagemLoja,
                lj.nome_loja as nomeLoja, p.descricao
            from
            	produtos p
            join
            	produto_link pl
            on
            	pl.produto_id = p.produto_id
            join
            	links_produtos lp
            on
            	lp.id = pl.link_produto_id
            join
                lojas lj
            on
                p.fk_loja = lj.loja_id
            where
            	p.fk_categoria = :categoriaId and lp.site = :site
            ORDER BY p.data_criacao DESC
            """, nativeQuery = true)
    Page<IProdutoResponseDto> buscarProdutosPorCategoria(@Param("site") Long site,@Param("categoriaId") Long categoriaId, Pageable pageable);

    @Query(value = """
            SELECT
                p.*
            FROM produtos p
            JOIN produto_link pl ON p.produto_id = pl.produto_id
            JOIN links_produtos lp ON pl.link_produto_id = lp.id
            WHERE p.fk_loja = :lojaId AND
            lp.site = :site
            ORDER BY p.data_criacao DESC
    """, nativeQuery = true)
    Page<Produto> buscarProdutosPorLoja(@Param("lojaId") Long lojaId, @Param("site") Long site, Pageable pageable);

//    @Query("SELECT new com.julius.julius.DTO.response.ProdutoPesquisa(" +
//            "p.id, p.titulo, p.copy, p.preco, p.precoParcelado, " +
//            "p.cupom, pl.url, p.freteVariacoes, p.mensagemAdicional, p.promocaoEncerrada, " +
//            "p.dataCriacao, p.urlImagem, p.imagemSocial ,l.urlImagem, l.nomeLoja, p.descricao) " +
//            "FROM Produto p " +
//            "JOIN linksProdutos pl " +
//            "JOIN p.loja l " +
//            "WHERE LOWER(p.titulo) LIKE LOWER(CONCAT('%', :termoPesquisa, '%')) AND pl.site = :site " +
//            "ORDER BY p.dataCriacao DESC")
//    Page<IProdutoResponseDto> procurarProdutos(@Param("termoPesquisa") String termoPesquisa, @Param("site") Long site,
//                                           Pageable pageable);

    @Query("SELECT p FROM Produto p WHERE p.dataCriacao <= :dataLimite")
    List<Produto> findProdutosComMaisDe7Dias(LocalDateTime dataLimite);

//    @Query(value = """
//            SELECT l.url FROM produtos p JOIN produto_link pl ON p.produto_id = pl.produto_id
//            JOIN links_Produtos l ON pl.link_produto_id = l.id
//            WHERE p.produto_id = :produtoId AND l.site = :site
//     """, nativeQuery = true)
//    String sfindByProdutoBySite(@Param("produtoId") Long produtoId, @Param("site") Long site);



    @Query(value = "SELECT * FROM produtos WHERE fk_categoria = :categoriaId", nativeQuery = true)
    List<Produto> findByProdutoPorCategoriaId(@Param("categoriaId") Long categoriaId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM produtos_promo WHERE produto_id = :idProduto", nativeQuery = true)
    void deleteByProdutoPromos(@Param("idProduto") Long idProduto);

    @Query(value = """
            SELECT
              p.produto_id as id, p.titulo,
                p.copy, p.preco,
                p.preco_parcelado, p.cupom,
                lp.url as link, p.frete_variacoes as freteVariacoes,
                p.mensagem_adicional, p.promocao_encerrada as promocaoEncerrada,
                p.data_criacao as dataCriacao, p.url_imagem as imagem,
                p.imagem_social as imagemSocial , lj.url_imagem as imagemLoja,
                lj.nome_loja as nomeLoja, p.descricao
            FROM produtos p
            JOIN produto_link pl ON p.produto_id = pl.produto_id
            JOIN links_produtos lp ON pl.link_produto_id = lp.id
            join lojas lj on p.fk_loja = lj.loja_id
            WHERE p.data_criacao >= CURRENT_TIMESTAMP - INTERVAL '2 HOURS' 
            AND lp.site = :site ORDER BY p.data_criacao DESC
    """, nativeQuery = true)
    Page<IProdutoResponseDto> listarProdutosDestaque(@Param("site") Long site,Pageable pageable);

    long countByPromosId(Long promoId);

    boolean existsByTitulo(String titulo);
}
