package com.autobots.automanager.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.Date;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.autobots.automanager.dtos.MercadoriaAtualizacaoDto;
import com.autobots.automanager.entidades.Mercadoria;

class MercadoriaAtualizadorTest {

    private final MercadoriaAtualizador atualizador = new MercadoriaAtualizador();

    private Mercadoria existente() {
        Mercadoria mercadoria = new Mercadoria();
        mercadoria.setNome("Pneu");
        mercadoria.setDescricao("Aro 15");
        mercadoria.setFabricacao(new Date(1000L));
        mercadoria.setValidade(new Date(2000L));
        mercadoria.setQuantidade(10);
        mercadoria.setValor(new BigDecimal("300.00"));
        return mercadoria;
    }

    @Test
    @DisplayName("altera todos os campos informados")
    void alteraCampos() {
        Mercadoria mercadoria = existente();
        MercadoriaAtualizacaoDto atualizacao = new MercadoriaAtualizacaoDto();
        atualizacao.setNome("Pneu novo");
        atualizacao.setDescricao("Aro 16");
        atualizacao.setFabricacao(new Date(3000L));
        atualizacao.setValidade(new Date(4000L));
        atualizacao.setQuantidade(20L);
        atualizacao.setValor(new BigDecimal("350.00"));

        atualizador.atualizar(mercadoria, atualizacao);

        assertEquals("Pneu novo", mercadoria.getNome());
        assertEquals("Aro 16", mercadoria.getDescricao());
        assertEquals(new Date(3000L), mercadoria.getFabricacao());
        assertEquals(new Date(4000L), mercadoria.getValidade());
        assertEquals(20L, mercadoria.getQuantidade());
        assertEquals(new BigDecimal("350.00"), mercadoria.getValor());
    }

    @Test
    @DisplayName("quantidade zero e aplicada, pois zerar o estoque e uma alteracao valida")
    void quantidadeZero() {
        Mercadoria mercadoria = existente();
        MercadoriaAtualizacaoDto atualizacao = new MercadoriaAtualizacaoDto();
        atualizacao.setQuantidade(0L);

        atualizador.atualizar(mercadoria, atualizacao);

        assertEquals(0L, mercadoria.getQuantidade());
    }

    @Test
    @DisplayName("campos nulos ou em branco nao alteram a mercadoria")
    void ignoraNuloEBranco() {
        Mercadoria mercadoria = existente();
        MercadoriaAtualizacaoDto atualizacao = new MercadoriaAtualizacaoDto();
        atualizacao.setNome("  ");

        atualizador.atualizar(mercadoria, atualizacao);

        assertEquals("Pneu", mercadoria.getNome());
        assertEquals("Aro 15", mercadoria.getDescricao());
        assertEquals(new Date(1000L), mercadoria.getFabricacao());
        assertEquals(new Date(2000L), mercadoria.getValidade());
        assertEquals(10L, mercadoria.getQuantidade());
        assertEquals(new BigDecimal("300.00"), mercadoria.getValor());
    }
}
