package com.autobots.automanager.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.autobots.automanager.entidades.Servico;

class ServicoAtualizadorTest {

    private final ServicoAtualizador atualizador = new ServicoAtualizador();

    private Servico existente() {
        Servico servico = new Servico();
        servico.setNome("Alinhamento");
        servico.setDescricao("Alinhamento das rodas");
        servico.setValor(new BigDecimal("50.00"));
        return servico;
    }

    @Test
    @DisplayName("altera todos os campos informados")
    void alteraCampos() {
        Servico servico = existente();
        Servico atualizacao = new Servico();
        atualizacao.setNome("Balanceamento");
        atualizacao.setDescricao("Balanceamento das rodas");
        atualizacao.setValor(new BigDecimal("30.00"));

        atualizador.atualizar(servico, atualizacao);

        assertEquals("Balanceamento", servico.getNome());
        assertEquals("Balanceamento das rodas", servico.getDescricao());
        assertEquals(new BigDecimal("30.00"), servico.getValor());
    }

    @Test
    @DisplayName("campos nulos ou em branco nao alteram o servico")
    void ignoraNuloEBranco() {
        Servico servico = existente();
        Servico atualizacao = new Servico();
        atualizacao.setDescricao("  ");

        atualizador.atualizar(servico, atualizacao);

        assertEquals("Alinhamento", servico.getNome());
        assertEquals("Alinhamento das rodas", servico.getDescricao());
        assertEquals(new BigDecimal("50.00"), servico.getValor());
    }
}
