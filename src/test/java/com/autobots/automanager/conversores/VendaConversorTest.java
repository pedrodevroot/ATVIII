package com.autobots.automanager.conversores;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.autobots.automanager.dtos.VendaRespostaDto;
import com.autobots.automanager.entidades.ItemServico;
import com.autobots.automanager.entidades.ItemVenda;
import com.autobots.automanager.entidades.Mercadoria;
import com.autobots.automanager.entidades.Servico;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.entidades.Veiculo;
import com.autobots.automanager.entidades.Venda;

class VendaConversorTest {

    private final VendaConversor conversor = new VendaConversor();

    private Usuario usuario(long id) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        return usuario;
    }

    private Venda venda() {
        Venda venda = new Venda();
        venda.setId(9L);
        venda.setCadastro(new Date(1000L));
        venda.setIdentificacao("V-1");
        venda.setCliente(usuario(3L));
        venda.setFuncionario(usuario(1L));
        return venda;
    }

    @Test
    @DisplayName("resposta: expoe os ids dos envolvidos e os itens vendidos, tambem em lista")
    void resposta() {
        Veiculo veiculo = new Veiculo();
        veiculo.setId(5L);
        Mercadoria mercadoria = new Mercadoria();
        mercadoria.setId(10L);
        mercadoria.setNome("Pneu");
        mercadoria.setQuantidade(30);
        ItemVenda item = new ItemVenda();
        item.setId(30L);
        item.setMercadoria(mercadoria);
        item.setQuantidade(4);
        item.setValorUnitario(new BigDecimal("280.00"));
        Servico servico = new Servico();
        servico.setId(20L);
        servico.setNome("Alinhamento");
        servico.setValor(new BigDecimal("999.00"));
        ItemServico itemServico = new ItemServico();
        itemServico.setServico(servico);
        itemServico.setValorUnitario(new BigDecimal("50.00"));
        Venda venda = venda();
        venda.setVeiculo(veiculo);
        venda.getItens().add(item);
        venda.getItensServico().add(itemServico);

        List<VendaRespostaDto> dtos = conversor.paraResposta(List.of(venda));

        assertEquals(1, dtos.size());
        VendaRespostaDto dto = dtos.get(0);
        assertEquals(9L, dto.getId());
        assertEquals(new Date(1000L), dto.getCadastro());
        assertEquals("V-1", dto.getIdentificacao());
        assertEquals(3L, dto.getClienteId());
        assertEquals(1L, dto.getFuncionarioId());
        assertEquals(5L, dto.getVeiculoId());
        assertEquals(30L, dto.getItens().get(0).getId());
        assertEquals(10L, dto.getItens().get(0).getMercadoriaId());
        assertEquals("Pneu", dto.getItens().get(0).getNome());
        assertEquals(4L, dto.getItens().get(0).getQuantidade());
        assertEquals(new BigDecimal("280.00"), dto.getItens().get(0).getValorUnitario());
        assertEquals(20L, dto.getServicos().get(0).getId());
        assertEquals("Alinhamento", dto.getServicos().get(0).getNome());
        assertEquals(new BigDecimal("50.00"), dto.getServicos().get(0).getValor());
        assertEquals(new BigDecimal("1170.00"), dto.getValorTotal());
    }

    @Test
    @DisplayName("resposta: o veiculo e opcional")
    void respostaSemVeiculo() {
        assertNull(conversor.paraResposta(venda()).getVeiculoId());
    }

    @Test
    @DisplayName("resposta: venda sem itens tem total zero")
    void totalZero() {
        assertEquals(0, BigDecimal.ZERO.compareTo(conversor.paraResposta(venda()).getValorTotal()));
    }
}
