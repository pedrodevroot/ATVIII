package com.autobots.automanager.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.autobots.automanager.entidades.Email;

class EmailAtualizadorTest {

    private final EmailAtualizador atualizador = new EmailAtualizador();

    private Email email(Long id, String endereco) {
        Email email = new Email();
        email.setId(id);
        email.setEndereco(endereco);
        return email;
    }

    @Test
    @DisplayName("atualiza o endereco enviado")
    void atualizaEndereco() {
        Email existente = email(1L, "maria@email.com");

        atualizador.atualizar(existente, email(1L, "nova@email.com"));

        assertEquals("nova@email.com", existente.getEndereco());
    }

    @Test
    @DisplayName("nao sobrescreve com endereco em branco ou atualizacao nula")
    void naoSobrescreveVazio() {
        Email existente = email(1L, "maria@email.com");

        atualizador.atualizar(existente, email(1L, "  "));
        atualizador.atualizar(existente, null);

        assertEquals("maria@email.com", existente.getEndereco());
    }

    @Test
    @DisplayName("REGRESSAO: na lista, casa e-mail com id FORA do cache de Long (>= 128)")
    void listaComIdForaDoCacheDeLong() {
        Email existente = email(200L, "maria@email.com");

        atualizador.atualizar(List.of(existente), List.of(email(200L, "nova@email.com")));

        assertEquals("nova@email.com", existente.getEndereco());
    }

    @Test
    @DisplayName("na lista, ignora e-mail sem id ou de id diferente")
    void listaIgnoraSemIdOuIdDiferente() {
        Email existente = email(200L, "maria@email.com");

        atualizador.atualizar(List.of(existente),
                List.of(email(null, "sem-id@email.com"), email(201L, "outro@email.com")));

        assertEquals("maria@email.com", existente.getEndereco());
    }
}
