package com.hackathon.simulacao.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MinimoMaximo {
    private BigDecimal minimo;
    private BigDecimal maximo;
}
