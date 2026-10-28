package com.autobots.automanager.excecoes;

public class RecursoNaoEncontradoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }

    public RecursoNaoEncontradoException(String recurso, long id) {
        super(recurso + " de id " + id + " nao foi encontrado");
    }
}
