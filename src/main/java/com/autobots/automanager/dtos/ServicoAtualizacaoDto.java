package com.autobots.automanager.dtos;

import java.math.BigDecimal;

import javax.validation.constraints.Digits;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class ServicoAtualizacaoDto {

    @Size(max = 255, message = "O nome deve ter no maximo 255 caracteres")
    private String nome;

    @Size(max = 255, message = "A descricao deve ter no maximo 255 caracteres")
    private String descricao;

    @PositiveOrZero(message = "O valor nao pode ser negativo")
    @Digits(integer = 8, fraction = 2, message = "O valor deve ter ate 8 digitos inteiros e 2 decimais")
    private BigDecimal valor;
}
