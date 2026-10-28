package com.autobots.automanager.dtos;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.springframework.hateoas.RepresentationModel;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class TelefoneDto extends RepresentationModel<TelefoneDto> {

    @NotNull(message = "O id do telefone e obrigatorio para atualizacao")
    private Long id;

    @Pattern(regexp = "[0-9]{2}", message = "O DDD deve conter exatamente 2 digitos")
    private String ddd;

    @Pattern(regexp = "[0-9]{8,9}", message = "O numero do telefone deve conter 8 ou 9 digitos")
    private String numero;
}
