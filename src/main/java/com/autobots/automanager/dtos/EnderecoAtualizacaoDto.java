package com.autobots.automanager.dtos;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class EnderecoAtualizacaoDto {

    @Size(max = 255, message = "O estado deve ter no maximo 255 caracteres")
    private String estado;

    @Size(max = 255, message = "A cidade deve ter no maximo 255 caracteres")
    private String cidade;

    @Size(max = 255, message = "O bairro deve ter no maximo 255 caracteres")
    private String bairro;

    @Size(max = 255, message = "A rua deve ter no maximo 255 caracteres")
    private String rua;

    @Size(max = 255, message = "O numero do endereco deve ter no maximo 255 caracteres")
    private String numero;

    @Pattern(regexp = "[0-9]{8}", message = "O codigo postal deve conter exatamente 8 digitos")
    private String codigoPostal;

    @Size(max = 255, message = "As informacoes adicionais devem ter no maximo 255 caracteres")
    private String informacoesAdicionais;
}
