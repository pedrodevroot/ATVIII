package com.autobots.automanager.dtos;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class ClienteAtualizacaoDto {

    @NotNull(message = "O id do cliente e obrigatorio para atualizacao")
    private Long id;

    @Size(max = 255, message = "O nome deve ter no maximo 255 caracteres")
    private String nome;

    @Size(max = 255, message = "O nome social deve ter no maximo 255 caracteres")
    private String nomeSocial;

    @Past(message = "A data de nascimento deve estar no passado")
    private Date dataNascimento;

    @Valid
    private List<DocumentoDto> documentos = new ArrayList<>();

    @Valid
    private List<EnderecoDto> enderecos = new ArrayList<>();

    @Valid
    private List<TelefoneDto> telefones = new ArrayList<>();
}
