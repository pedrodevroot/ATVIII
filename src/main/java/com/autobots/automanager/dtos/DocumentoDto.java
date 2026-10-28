package com.autobots.automanager.dtos;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.hateoas.RepresentationModel;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class DocumentoDto extends RepresentationModel<DocumentoDto> {

    @NotNull(message = "O id do documento e obrigatorio para atualizacao")
    private Long id;

    @Size(max = 255, message = "O tipo do documento deve ter no maximo 255 caracteres")
    private String tipo;

    @Size(max = 255, message = "O numero do documento deve ter no maximo 255 caracteres")
    private String numero;
}
