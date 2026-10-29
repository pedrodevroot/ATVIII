package com.autobots.automanager.dtos;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class EmailCadastroDto {

    @NotBlank(message = "O endereco de e-mail e obrigatorio")
    @Email(message = "O endereco de e-mail e invalido")
    @Size(max = 255, message = "O e-mail deve ter no maximo 255 caracteres")
    private String endereco;
}
