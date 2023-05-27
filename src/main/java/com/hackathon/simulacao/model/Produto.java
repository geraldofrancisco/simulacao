package com.hackathon.simulacao.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.math.BigDecimal;

@Table(name = "PRODUTO", schema = "dbo")
@Entity
@Data
public class Produto {
    @Id
    @Column(name = "CO_PRODUTO")
    private Long codigo;

    @Column(name = "NO_PRODUTO")
    private String nome;

    @Column(name = "PC_TAXA_JUROS")
    private BigDecimal taxaJuros;

    @Column(name = "NU_MINIMO_MESES")
    private Integer minMeses;

    @Column(name = "NU_MAXIMO_MESES")
    private Integer maxMeses;

    @Column(name = "VR_MINIMO")
    private BigDecimal minValor;

    @Column(name = "VR_MAXIMO")
    private BigDecimal maxValor;
}
