package com.autobots.automanager.excecoes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ManipuladorDeExcecoes {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResposta> tratarNaoEncontrado(
            RecursoNaoEncontradoException excecao, HttpServletRequest requisicao) {
        return montar(HttpStatus.NOT_FOUND, excecao.getMessage(), null, requisicao);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResposta> tratarValidacao(
            MethodArgumentNotValidException excecao, HttpServletRequest requisicao) {
        List<ErroCampo> erros = new ArrayList<>();
        for (FieldError erro : excecao.getBindingResult().getFieldErrors()) {
            erros.add(new ErroCampo(erro.getField(), erro.getDefaultMessage()));
        }
        return montar(HttpStatus.BAD_REQUEST,
                "Os dados enviados nao passaram na validacao.", erros, requisicao);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroResposta> tratarViolacaoDeIntegridade(
            DataIntegrityViolationException excecao, HttpServletRequest requisicao) {
        return montar(HttpStatus.CONFLICT,
                "A operacao viola uma restricao de integridade dos dados. "
                        + "Verifique se ha valor duplicado em campo unico "
                        + "ou campo obrigatorio ausente.",
                null, requisicao);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErroResposta> tratarArgumentoInvalido(
            IllegalArgumentException excecao, HttpServletRequest requisicao) {
        return montar(HttpStatus.BAD_REQUEST, excecao.getMessage(), null, requisicao);
    }

    private ResponseEntity<ErroResposta> montar(HttpStatus status, String mensagem,
            List<ErroCampo> erros, HttpServletRequest requisicao) {
        ErroResposta corpo = new ErroResposta(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                mensagem,
                requisicao.getRequestURI(),
                erros);
        return ResponseEntity.status(status).body(corpo);
    }
}
