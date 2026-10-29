package com.autobots.automanager.dtos;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.hateoas.RepresentationModel;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class EmailDto extends RepresentationModel<EmailDto> {

    @NotNull(message = "O id do e-mail e obrigatorio para atualizacao")
    private Long id;

    @Email(message = "O endereco de e-mail e invalido")
    @Size(max = 255, message = "O e-mail deve ter no maximo 255 caracteres")
    private String endereco;
}
