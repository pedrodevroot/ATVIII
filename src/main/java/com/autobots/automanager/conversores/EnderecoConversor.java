package com.autobots.automanager.conversores;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.autobots.automanager.dtos.EnderecoAtualizacaoDto;
import com.autobots.automanager.dtos.EnderecoCadastroDto;
import com.autobots.automanager.dtos.EnderecoDto;
import com.autobots.automanager.entidades.Endereco;

@Component
public class EnderecoConversor {

    public Endereco paraEntidade(EnderecoCadastroDto dto) {
        Endereco endereco = new Endereco();
        endereco.setEstado(dto.getEstado());
        endereco.setCidade(dto.getCidade());
        endereco.setBairro(dto.getBairro());
        endereco.setRua(dto.getRua());
        endereco.setNumero(dto.getNumero());
        endereco.setCodigoPostal(dto.getCodigoPostal());
        endereco.setInformacoesAdicionais(dto.getInformacoesAdicionais());
        return endereco;
    }

    public EnderecoDto paraResposta(Endereco endereco) {
        EnderecoDto dto = new EnderecoDto();
        dto.setId(endereco.getId());
        dto.setEstado(endereco.getEstado());
        dto.setCidade(endereco.getCidade());
        dto.setBairro(endereco.getBairro());
        dto.setRua(endereco.getRua());
        dto.setNumero(endereco.getNumero());
        dto.setCodigoPostal(endereco.getCodigoPostal());
        dto.setInformacoesAdicionais(endereco.getInformacoesAdicionais());
        return dto;
    }

    public Endereco paraEntidade(EnderecoAtualizacaoDto dto) {
        Endereco endereco = new Endereco();
        endereco.setEstado(dto.getEstado());
        endereco.setCidade(dto.getCidade());
        endereco.setBairro(dto.getBairro());
        endereco.setRua(dto.getRua());
        endereco.setNumero(dto.getNumero());
        endereco.setCodigoPostal(dto.getCodigoPostal());
        endereco.setInformacoesAdicionais(dto.getInformacoesAdicionais());
        return endereco;
    }

    public List<EnderecoDto> paraResposta(List<Endereco> enderecos) {
        List<EnderecoDto> dtos = new ArrayList<>();
        for (Endereco endereco : enderecos) {
            dtos.add(paraResposta(endereco));
        }
        return dtos;
    }
}
