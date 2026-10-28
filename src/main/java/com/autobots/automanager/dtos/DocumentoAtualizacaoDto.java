package com.autobots.automanager.dtos;

import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class DocumentoAtualizacaoDto {

    @Size(max = 255, message = "O tipo do documento deve ter no maximo 255 caracteres")
    private String tipo;

    @Size(max = 255, message = "O numero do documento deve ter no maximo 255 caracteres")
    private String numero;
}
