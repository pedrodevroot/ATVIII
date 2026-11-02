package com.autobots.automanager.dtos;

import java.math.BigDecimal;
import java.util.Date;

import javax.validation.constraints.Digits;
import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class MercadoriaAtualizacaoDto {

    @Size(max = 255, message = "O nome deve ter no maximo 255 caracteres")
    private String nome;

    @Size(max = 255, message = "A descricao deve ter no maximo 255 caracteres")
    private String descricao;

    @PastOrPresent(message = "A data de fabricacao nao pode estar no futuro")
    private Date fabricacao;

    private Date validade;

    @PositiveOrZero(message = "A quantidade nao pode ser negativa")
    private Long quantidade;

    @PositiveOrZero(message = "O valor nao pode ser negativo")
    @Digits(integer = 8, fraction = 2, message = "O valor deve ter ate 8 digitos inteiros e 2 decimais")
    private BigDecimal valor;
}
