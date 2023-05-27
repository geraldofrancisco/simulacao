package com.hackathon.simulacao.repository;

import com.hackathon.simulacao.model.Produto;
import com.hackathon.simulacao.model.dto.MinimoMaximo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {
    @Query("SELECT NEW com.hackathon.simulacao.model.dto.MinimoMaximo(MIN(p.minValor), MAX(p.maxValor)) FROM Produto p")
    MinimoMaximo buscaExtremos();

    @Query("SELECT p FROM Produto p WHERE p.minValor <= ?1 AND p.maxValor >= ?1")
    Optional<Produto> buscaPorValoresMinimoEMaximo(BigDecimal valor);

    @Query("SELECT p FROM Produto p WHERE p.maxValor is null")
    Produto buscaSemValorMaximo();
}
