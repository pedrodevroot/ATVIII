package com.autobots.automanager.dtos;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "tipo")
@JsonSubTypes({
        @JsonSubTypes.Type(value = CredencialUsuarioSenhaCadastroDto.class,
                name = CredencialCadastroDto.USUARIO_SENHA),
        @JsonSubTypes.Type(value = CredencialCodigoBarraCadastroDto.class,
                name = CredencialCadastroDto.CODIGO_BARRA) })
public abstract class CredencialCadastroDto {
    public static final String USUARIO_SENHA = "USUARIO_SENHA";
    public static final String CODIGO_BARRA = "CODIGO_BARRA";
}
