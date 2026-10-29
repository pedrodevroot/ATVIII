package com.autobots.automanager.dtos;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class CredencialUsuarioSenhaCadastroDto extends CredencialCadastroDto {

    @NotBlank(message = "O nome de usuario e obrigatorio")
    @Size(max = 255, message = "O nome de usuario deve ter no maximo 255 caracteres")
    private String nomeUsuario;

    @NotBlank(message = "A senha e obrigatoria")
    @Size(min = 6, max = 255, message = "A senha deve ter entre 6 e 255 caracteres")
    private String senha;
}
