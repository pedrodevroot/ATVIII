package com.autobots.automanager.excecoes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.InvalidTypeIdException;

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
                        + "Verifique se ha valor duplicado em campo unico, "
                        + "campo obrigatorio ausente ou registro ainda vinculado a outro.",
                null, requisicao);
    }

    @ExceptionHandler(ConflitoException.class)
    public ResponseEntity<ErroResposta> tratarConflito(
            ConflitoException excecao, HttpServletRequest requisicao) {
        return montar(HttpStatus.CONFLICT, excecao.getMessage(), null, requisicao);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResposta> tratarCorpoIlegivel(
            HttpMessageNotReadableException excecao, HttpServletRequest requisicao) {
        String mensagem = "O corpo da requisicao nao pode ser lido. Verifique o formato do JSON.";
        Throwable causa = excecao.getCause();
        if (causa instanceof InvalidTypeIdException) {
            mensagem = "O campo tipo esta ausente ou tem um valor nao aceito.";
        } else if (causa instanceof InvalidFormatException formato && formato.getTargetType().isEnum()) {
            mensagem = "O valor " + formato.getValue() + " nao e aceito. Valores aceitos: "
                    + Arrays.toString(formato.getTargetType().getEnumConstants()) + ".";
        }
        return montar(HttpStatus.BAD_REQUEST, mensagem, null, requisicao);
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
