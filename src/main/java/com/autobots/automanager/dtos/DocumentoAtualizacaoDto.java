package com.autobots.automanager.dtos;

import java.util.Date;

import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.Size;

import com.autobots.automanager.enumeracoes.TipoDocumento;

import lombok.Data;

@Data
public class DocumentoAtualizacaoDto {

    private TipoDocumento tipo;

    @PastOrPresent(message = "A data de emissao nao pode estar no futuro")
    private Date dataEmissao;

    @Size(max = 255, message = "O numero do documento deve ter no maximo 255 caracteres")
    private String numero;
}
