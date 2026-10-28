package com.autobots.automanager.conversores;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.autobots.automanager.dtos.DocumentoAtualizacaoDto;
import com.autobots.automanager.dtos.DocumentoCadastroDto;
import com.autobots.automanager.dtos.DocumentoDto;
import com.autobots.automanager.entidades.Documento;

class DocumentoConversorTest {

    private final DocumentoConversor conversor = new DocumentoConversor();

    @Test
    @DisplayName("cadastro: copia tipo e numero, e a entidade nasce sem id")
    void cadastroNasceSemId() {
        DocumentoCadastroDto dto = new DocumentoCadastroDto();
        dto.setTipo("RG");
        dto.setNumero("1500");

        Documento documento = conversor.paraEntidade(dto);

        assertNull(documento.getId());
        assertEquals("RG", documento.getTipo());
        assertEquals("1500", documento.getNumero());
    }

    @Test
    @DisplayName("atualizacao: copia os campos e nao carrega id (ele vem da URL)")
    void atualizacaoNaoCarregaId() {
        DocumentoAtualizacaoDto dto = new DocumentoAtualizacaoDto();
        dto.setTipo("CNH");

        Documento documento = conversor.paraEntidade(dto);

        assertNull(documento.getId());
        assertEquals("CNH", documento.getTipo());
        assertNull(documento.getNumero());
    }

    @Test
    @DisplayName("resposta: expoe o id")
    void respostaExpoeId() {
        Documento documento = new Documento();
        documento.setId(9L);
        documento.setTipo("CPF");
        documento.setNumero("00000000001");

        DocumentoDto dto = conversor.paraResposta(documento);

        assertEquals(9L, dto.getId());
        assertEquals("CPF", dto.getTipo());
        assertEquals("00000000001", dto.getNumero());
    }

    @Test
    @DisplayName("resposta em lista: converte todos os itens")
    void respostaEmLista() {
        Documento primeiro = new Documento();
        primeiro.setId(1L);
        Documento segundo = new Documento();
        segundo.setId(2L);

        List<DocumentoDto> dtos = conversor.paraResposta(List.of(primeiro, segundo));

        assertEquals(2, dtos.size());
        assertEquals(1L, dtos.get(0).getId());
        assertEquals(2L, dtos.get(1).getId());
    }
}
