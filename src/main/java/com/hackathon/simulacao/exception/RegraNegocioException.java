package com.hackathon.simulacao.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Getter
public class RegraNegocioException extends RuntimeException {
    public RegraNegocioException(String erro) {
        this.erro = erro;
        this.status = BAD_REQUEST;
    }

    private String erro;
    private HttpStatus status;
}
