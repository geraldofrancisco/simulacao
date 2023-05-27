package com.hackathon.simulacao.exception;

import com.hackathon.simulacao.model.dto.ErroSistema;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@ControllerAdvice
public class SimulacaoExceptionHandler extends ResponseEntityExceptionHandler {
    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler({RegraNegocioException.class})
    public ResponseEntity<Object> RegraNegocioExceptionHandler(RegraNegocioException ex, WebRequest request) {
        return handleExceptionInternal(ex, new ErroSistema(ex.getErro()), new HttpHeaders(), BAD_REQUEST, request);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        var errors = ex.getAllErrors().stream().map(ObjectError::getDefaultMessage).toList();
        return handleExceptionInternal(ex, new ErroSistema(errors), new HttpHeaders(), BAD_REQUEST, request);
    }
}
