package com.hackathon.simulacao.model.service;

import com.azure.identity.AzureAuthorityHosts;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import com.azure.messaging.eventhubs.models.SendOptions;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackathon.simulacao.exception.RegraNegocioException;
import com.hackathon.simulacao.model.Produto;
import com.hackathon.simulacao.model.dto.SimulacaoParcelas;
import com.hackathon.simulacao.model.dto.SimulacaoRequest;
import com.hackathon.simulacao.model.dto.SimulacaoResponse;
import com.hackathon.simulacao.model.dto.SimulacaoResultado;
import com.hackathon.simulacao.repository.ProdutoRepository;
import com.hackathon.simulacao.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

import static com.hackathon.simulacao.enumerable.TipoTabelaCorrecaoMonetaria.PRICE;
import static com.hackathon.simulacao.enumerable.TipoTabelaCorrecaoMonetaria.SAC;
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
        log.info(json);
        var producer = client();
       // producer.send(List.of(new EventData(json)));
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
