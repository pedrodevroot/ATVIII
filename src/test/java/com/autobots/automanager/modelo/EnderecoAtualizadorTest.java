package com.autobots.automanager.modelo;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.autobots.automanager.entidades.Endereco;

class EnderecoAtualizadorTest {

    private final EnderecoAtualizador atualizador = new EnderecoAtualizador();

    private Endereco enderecoCompleto() {
        Endereco endereco = new Endereco();
        endereco.setEstado("Rio de Janeiro");
        endereco.setCidade("Rio de Janeiro");
        endereco.setBairro("Copacabana");
        endereco.setRua("Avenida Atlantica");
        endereco.setNumero("1702");
        endereco.setCodigoPostal("22021001");
        endereco.setInformacoesAdicionais("Hotel");
        return endereco;
    }

    @Test
    @DisplayName("atualiza todos os campos enviados")
    void atualizaTodosOsCampos() {
        Endereco existente = enderecoCompleto();
        Endereco atualizacao = new Endereco();
        atualizacao.setEstado("Sao Paulo");
        atualizacao.setCidade("Santos");
        atualizacao.setBairro("Gonzaga");
        atualizacao.setRua("Avenida Ana Costa");
        atualizacao.setNumero("250");
        atualizacao.setCodigoPostal("11060002");
        atualizacao.setInformacoesAdicionais("Apartamento 32");

        atualizador.atualizar(existente, atualizacao);

        assertEquals("Sao Paulo", existente.getEstado());
        assertEquals("Santos", existente.getCidade());
        assertEquals("Gonzaga", existente.getBairro());
        assertEquals("Avenida Ana Costa", existente.getRua());
        assertEquals("250", existente.getNumero());
        assertEquals("11060002", existente.getCodigoPostal());
        assertEquals("Apartamento 32", existente.getInformacoesAdicionais());
    }

    @Test
    @DisplayName("REGRESSAO P7: atualiza o codigoPostal, que antes era ignorado")
    void atualizaCodigoPostal() {
        Endereco existente = enderecoCompleto();
        Endereco atualizacao = new Endereco();
        atualizacao.setCodigoPostal("11015200");

        atualizador.atualizar(existente, atualizacao);

        assertEquals("11015200", existente.getCodigoPostal());
    }

    @Test
    @DisplayName("atualizacao parcial preserva os campos ausentes")
    void atualizacaoParcialPreservaOsDemais() {
        Endereco existente = enderecoCompleto();
        Endereco atualizacao = new Endereco();
        atualizacao.setCidade("Santos");

        atualizador.atualizar(existente, atualizacao);

        assertEquals("Santos", existente.getCidade());
        assertEquals("Copacabana", existente.getBairro());
        assertEquals("Avenida Atlantica", existente.getRua());
        assertEquals("1702", existente.getNumero());
        assertEquals("22021001", existente.getCodigoPostal());
    }

    @Test
    @DisplayName("ignora atualizacao nula")
    void ignoraAtualizacaoNula() {
        Endereco existente = enderecoCompleto();

        atualizador.atualizar(existente, null);

        assertEquals("Rio de Janeiro", existente.getCidade());
    }

    @Test
    @DisplayName("REGRESSAO P17: nao estoura NullPointerException quando o endereco existente e nulo")
    void naoEstouraComEnderecoExistenteNulo() {
        Endereco atualizacao = new Endereco();
        atualizacao.setCidade("Santos");

        assertDoesNotThrow(() -> atualizador.atualizar(null, atualizacao));
    }
}
