package com.autobots.automanager.dtos;

import java.util.Date;

import org.springframework.hateoas.RepresentationModel;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class CredencialDto extends RepresentationModel<CredencialDto> {
    private Long id;
    private String tipo;
    private Date criacao;
    private Date ultimoAcesso;
    private Boolean inativo;
    private String nomeUsuario;
    private Long codigo;
}
