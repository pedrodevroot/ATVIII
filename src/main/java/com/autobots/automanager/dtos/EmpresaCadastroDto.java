package com.autobots.automanager.dtos;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

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
    private List<TelefoneCadastroDto> telefones = new ArrayList<>();
}
