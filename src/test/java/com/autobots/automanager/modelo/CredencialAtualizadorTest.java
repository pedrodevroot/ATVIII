package com.autobots.automanager.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.autobots.automanager.dtos.CredencialAtualizacaoDto;
import com.autobots.automanager.entidades.CredencialCodigoBarra;
import com.autobots.automanager.entidades.CredencialUsuarioSenha;

class CredencialAtualizadorTest {

    private final CredencialAtualizador atualizador = new CredencialAtualizador();

    private CredencialUsuarioSenha usuarioSenha() {
        CredencialUsuarioSenha credencial = new CredencialUsuarioSenha();
        credencial.setNomeUsuario("maria");
        credencial.setSenha("segredo1");
        return credencial;
    }

    private CredencialCodigoBarra codigoBarra() {
        CredencialCodigoBarra credencial = new CredencialCodigoBarra();
        credencial.setCodigo(111L);
        return credencial;
    }

    @Test
    @DisplayName("desativa e reativa a credencial, de qualquer tipo")
    void alteraInativo() {
        CredencialCodigoBarra credencial = codigoBarra();
        CredencialAtualizacaoDto atualizacao = new CredencialAtualizacaoDto();

        atualizacao.setInativo(true);
        atualizador.atualizar(credencial, atualizacao);
        assertTrue(credencial.isInativo());

        atualizacao.setInativo(false);
        atualizador.atualizar(credencial, atualizacao);
        assertFalse(credencial.isInativo());
    }

    @Test
    @DisplayName("troca a senha de usuario e senha sem mexer no nome nem no inativo")
    void trocaSenha() {
        CredencialUsuarioSenha credencial = usuarioSenha();
        CredencialAtualizacaoDto atualizacao = new CredencialAtualizacaoDto();
        atualizacao.setSenha("novaSenha");

        atualizador.atualizar(credencial, atualizacao);

        assertEquals("novaSenha", credencial.getSenha());
        assertEquals("maria", credencial.getNomeUsuario());
        assertFalse(credencial.isInativo());
    }

    @Test
    @DisplayName("troca o codigo de barras")
    void trocaCodigo() {
        CredencialCodigoBarra credencial = codigoBarra();
        CredencialAtualizacaoDto atualizacao = new CredencialAtualizacaoDto();
        atualizacao.setCodigo(222L);

        atualizador.atualizar(credencial, atualizacao);

        assertEquals(222L, credencial.getCodigo());
    }

    @Test
    @DisplayName("ignora campo que nao pertence ao tipo da credencial")
    void ignoraCampoDeOutroTipo() {
        CredencialUsuarioSenha usuarioSenha = usuarioSenha();
        CredencialCodigoBarra codigoBarra = codigoBarra();
        CredencialAtualizacaoDto atualizacao = new CredencialAtualizacaoDto();
        atualizacao.setCodigo(999L);
        atualizacao.setNomeUsuario("outro");

        atualizador.atualizar(usuarioSenha, atualizacao);
        atualizacao.setNomeUsuario(null);
        atualizador.atualizar(codigoBarra, atualizacao);

        assertEquals("outro", usuarioSenha.getNomeUsuario());
        assertEquals(999L, codigoBarra.getCodigo());
        assertEquals("segredo1", usuarioSenha.getSenha());
    }
}
