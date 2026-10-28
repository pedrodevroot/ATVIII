package com.autobots.automanager.excecoes;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ErroResposta {
    private LocalDateTime momento;
    private int status;
    private String erro;
    private String mensagem;
    private String caminho;
    private List<ErroCampo> erros;
}
