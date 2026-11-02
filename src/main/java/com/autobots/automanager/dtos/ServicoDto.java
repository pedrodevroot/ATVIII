package com.autobots.automanager.dtos;

import java.math.BigDecimal;

import org.springframework.hateoas.RepresentationModel;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class ServicoDto extends RepresentationModel<ServicoDto> {
    private Long id;
    private String nome;
    private String descricao;
    private BigDecimal valor;
}
