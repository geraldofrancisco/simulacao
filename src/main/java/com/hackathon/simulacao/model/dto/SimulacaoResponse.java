package com.hackathon.simulacao.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimulacaoResponse {
    @Schema(description = "código do produto")
    private Long codigoProduto;

    @Schema(description = "descrição do produto")
    private String descricaoProduto;

    @Schema(description = "taxa de juros do produto")
    private BigDecimal taxaJuros;

    @Schema(description = "resultado das simulações de empréstimos")
    private List<SimulacaoResultado> resultadoSimulacao;
}
