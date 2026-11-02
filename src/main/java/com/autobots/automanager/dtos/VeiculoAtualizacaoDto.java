package com.autobots.automanager.dtos;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.autobots.automanager.enumeracoes.TipoVeiculo;

import lombok.Data;

@Data
public class VeiculoAtualizacaoDto {

    private TipoVeiculo tipo;

    @Size(max = 255, message = "O modelo deve ter no maximo 255 caracteres")
    private String modelo;

    @Pattern(regexp = "[A-Z]{3}[0-9][A-Z0-9][0-9]{2}", message = "A placa deve seguir o formato ABC1234 ou ABC1D23")
    private String placa;
}
