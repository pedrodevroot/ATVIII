package com.autobots.automanager.dtos;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Past;
import javax.validation.constraints.Size;

import com.autobots.automanager.enumeracoes.PerfilUsuario;

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
    private Set<PerfilUsuario> perfis = new HashSet<>();

    @Valid
    private EnderecoCadastroDto endereco;

    @Valid
    private List<DocumentoCadastroDto> documentos = new ArrayList<>();

    @Valid
    private List<TelefoneCadastroDto> telefones = new ArrayList<>();

    @Valid
    private List<EmailCadastroDto> emails = new ArrayList<>();

    @Valid
    private List<CredencialCadastroDto> credenciais = new ArrayList<>();
}
