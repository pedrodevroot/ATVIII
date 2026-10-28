package com.autobots.automanager.excecoes;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ErroCampo {
    private String campo;
    private String mensagem;
}
