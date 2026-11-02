package com.autobots.automanager.dtos;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

import lombok.Data;

@Data
public class ItemVendaCadastroDto {

    @NotNull(message = "O id da mercadoria e obrigatorio")
    private Long mercadoriaId;

    @NotNull(message = "A quantidade do item e obrigatoria")
    @Positive(message = "A quantidade do item deve ser maior que zero")
    private Long quantidade;
}
