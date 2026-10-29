package com.autobots.automanager.dtos;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.springframework.hateoas.RepresentationModel;

import com.autobots.automanager.enumeracoes.PerfilUsuario;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class UsuarioRespostaDto extends RepresentationModel<UsuarioRespostaDto> {
    private Long id;
    private String nome;
    private String nomeSocial;
    private Date dataNascimento;
    private Date dataCadastro;
    private Set<PerfilUsuario> perfis = new TreeSet<>();
    private EnderecoDto endereco;
    private List<DocumentoDto> documentos = new ArrayList<>();
    private List<TelefoneDto> telefones = new ArrayList<>();
    private List<EmailDto> emails = new ArrayList<>();
    private List<CredencialDto> credenciais = new ArrayList<>();
}
