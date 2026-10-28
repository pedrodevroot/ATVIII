package com.autobots.automanager.dtos;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Past;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class ClienteCadastroDto {

    @NotBlank(message = "O nome e obrigatorio")
    @Size(max = 255, message = "O nome deve ter no maximo 255 caracteres")
    private String nome;

    @Size(max = 255, message = "O nome social deve ter no maximo 255 caracteres")
    private String nomeSocial;

    @Past(message = "A data de nascimento deve estar no passado")
    private Date dataNascimento;

    @Valid
    private List<DocumentoCadastroDto> documentos = new ArrayList<>();

    @Valid
    private List<EnderecoCadastroDto> enderecos = new ArrayList<>();

    @Valid
    private List<TelefoneCadastroDto> telefones = new ArrayList<>();
}
