package com.hackathon.simulacao.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

import static java.lang.Integer.MAX_VALUE;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimulacaoRequest {
    @NotNull(message = "O valor desejado deve ser informado")
    @Digits(fraction = 2, integer = MAX_VALUE, message = "O valor deve ter até 2 casas decimais")
    private BigDecimal valorDesejado;

    @NotNull(message = "O prazo deve ser informado")
    private Integer prazo;

    @JsonIgnore
    public boolean prazoEMaiorQueZero() {
        return prazo > 0;
    }
}
