package com.autobots.automanager.modelo;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StringVerificadorNuloTest {

    private final StringVerificadorNulo verificador = new StringVerificadorNulo();

    @Test
    @DisplayName("considera nulo o valor null")
    void nulo() {
        assertTrue(verificador.verificar(null));
    }

    @Test
    @DisplayName("considera nulo o texto vazio")
    void vazio() {
        assertTrue(verificador.verificar(""));
    }

    @Test
    @DisplayName("considera nulo o texto com apenas espacos")
    void apenasEspacos() {
        assertTrue(verificador.verificar("     "));
    }

    @Test
    @DisplayName("nao considera nulo um texto com conteudo")
    void comConteudo() {
        assertFalse(verificador.verificar("Dom Pedro"));
    }
}
