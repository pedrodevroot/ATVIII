package com.autobots.automanager.dtos;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.hateoas.RepresentationModel;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class ClienteRespostaDto extends RepresentationModel<ClienteRespostaDto> {
    private Long id;
    private String nome;
    private String nomeSocial;
    private Date dataNascimento;
    private Date dataCadastro;
    private List<DocumentoDto> documentos = new ArrayList<>();
    private List<EnderecoDto> enderecos = new ArrayList<>();
    private List<TelefoneDto> telefones = new ArrayList<>();
}
