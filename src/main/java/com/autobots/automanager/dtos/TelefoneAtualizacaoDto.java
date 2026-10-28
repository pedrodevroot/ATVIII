package com.autobots.automanager.dtos;

import javax.validation.constraints.Pattern;

import lombok.Data;

@Data
public class TelefoneAtualizacaoDto {

    @Pattern(regexp = "[0-9]{2}", message = "O DDD deve conter exatamente 2 digitos")
    private String ddd;

    @Pattern(regexp = "[0-9]{8,9}", message = "O numero do telefone deve conter 8 ou 9 digitos")
    private String numero;
}
