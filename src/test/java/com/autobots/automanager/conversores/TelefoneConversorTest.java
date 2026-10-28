package com.autobots.automanager.conversores;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.autobots.automanager.dtos.TelefoneAtualizacaoDto;
import com.autobots.automanager.dtos.TelefoneCadastroDto;
import com.autobots.automanager.dtos.TelefoneDto;
import com.autobots.automanager.entidades.Telefone;

class TelefoneConversorTest {

    private final TelefoneConversor conversor = new TelefoneConversor();

    @Test
    @DisplayName("cadastro: copia ddd e numero, e a entidade nasce sem id")
    void cadastroNasceSemId() {
        TelefoneCadastroDto dto = new TelefoneCadastroDto();
        dto.setDdd("13");
        dto.setNumero("981234567");

        Telefone telefone = conversor.paraEntidade(dto);

        assertNull(telefone.getId());
        assertEquals("13", telefone.getDdd());
        assertEquals("981234567", telefone.getNumero());
    }

    @Test
    @DisplayName("atualizacao: copia os campos e nao carrega id (ele vem da URL)")
    void atualizacaoNaoCarregaId() {
        TelefoneAtualizacaoDto dto = new TelefoneAtualizacaoDto();
        dto.setDdd("11");

        Telefone telefone = conversor.paraEntidade(dto);

        assertNull(telefone.getId());
        assertEquals("11", telefone.getDdd());
        assertNull(telefone.getNumero());
    }

    @Test
    @DisplayName("resposta: expoe o id")
    void respostaExpoeId() {
        Telefone telefone = new Telefone();
        telefone.setId(7L);
        telefone.setDdd("21");
        telefone.setNumero("981234576");

        TelefoneDto dto = conversor.paraResposta(telefone);

        assertEquals(7L, dto.getId());
        assertEquals("21", dto.getDdd());
        assertEquals("981234576", dto.getNumero());
    }

    @Test
    @DisplayName("resposta em lista: converte todos os itens")
    void respostaEmLista() {
        Telefone primeiro = new Telefone();
        primeiro.setId(1L);
        primeiro.setDdd("21");
        Telefone segundo = new Telefone();
        segundo.setId(2L);
        segundo.setDdd("13");

        List<TelefoneDto> dtos = conversor.paraResposta(List.of(primeiro, segundo));

        assertEquals(2, dtos.size());
        assertEquals(1L, dtos.get(0).getId());
        assertEquals(2L, dtos.get(1).getId());
    }
}
