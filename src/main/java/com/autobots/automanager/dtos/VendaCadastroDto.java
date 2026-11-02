package com.autobots.automanager.dtos;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.UniqueElements;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;

import lombok.Data;

@Data
public class VendaCadastroDto {

    @NotBlank(message = "A identificacao da venda e obrigatoria")
    @Size(max = 255, message = "A identificacao deve ter no maximo 255 caracteres")
    private String identificacao;

    @NotNull(message = "O id do cliente e obrigatorio")
    private Long clienteId;

    @NotNull(message = "O id do funcionario e obrigatorio")
    private Long funcionarioId;

    private Long veiculoId;

    @Valid
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<@NotNull(message = "A lista de itens nao pode conter valor nulo") ItemVendaCadastroDto> itens = new ArrayList<>();

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    @UniqueElements(message = "O mesmo servico nao pode ser informado mais de uma vez")
    private List<@NotNull(message = "O id do servico nao pode ser nulo") Long> servicoIds = new ArrayList<>();
}
