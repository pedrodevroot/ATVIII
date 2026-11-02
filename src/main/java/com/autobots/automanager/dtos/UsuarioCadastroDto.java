package com.autobots.automanager.dtos;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import javax.validation.constraints.Size;

import com.autobots.automanager.enumeracoes.PerfilUsuario;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;

import lombok.Data;

@Data
public class UsuarioCadastroDto {

    @NotBlank(message = "O nome e obrigatorio")
    @Size(max = 255, message = "O nome deve ter no maximo 255 caracteres")
    private String nome;

    @Size(max = 255, message = "O nome social deve ter no maximo 255 caracteres")
    private String nomeSocial;

    @Past(message = "A data de nascimento deve estar no passado")
    private Date dataNascimento;

    @NotEmpty(message = "Informe ao menos um perfil: CLIENTE, FUNCIONARIO ou FORNECEDOR")
    private Set<@NotNull(message = "Os perfis nao podem conter valor nulo") PerfilUsuario> perfis = new HashSet<>();

    @Valid
    private EnderecoCadastroDto endereco;

    @Valid
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<@NotNull(message = "A lista de documentos nao pode conter valor nulo") DocumentoCadastroDto> documentos = new ArrayList<>();

    @Valid
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<@NotNull(message = "A lista de telefones nao pode conter valor nulo") TelefoneCadastroDto> telefones = new ArrayList<>();

    @Valid
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<@NotNull(message = "A lista de e-mails nao pode conter valor nulo") EmailCadastroDto> emails = new ArrayList<>();

    @Valid
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<@NotNull(message = "A lista de credenciais nao pode conter valor nulo") CredencialCadastroDto> credenciais = new ArrayList<>();
}
