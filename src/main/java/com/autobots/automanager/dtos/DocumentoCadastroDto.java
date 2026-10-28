package com.autobots.automanager.dtos;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class DocumentoCadastroDto {

    @NotBlank(message = "O tipo do documento e obrigatorio")
    @Size(max = 255, message = "O tipo do documento deve ter no maximo 255 caracteres")
    private String tipo;

    @NotBlank(message = "O numero do documento e obrigatorio")
    @Size(max = 255, message = "O numero do documento deve ter no maximo 255 caracteres")
    private String numero;
}
