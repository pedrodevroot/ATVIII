package com.autobots.automanager.conversores;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.autobots.automanager.dtos.VendaRespostaDto;
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
        Servico servico = new Servico();
        servico.setId(20L);
        Venda venda = venda();
        venda.setVeiculo(veiculo);
        venda.getMercadorias().add(mercadoria);
        venda.getServicos().add(servico);

        List<VendaRespostaDto> dtos = conversor.paraResposta(List.of(venda));

        assertEquals(1, dtos.size());
        VendaRespostaDto dto = dtos.get(0);
        assertEquals(9L, dto.getId());
        assertEquals(new Date(1000L), dto.getCadastro());
        assertEquals("V-1", dto.getIdentificacao());
        assertEquals(3L, dto.getClienteId());
        assertEquals(1L, dto.getFuncionarioId());
        assertEquals(5L, dto.getVeiculoId());
        assertEquals(10L, dto.getMercadorias().get(0).getId());
        assertEquals(20L, dto.getServicos().get(0).getId());
    }

    @Test
    @DisplayName("resposta: o veiculo e opcional")
    void respostaSemVeiculo() {
        assertNull(conversor.paraResposta(venda()).getVeiculoId());
    }
}
