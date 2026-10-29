package com.autobots.automanager.conversores;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.autobots.automanager.dtos.EmailAtualizacaoDto;
import com.autobots.automanager.dtos.EmailCadastroDto;
import com.autobots.automanager.dtos.EmailDto;
import com.autobots.automanager.entidades.Email;

class EmailConversorTest {

    private final EmailConversor conversor = new EmailConversor();

    @Test
    @DisplayName("cadastro: copia o endereco, e a entidade nasce sem id")
    void cadastroNasceSemId() {
        EmailCadastroDto dto = new EmailCadastroDto();
        dto.setEndereco("maria@email.com");

        Email email = conversor.paraEntidade(dto);

        assertNull(email.getId());
        assertEquals("maria@email.com", email.getEndereco());
    }

    @Test
    @DisplayName("atualizacao: copia o endereco e nao carrega id (ele vem da URL)")
    void atualizacaoNaoCarregaId() {
        EmailAtualizacaoDto dto = new EmailAtualizacaoDto();
        dto.setEndereco("nova@email.com");

        Email email = conversor.paraEntidade(dto);

        assertNull(email.getId());
        assertEquals("nova@email.com", email.getEndereco());
    }

    @Test
    @DisplayName("item da lista do usuario: PRESERVA o id, pois o atualizador casa por id")
    void itemDaListaPreservaId() {
        EmailDto dto = new EmailDto();
        dto.setId(200L);
        dto.setEndereco("nova@email.com");

        assertEquals(200L, conversor.paraEntidade(dto).getId());
    }

    @Test
    @DisplayName("resposta: expoe id e endereco, tambem em lista")
    void resposta() {
        Email email = new Email();
        email.setId(9L);
        email.setEndereco("maria@email.com");

        List<EmailDto> dtos = conversor.paraResposta(List.of(email));

        assertEquals(1, dtos.size());
        assertEquals(9L, dtos.get(0).getId());
        assertEquals("maria@email.com", dtos.get(0).getEndereco());
    }
}
