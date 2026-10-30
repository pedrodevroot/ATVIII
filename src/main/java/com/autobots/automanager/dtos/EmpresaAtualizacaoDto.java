package com.autobots.automanager.dtos;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class EmpresaAtualizacaoDto {

    @NotNull(message = "O id da empresa e obrigatorio para atualizacao")
    private Long id;

    @Size(max = 255, message = "A razao social deve ter no maximo 255 caracteres")
    private String razaoSocial;

    @Size(max = 255, message = "O nome fantasia deve ter no maximo 255 caracteres")
    private String nomeFantasia;

    @Valid
    private EnderecoAtualizacaoDto endereco;

    @Valid
    private List<TelefoneDto> telefones = new ArrayList<>();
}
