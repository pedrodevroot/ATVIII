package com.autobots.automanager.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.autobots.automanager.entidades.Documento;

class DocumentoAtualizadorTest {

    private final DocumentoAtualizador atualizador = new DocumentoAtualizador();

    private Documento documento(Long id, String tipo, String numero) {
        Documento documento = new Documento();
        documento.setId(id);
        documento.setTipo(tipo);
        documento.setNumero(numero);
        return documento;
    }

    @Test
    @DisplayName("atualiza os campos enviados")
    void atualizaCamposEnviados() {
        Documento existente = documento(1L, "RG", "1500");

        atualizador.atualizar(existente, documento(1L, "CNH", "998877"));

        assertEquals("CNH", existente.getTipo());
        assertEquals("998877", existente.getNumero());
    }

    @Test
    @DisplayName("nao sobrescreve campo ausente na atualizacao")
    void naoSobrescreveCampoAusente() {
        Documento existente = documento(1L, "RG", "1500");

        atualizador.atualizar(existente, documento(1L, "CNH", null));

        assertEquals("CNH", existente.getTipo());
        assertEquals("1500", existente.getNumero());
    }

    @Test
    @DisplayName("ignora atualizacao nula")
    void ignoraAtualizacaoNula() {
        Documento existente = documento(1L, "RG", "1500");

        atualizador.atualizar(existente, null);

        assertEquals("RG", existente.getTipo());
    }

    @Test
    @DisplayName("na lista, casa documento com id DENTRO do cache de Long (1..127)")
    void listaComIdPequeno() {
        Documento existente = documento(1L, "RG", "1500");

        atualizador.atualizar(List.of(existente), List.of(documento(1L, "CNH", "998877")));

        assertEquals("CNH", existente.getTipo());
        assertEquals("998877", existente.getNumero());
    }

    @Test
    @DisplayName("REGRESSAO: na lista, casa documento com id FORA do cache de Long (>= 128)")
    void listaComIdForaDoCacheDeLong() {
        Documento existente = documento(200L, "RG", "1500");

        atualizador.atualizar(List.of(existente), List.of(documento(200L, "CNH", "998877")));

        assertEquals("CNH", existente.getTipo());
        assertEquals("998877", existente.getNumero());
    }

    @Test
    @DisplayName("na lista, nao altera documento de id diferente")
    void listaNaoAlteraIdDiferente() {
        Documento existente = documento(200L, "RG", "1500");

        atualizador.atualizar(List.of(existente), List.of(documento(201L, "CNH", "998877")));

        assertEquals("RG", existente.getTipo());
        assertEquals("1500", existente.getNumero());
    }
}
