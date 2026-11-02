package com.autobots.automanager.dtos;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;

import lombok.Data;

@Data
public class EmpresaCadastroDto {

    @NotBlank(message = "A razao social e obrigatoria")
    @Size(max = 255, message = "A razao social deve ter no maximo 255 caracteres")
    private String razaoSocial;

    @Size(max = 255, message = "O nome fantasia deve ter no maximo 255 caracteres")
    private String nomeFantasia;

    @Valid
    private EnderecoCadastroDto endereco;

    @Valid
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<@NotNull(message = "A lista de telefones nao pode conter valor nulo") TelefoneCadastroDto> telefones = new ArrayList<>();
}
