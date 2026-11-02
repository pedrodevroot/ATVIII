package com.autobots.automanager.conversores;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.autobots.automanager.dtos.ItemVendaDto;
import com.autobots.automanager.dtos.ServicoDto;
import com.autobots.automanager.dtos.VendaRespostaDto;
import com.autobots.automanager.entidades.ItemServico;
import com.autobots.automanager.entidades.ItemVenda;
import com.autobots.automanager.entidades.Venda;

@Component
public class VendaConversor {
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
        for (ItemVenda item : venda.getItens()) {
            dto.getItens().add(itemParaResposta(item));
        }
        for (ItemServico item : venda.getItensServico()) {
            dto.getServicos().add(servicoParaResposta(item));
        }
        dto.setValorTotal(valorTotal(venda));
        return dto;
    }

    public List<VendaRespostaDto> paraResposta(List<Venda> vendas) {
        List<VendaRespostaDto> dtos = new ArrayList<>();
        for (Venda venda : vendas) {
            dtos.add(paraResposta(venda));
        }
        return dtos;
    }

    private BigDecimal valorTotal(Venda venda) {
        BigDecimal total = BigDecimal.ZERO;
        for (ItemVenda item : venda.getItens()) {
            total = total.add(item.getValorUnitario().multiply(BigDecimal.valueOf(item.getQuantidade())));
        }
        for (ItemServico item : venda.getItensServico()) {
            total = total.add(item.getValorUnitario());
        }
        return total;
    }

    private ServicoDto servicoParaResposta(ItemServico item) {
        ServicoDto dto = new ServicoDto();
        dto.setId(item.getServico().getId());
        dto.setNome(item.getServico().getNome());
        dto.setDescricao(item.getServico().getDescricao());
        dto.setValor(item.getValorUnitario());
        return dto;
    }

    private ItemVendaDto itemParaResposta(ItemVenda item) {
        ItemVendaDto dto = new ItemVendaDto();
        dto.setId(item.getId());
        dto.setMercadoriaId(item.getMercadoria().getId());
        dto.setNome(item.getMercadoria().getNome());
        dto.setQuantidade(item.getQuantidade());
        dto.setValorUnitario(item.getValorUnitario());
        return dto;
    }
}
