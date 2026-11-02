package com.autobots.automanager.dtos;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import javax.validation.constraints.Size;

import com.autobots.automanager.enumeracoes.PerfilUsuario;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;

import lombok.Data;

@Data
public class UsuarioAtualizacaoDto {

    @NotNull(message = "O id do usuario e obrigatorio para atualizacao")
    private Long id;

    @Size(max = 255, message = "O nome deve ter no maximo 255 caracteres")
    private String nome;

    @Size(max = 255, message = "O nome social deve ter no maximo 255 caracteres")
    private String nomeSocial;

    @Past(message = "A data de nascimento deve estar no passado")
    private Date dataNascimento;

    @Size(min = 1, message = "Se informados, os perfis devem ter ao menos um valor")
    private Set<@NotNull(message = "Os perfis nao podem conter valor nulo") PerfilUsuario> perfis;

    @Valid
    private EnderecoAtualizacaoDto endereco;

    @Valid
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<@NotNull(message = "A lista de documentos nao pode conter valor nulo") DocumentoDto> documentos = new ArrayList<>();

    @Valid
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<@NotNull(message = "A lista de telefones nao pode conter valor nulo") TelefoneDto> telefones = new ArrayList<>();

    @Valid
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<@NotNull(message = "A lista de e-mails nao pode conter valor nulo") EmailDto> emails = new ArrayList<>();
}
