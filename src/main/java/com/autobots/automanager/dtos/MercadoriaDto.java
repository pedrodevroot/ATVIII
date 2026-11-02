package com.autobots.automanager.dtos;

import java.math.BigDecimal;
import java.util.Date;

import org.springframework.hateoas.RepresentationModel;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class MercadoriaDto extends RepresentationModel<MercadoriaDto> {
    private Long id;
    private String nome;
    private String descricao;
    private Date fabricacao;
    private Date validade;
    private Date cadastro;
    private Long quantidade;
    private BigDecimal valor;
}
