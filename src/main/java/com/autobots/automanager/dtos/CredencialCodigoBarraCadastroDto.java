package com.autobots.automanager.dtos;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class CredencialCodigoBarraCadastroDto extends CredencialCadastroDto {

    @NotNull(message = "O codigo de barras e obrigatorio")
    @Positive(message = "O codigo de barras deve ser um numero positivo")
    private Long codigo;
}
