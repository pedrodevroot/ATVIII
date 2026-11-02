package com.autobots.automanager.conversores;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.autobots.automanager.dtos.VendaRespostaDto;
import com.autobots.automanager.entidades.Venda;

@Component
public class VendaConversor {
    private MercadoriaConversor mercadoriaConversor = new MercadoriaConversor();
    private ServicoConversor servicoConversor = new ServicoConversor();

    public VendaRespostaDto paraResposta(Venda venda) {
        VendaRespostaDto dto = new VendaRespostaDto();
        dto.setId(venda.getId());
        dto.setCadastro(venda.getCadastro());
        dto.setIdentificacao(venda.getIdentificacao());
        dto.setClienteId(venda.getCliente().getId());
        dto.setFuncionarioId(venda.getFuncionario().getId());
        if (venda.getVeiculo() != null) {
            dto.setVeiculoId(venda.getVeiculo().getId());
        }
        dto.setMercadorias(mercadoriaConversor.paraResposta(venda.getMercadorias()));
        dto.setServicos(servicoConversor.paraResposta(venda.getServicos()));
        return dto;
    }

    public List<VendaRespostaDto> paraResposta(List<Venda> vendas) {
        List<VendaRespostaDto> dtos = new ArrayList<>();
        for (Venda venda : vendas) {
            dtos.add(paraResposta(venda));
        }
        return dtos;
    }
}
