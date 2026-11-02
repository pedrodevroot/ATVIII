package com.autobots.automanager.conversores;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.autobots.automanager.dtos.MercadoriaCadastroDto;
import com.autobots.automanager.dtos.MercadoriaDto;
import com.autobots.automanager.entidades.Mercadoria;

@Component
public class MercadoriaConversor {

    public Mercadoria paraEntidade(MercadoriaCadastroDto dto) {
        Mercadoria mercadoria = new Mercadoria();
        mercadoria.setNome(dto.getNome());
        mercadoria.setDescricao(dto.getDescricao());
        mercadoria.setFabricacao(dto.getFabricacao());
        mercadoria.setValidade(dto.getValidade());
        mercadoria.setQuantidade(dto.getQuantidade());
        mercadoria.setValor(dto.getValor());
        return mercadoria;
    }

    public MercadoriaDto paraResposta(Mercadoria mercadoria) {
        MercadoriaDto dto = new MercadoriaDto();
        dto.setId(mercadoria.getId());
        dto.setNome(mercadoria.getNome());
        dto.setDescricao(mercadoria.getDescricao());
        dto.setFabricacao(mercadoria.getFabricacao());
        dto.setValidade(mercadoria.getValidade());
        dto.setCadastro(mercadoria.getCadastro());
        dto.setQuantidade(mercadoria.getQuantidade());
        dto.setValor(mercadoria.getValor());
        return dto;
    }

    public List<MercadoriaDto> paraResposta(List<Mercadoria> mercadorias) {
        List<MercadoriaDto> dtos = new ArrayList<>();
        for (Mercadoria mercadoria : mercadorias) {
            dtos.add(paraResposta(mercadoria));
        }
        return dtos;
    }
}
