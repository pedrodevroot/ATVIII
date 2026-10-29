package com.autobots.automanager.dtos;

import java.util.Date;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.Size;

import org.springframework.hateoas.RepresentationModel;

import com.autobots.automanager.enumeracoes.TipoDocumento;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class DocumentoDto extends RepresentationModel<DocumentoDto> {

    @NotNull(message = "O id do documento e obrigatorio para atualizacao")
    private Long id;

    private TipoDocumento tipo;

    @PastOrPresent(message = "A data de emissao nao pode estar no futuro")
    private Date dataEmissao;

    @Size(max = 255, message = "O numero do documento deve ter no maximo 255 caracteres")
    private String numero;
}
