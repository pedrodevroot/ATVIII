package com.autobots.automanager.dtos;

import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.UniqueElements;

import lombok.Data;

@Data
public class VendaAtualizacaoDto {

    @Size(max = 255, message = "A identificacao deve ter no maximo 255 caracteres")
    private String identificacao;

    private Long clienteId;

    private Long funcionarioId;

    private Long veiculoId;

    @UniqueElements(message = "A mesma mercadoria nao pode ser informada mais de uma vez")
    private List<@NotNull(message = "O id da mercadoria nao pode ser nulo") Long> mercadoriaIds;

    @UniqueElements(message = "O mesmo servico nao pode ser informado mais de uma vez")
    private List<@NotNull(message = "O id do servico nao pode ser nulo") Long> servicoIds;
}
