package com.hackathon.simulacao.model.service;

import com.azure.identity.AzureAuthorityHosts;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import com.hackathon.simulacao.enumerable.TipoTabelaCorrecaoMonetaria;
import com.hackathon.simulacao.exception.RegraNegocioException;
import com.hackathon.simulacao.model.Produto;
import com.hackathon.simulacao.model.dto.SimulacaoParcela;
import com.hackathon.simulacao.model.dto.SimulacaoRequest;
import com.hackathon.simulacao.model.dto.SimulacaoResponse;
import com.hackathon.simulacao.model.dto.SimulacaoResultado;
import com.hackathon.simulacao.repository.ProdutoRepository;
import com.hackathon.simulacao.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

import static com.hackathon.simulacao.enumerable.TipoTabelaCorrecaoMonetaria.PRICE;
import static com.hackathon.simulacao.enumerable.TipoTabelaCorrecaoMonetaria.SAC;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.CEILING;
import static java.math.RoundingMode.HALF_UP;
import static java.util.List.of;
import static java.util.Objects.nonNull;

@Slf4j
@Service
@RequiredArgsConstructor
public class SimulacaoService {
    @Value("${event-hub.namespace}")
    private String namespace;

    @Value("${event-hub.name}")
    private String eventHubName;

    @Value("${event-hub.key-name}")
    private String keyName;

    @Value("${event-hub.key-value}")
    private String keyValue;

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

    private void enviaParaFila(SimulacaoResponse calculo) {
        var json = JsonUtil.ObjectToString(calculo);
        var producer = client();
        producer.send(List.of(new EventData(json)));
        producer.close();
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

    private SimulacaoResultado calculaPRICE(Produto produto, SimulacaoRequest request) {
        var resposta = buscaDefault(request, PRICE);
        if (request.prazoEMaiorQueZero()){
            var parcelas = geraPrestacoesPRICE(request, produto);
            resposta
                    .parcelas(parcelas)
                    .valorTotalParcelas(parcelas.stream().map(SimulacaoParcela::getValorPrestacao).reduce(ZERO, BigDecimal::add));
        }

        return resposta.build();
    }

    private List<SimulacaoParcela> geraPrestacoesPRICE(SimulacaoRequest request, Produto produto) {
        var valorPrestacao = calculaValorPrestacaoPRICE(request, produto.getTaxaJuros()).setScale(2, HALF_UP);
        var saldoDevedor = new AtomicReference<>(request.getValorDesejado());
        return IntStream.range(1, request.getPrazo() + 1)
                .mapToObj(parcela -> {
                    var jurosParcela = saldoDevedor.get().multiply(produto.getTaxaJuros()).setScale(2, CEILING);
                    var valorAmortizacao = valorPrestacao.subtract(jurosParcela);
                    saldoDevedor.set(saldoDevedor.get().subtract(valorAmortizacao));
                    return SimulacaoParcela.builder()
                            .numero(parcela)
                            .valorJuros(jurosParcela)
                            .valorAmortizacao(valorAmortizacao)
                            .valorPrestacao(valorAmortizacao.add(jurosParcela))
                            .build();
                })
                .toList();
    }

    private static SimulacaoResultado.SimulacaoResultadoBuilder buscaDefault(SimulacaoRequest request, TipoTabelaCorrecaoMonetaria tipo) {
        return SimulacaoResultado.builder()
                .tipo(tipo)
                .parcelas(of(
                                SimulacaoParcela.builder()
                                        .valorPrestacao(request.getValorDesejado())
                                        .numero(1)
                                        .valorJuros(ZERO)
                                        .valorAmortizacao(request.getValorDesejado())
                                        .build()
                        )
                );
    }

    private BigDecimal calculaValorPrestacaoPRICE(SimulacaoRequest request, BigDecimal taxaJuros) {
        var jurosExponencial = taxaJuros.add(ONE).pow(request.getPrazo()).setScale(6, HALF_UP);
        var primeiraParte = jurosExponencial.multiply(taxaJuros).setScale(6, HALF_UP);
        var segundaParte = jurosExponencial.subtract(ONE);
        return request.getValorDesejado()
                .multiply(primeiraParte.divide(segundaParte, new MathContext(6)));
    }

    private SimulacaoResultado calculaSAC(Produto produto, SimulacaoRequest request) {
        var resposta = buscaDefault(request, SAC);
        if (request.prazoEMaiorQueZero()){
            var parcelas = geraPrestacoesSAC(request, produto);
            resposta
                    .parcelas(parcelas)
                    .valorTotalParcelas(parcelas.stream().map(SimulacaoParcela::getValorPrestacao).reduce(ZERO, BigDecimal::add));
        }

        return resposta.build();
    }

    private List<SimulacaoParcela> geraPrestacoesSAC(SimulacaoRequest request, Produto produto) {
        var saldoDevedor = new AtomicReference<>(request.getValorDesejado());
        var valorAmortizacao = request.getValorDesejado()
                .divide(BigDecimal.valueOf(request.getPrazo()), new MathContext(10)).setScale(2, CEILING);
        return IntStream.range(1, request.getPrazo() + 1)
                .mapToObj(parcela -> {
                    saldoDevedor.set(saldoDevedor.get().subtract(valorAmortizacao));
                    var jurosParcela = saldoDevedor.get().multiply(produto.getTaxaJuros()).setScale(2, CEILING);
                    return SimulacaoParcela.builder()
                            .numero(parcela)
                            .valorJuros(jurosParcela)
                            .valorAmortizacao(valorAmortizacao)
                            .valorPrestacao(valorAmortizacao.add(jurosParcela).setScale(2, CEILING))
                            .build();
                }).toList();
    }

    private void validaPrazo(Produto produto, Integer prazo) {
        if (prazo < produto.getMinMeses())
            throw new RegraNegocioException("Prazo inferior a %s parcelas para o valor desejado".formatted(produto.getMinMeses()));

        if (nonNull(produto.getMaxMeses()) && prazo > produto.getMaxMeses())
            throw new RegraNegocioException("Prazo superior a %s parcelas para o valor desejado".formatted(produto.getMaxMeses()));
    }

    private Produto buscaProduto(BigDecimal valorDesejado, BigDecimal maximo) {
        if (valorDesejado.compareTo(maximo) == -1L)
            return repository.buscaPorValoresMinimoEMaximo(valorDesejado)
                    .orElseThrow(() -> new RegraNegocioException("Produto não encontrado"));

        return repository.buscaSemValorMaximo()
                .orElseThrow(() -> new RegraNegocioException("Produto não encontrado"));
    }

    private void validaValorMinimo(BigDecimal valorDesejado, BigDecimal minimo) {
        if (valorDesejado.compareTo(minimo) == -1L) {
            log.info("valor mínimo inválido");
            throw new RegraNegocioException("valor inferior ao mínimo de R$ " + minimo);
        }
    }

    private EventHubProducerClient client() {
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder()
                .authorityHost(AzureAuthorityHosts.AZURE_PUBLIC_CLOUD)
                .build();

        var connection = "Endpoint=%s;SharedAccessKeyName=%s;SharedAccessKey=%s"
                .formatted(namespace, keyName, keyValue);

        return new EventHubClientBuilder()
                .connectionString(connection, eventHubName)
                .buildProducerClient();
    }
}
