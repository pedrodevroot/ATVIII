package com.autobots.automanager.dtos;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.hateoas.RepresentationModel;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class VendaRespostaDto extends RepresentationModel<VendaRespostaDto> {
    private Long id;
    private Date cadastro;
    private String identificacao;
    private Long clienteId;
    private Long funcionarioId;
    private Long veiculoId;
    private List<MercadoriaDto> mercadorias = new ArrayList<>();
    private List<ServicoDto> servicos = new ArrayList<>();
}
