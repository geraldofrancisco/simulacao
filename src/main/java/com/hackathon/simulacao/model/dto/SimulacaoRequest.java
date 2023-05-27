package com.hackathon.simulacao.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimulacaoRequest {
    @NotNull(message = "O valor desejado deve ser informado")
    private BigDecimal valorDesejado;

    @NotNull(message = "O prazo deve ser informado")
    private Integer prazo;
}
