package com.autobots.automanager.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.autobots.automanager.entidades.Telefone;

class TelefoneAtualizadorTest {

    private final TelefoneAtualizador atualizador = new TelefoneAtualizador();

    private Telefone telefone(Long id, String ddd, String numero) {
        Telefone telefone = new Telefone();
        telefone.setId(id);
        telefone.setDdd(ddd);
        telefone.setNumero(numero);
        return telefone;
    }

    @Test
    @DisplayName("atualiza os campos enviados")
    void atualizaCamposEnviados() {
        Telefone existente = telefone(1L, "21", "981234576");

        atualizador.atualizar(existente, telefone(1L, "11", "999998888"));

        assertEquals("11", existente.getDdd());
        assertEquals("999998888", existente.getNumero());
    }

    @Test
    @DisplayName("nao sobrescreve campo ausente na atualizacao")
    void naoSobrescreveCampoAusente() {
        Telefone existente = telefone(1L, "21", "981234576");

        atualizador.atualizar(existente, telefone(1L, "11", null));

        assertEquals("11", existente.getDdd());
        assertEquals("981234576", existente.getNumero());
    }

    @Test
    @DisplayName("nao sobrescreve campo em branco na atualizacao")
    void naoSobrescreveCampoEmBranco() {
        Telefone existente = telefone(1L, "21", "981234576");

        atualizador.atualizar(existente, telefone(1L, "   ", ""));

        assertEquals("21", existente.getDdd());
        assertEquals("981234576", existente.getNumero());
    }

    @Test
    @DisplayName("ignora atualizacao nula")
    void ignoraAtualizacaoNula() {
        Telefone existente = telefone(1L, "21", "981234576");

        atualizador.atualizar(existente, null);

        assertEquals("21", existente.getDdd());
    }

    @Test
    @DisplayName("na lista, casa telefone com id DENTRO do cache de Long (1..127)")
    void listaComIdPequeno() {
        Telefone existente = telefone(1L, "21", "981234576");

        atualizador.atualizar(List.of(existente), List.of(telefone(1L, "11", "999998888")));

        assertEquals("11", existente.getDdd());
        assertEquals("999998888", existente.getNumero());
    }

    @Test
    @DisplayName("REGRESSAO: na lista, casa telefone com id FORA do cache de Long (>= 128)")
    void listaComIdForaDoCacheDeLong() {
        Telefone existente = telefone(200L, "21", "981234576");

        atualizador.atualizar(List.of(existente), List.of(telefone(200L, "11", "999998888")));

        assertEquals("11", existente.getDdd());
        assertEquals("999998888", existente.getNumero());
    }

    @Test
    @DisplayName("na lista, nao altera telefone de id diferente")
    void listaNaoAlteraIdDiferente() {
        Telefone existente = telefone(200L, "21", "981234576");

        atualizador.atualizar(List.of(existente), List.of(telefone(201L, "11", "999998888")));

        assertEquals("21", existente.getDdd());
        assertEquals("981234576", existente.getNumero());
    }

    @Test
    @DisplayName("na lista, ignora atualizacao sem id")
    void listaIgnoraAtualizacaoSemId() {
        Telefone existente = telefone(1L, "21", "981234576");

        atualizador.atualizar(List.of(existente), List.of(telefone(null, "11", "999998888")));

        assertEquals("21", existente.getDdd());
    }
}
