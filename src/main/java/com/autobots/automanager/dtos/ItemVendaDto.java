package com.autobots.automanager.dtos;

import java.math.BigDecimal;

import org.springframework.hateoas.RepresentationModel;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class ItemVendaDto extends RepresentationModel<ItemVendaDto> {
    private Long id;
    private Long mercadoriaId;
    private String nome;
    private Long quantidade;
    private BigDecimal valorUnitario;
}
