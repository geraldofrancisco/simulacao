package com.hackathon.simulacao.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimulacaoParcela {
    @Schema(description = "número da parcela")
    private Integer numero;

    @Schema(description = "valor da amortização do empréstimo")
    private BigDecimal valorAmortizacao;

    @Schema(description = "valor do juros cobrado")
    private BigDecimal valorJuros;

    @Schema(description = "valor total")
    private BigDecimal valorPrestacao;
}
