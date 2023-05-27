package com.hackathon.simulacao.model.controller;

import com.hackathon.simulacao.model.dto.ErroSistema;
import com.hackathon.simulacao.model.dto.SimulacaoRequest;
import com.hackathon.simulacao.model.dto.SimulacaoResponse;
import com.hackathon.simulacao.model.service.SimulacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@Tag(name = "Simulação Controller")
@RestController
@RequestMapping("/api/v1/simulacao")
@RequiredArgsConstructor
public class SimulacaoController {

    private final SimulacaoService service;

    @PostMapping
    @Operation(
            summary = "Simulação",
            description = "Endpoint para fazer uma simulação de valores para empréstimo",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Sucesso na simulação",
                            content = @Content(
                                    mediaType = APPLICATION_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = SimulacaoResponse.class,
                                            description = "Resposta de sucesso da simulação"
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Erro do sistema",
                            content = @Content(
                                    mediaType = APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErroSistema.class)
                            )
                    )
            }
    )
    private SimulacaoResponse buscaSimulacao(@Valid @RequestBody SimulacaoRequest request) {
        return service.simula(request);
    }
}
