package com.autobots.automanager.conversores;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.autobots.automanager.dtos.TelefoneAtualizacaoDto;
import com.autobots.automanager.dtos.TelefoneCadastroDto;
import com.autobots.automanager.dtos.TelefoneDto;
import com.autobots.automanager.entidades.Telefone;

@Component
public class TelefoneConversor {

    public Telefone paraEntidade(TelefoneCadastroDto dto) {
        Telefone telefone = new Telefone();
        telefone.setDdd(dto.getDdd());
        telefone.setNumero(dto.getNumero());
        return telefone;
    }

    public Telefone paraEntidade(TelefoneAtualizacaoDto dto) {
        Telefone telefone = new Telefone();
        telefone.setDdd(dto.getDdd());
        telefone.setNumero(dto.getNumero());
        return telefone;
    }

    public TelefoneDto paraResposta(Telefone telefone) {
        TelefoneDto dto = new TelefoneDto();
        dto.setId(telefone.getId());
        dto.setDdd(telefone.getDdd());
        dto.setNumero(telefone.getNumero());
        return dto;
    }

    public List<TelefoneDto> paraResposta(List<Telefone> telefones) {
        List<TelefoneDto> dtos = new ArrayList<>();
        for (Telefone telefone : telefones) {
            dtos.add(paraResposta(telefone));
        }
        return dtos;
    }
}
