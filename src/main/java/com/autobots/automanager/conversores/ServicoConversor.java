package com.autobots.automanager.conversores;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.autobots.automanager.dtos.ServicoAtualizacaoDto;
import com.autobots.automanager.dtos.ServicoCadastroDto;
import com.autobots.automanager.dtos.ServicoDto;
import com.autobots.automanager.entidades.Servico;

@Component
public class ServicoConversor {

    public Servico paraEntidade(ServicoCadastroDto dto) {
        Servico servico = new Servico();
        servico.setNome(dto.getNome());
        servico.setDescricao(dto.getDescricao());
        servico.setValor(dto.getValor());
        return servico;
    }

    public Servico paraEntidade(ServicoAtualizacaoDto dto) {
        Servico servico = new Servico();
        servico.setNome(dto.getNome());
        servico.setDescricao(dto.getDescricao());
        servico.setValor(dto.getValor());
        return servico;
    }

    public ServicoDto paraResposta(Servico servico) {
        ServicoDto dto = new ServicoDto();
        dto.setId(servico.getId());
        dto.setNome(servico.getNome());
        dto.setDescricao(servico.getDescricao());
        dto.setValor(servico.getValor());
        return dto;
    }

    public List<ServicoDto> paraResposta(List<Servico> servicos) {
        List<ServicoDto> dtos = new ArrayList<>();
        for (Servico servico : servicos) {
            dtos.add(paraResposta(servico));
        }
        return dtos;
    }
}
