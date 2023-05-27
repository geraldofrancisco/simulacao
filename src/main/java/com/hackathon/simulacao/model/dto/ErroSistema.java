package com.hackathon.simulacao.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class ErroSistema {

    @Schema(description = "Lista com as mensagens de erro do sistema")
    private List<String> mensagensErro = new ArrayList<>();
    @Schema(description = "Timestamp do erro ocorrido")
    private LocalDateTime timestamp = LocalDateTime.now();

    public ErroSistema(String mensagem) {
        this.mensagensErro.add(mensagem);
    }

    public ErroSistema(List<String> mensagens) {
        this.mensagensErro = mensagens;
    }
}
