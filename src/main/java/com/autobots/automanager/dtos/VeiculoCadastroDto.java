package com.autobots.automanager.dtos;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.autobots.automanager.enumeracoes.TipoVeiculo;

import lombok.Data;

@Data
public class VeiculoCadastroDto {

    @NotNull(message = "O tipo do veiculo e obrigatorio: HATCH, SEDA, SUV, PICKUP ou SW")
    private TipoVeiculo tipo;

    @NotBlank(message = "O modelo do veiculo e obrigatorio")
    @Size(max = 255, message = "O modelo deve ter no maximo 255 caracteres")
    private String modelo;

    @NotBlank(message = "A placa do veiculo e obrigatoria")
    @Pattern(regexp = "[A-Z]{3}[0-9][A-Z0-9][0-9]{2}", message = "A placa deve seguir o formato ABC1234 ou ABC1D23")
    private String placa;
}
