package com.autobots.automanager.dtos;

import java.util.Date;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.Size;

import com.autobots.automanager.enumeracoes.TipoDocumento;

import lombok.Data;

@Data
public class DocumentoCadastroDto {

    @NotNull(message = "O tipo do documento e obrigatorio: CPF, CNPJ, RG, CNH ou PASSAPORTE")
    private TipoDocumento tipo;

    @NotNull(message = "A data de emissao do documento e obrigatoria")
    @PastOrPresent(message = "A data de emissao nao pode estar no futuro")
    private Date dataEmissao;

    @NotBlank(message = "O numero do documento e obrigatorio")
    @Size(max = 255, message = "O numero do documento deve ter no maximo 255 caracteres")
    private String numero;
}
