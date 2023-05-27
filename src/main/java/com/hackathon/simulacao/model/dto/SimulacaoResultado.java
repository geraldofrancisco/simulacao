package com.hackathon.simulacao.model.dto;

import com.hackathon.simulacao.enumerable.TipoTabelaCorrecaoMonetaria;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimulacaoResultado {
    @Schema(description = "tipo da simulação")
    private TipoTabelaCorrecaoMonetaria tipo;

    @Schema(description = "valores das parcelas")
    private List<SimulacaoParcelas> parcelas;
}
