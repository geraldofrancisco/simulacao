package com.hackathon.simulacao.model.service;

import com.hackathon.simulacao.exception.RegraNegocioException;
import com.hackathon.simulacao.model.Produto;
import com.hackathon.simulacao.model.dto.SimulacaoParcelas;
import com.hackathon.simulacao.model.dto.SimulacaoRequest;
import com.hackathon.simulacao.model.dto.SimulacaoResponse;
import com.hackathon.simulacao.model.dto.SimulacaoResultado;
import com.hackathon.simulacao.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import static com.hackathon.simulacao.enumerable.TipoTabelaCorrecaoMonetaria.PRICE;
import static com.hackathon.simulacao.enumerable.TipoTabelaCorrecaoMonetaria.SAC;
import static java.util.List.of;
import static java.util.Objects.nonNull;

@Slf4j
@Service
@RequiredArgsConstructor
public class SimulacaoService {
    private final ProdutoRepository repository;


    public SimulacaoResponse simula(SimulacaoRequest request) {
        var extremos = repository.buscaExtremos();
        validaValorMinimo(request.getValorDesejado(), extremos.getMinimo());
        var produto = buscaProduto(request.getValorDesejado(), extremos.getMaximo());
        validaPrazo(produto, request.getPrazo());
        var calculo = realizaCalculo(produto, request);
        enviaParaFila(calculo);
        return calculo;
    }

    @Async
    private void enviaParaFila(SimulacaoResponse calculo) {
    }

    private SimulacaoResponse realizaCalculo(Produto produto, SimulacaoRequest request) {
        return SimulacaoResponse.builder()
                .codigoProduto(produto.getCodigo())
                .descricaoProduto(produto.getNome())
                .taxaJuros(produto.getTaxaJuros())
                .resultadoSimulacao(of(
                        calculaSAC(produto, request),
                        calculaPRICE(produto, request)
                ))
                .build();
    }

    private SimulacaoResultado calculaPRICE(Produto produto, SimulacaoRequest valorDesejado) {
        return SimulacaoResultado.builder()
                .tipo(PRICE)
                .parcelas(of(SimulacaoParcelas.builder().build()))
                .build();
    }

    private SimulacaoResultado calculaSAC(Produto produto, SimulacaoRequest valorDesejado) {
        return SimulacaoResultado.builder()
                .tipo(SAC)
                .parcelas(of(SimulacaoParcelas.builder().build()))
                .build();
    }

    private void validaPrazo(Produto produto, Integer prazo) {
        if(prazo < produto.getMinMeses())
            throw new RegraNegocioException("Prazo inferior a %s parcelas para o valor desejado".formatted(produto.getMinMeses()));

        if(nonNull(produto.getMaxMeses()) && prazo > produto.getMaxMeses())
            throw new RegraNegocioException("Prazo superior a %s parcelas para o valor desejado".formatted(produto.getMaxMeses()));
    }

    private Produto buscaProduto(BigDecimal valorDesejado, BigDecimal maximo) {
        if(valorDesejado.compareTo(maximo) == -1L)
            return repository.buscaPorValoresMinimoEMaximo(valorDesejado)
                    .orElseThrow(() -> new RegraNegocioException("Produto não encontrado"));
        return repository.buscaSemValorMaximo();
    }

    private void validaValorMinimo(BigDecimal valorDesejado, BigDecimal minimo) {
        if(valorDesejado.compareTo(minimo) == -1L) {
            log.info("valor mínimo inválido");
            throw new RegraNegocioException("valor inferior ao mínimo de R$ "+minimo);
        }
    }
}
