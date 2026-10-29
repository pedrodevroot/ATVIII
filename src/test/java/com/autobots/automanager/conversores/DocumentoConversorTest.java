package com.autobots.automanager.conversores;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.autobots.automanager.dtos.DocumentoAtualizacaoDto;
import com.autobots.automanager.dtos.DocumentoCadastroDto;
import com.autobots.automanager.dtos.DocumentoDto;
import com.autobots.automanager.entidades.Documento;
import com.autobots.automanager.enumeracoes.TipoDocumento;

class DocumentoConversorTest {

    private final DocumentoConversor conversor = new DocumentoConversor();

    @Test
    @DisplayName("cadastro: copia tipo, data de emissao e numero, e a entidade nasce sem id")
    void cadastroNasceSemId() {
        Date emissao = new Date(0L);
        DocumentoCadastroDto dto = new DocumentoCadastroDto();
        dto.setTipo(TipoDocumento.RG);
        dto.setDataEmissao(emissao);
        dto.setNumero("1500");

        Documento documento = conversor.paraEntidade(dto);

        assertNull(documento.getId());
        assertEquals(TipoDocumento.RG, documento.getTipo());
        assertEquals(emissao, documento.getDataEmissao());
        assertEquals("1500", documento.getNumero());
    }

    @Test
    @DisplayName("atualizacao: copia os campos e nao carrega id (ele vem da URL)")
    void atualizacaoNaoCarregaId() {
        DocumentoAtualizacaoDto dto = new DocumentoAtualizacaoDto();
        dto.setTipo(TipoDocumento.CNH);

        Documento documento = conversor.paraEntidade(dto);

        assertNull(documento.getId());
        assertEquals(TipoDocumento.CNH, documento.getTipo());
        assertNull(documento.getNumero());
        assertNull(documento.getDataEmissao());
    }

    @Test
    @DisplayName("item da lista do usuario: PRESERVA o id, pois o atualizador casa por id")
    void itemDaListaPreservaId() {
        DocumentoDto dto = new DocumentoDto();
        dto.setId(200L);
        dto.setNumero("998877");

        Documento documento = conversor.paraEntidade(dto);

        assertEquals(200L, documento.getId());
        assertEquals("998877", documento.getNumero());
    }

    @Test
    @DisplayName("resposta: expoe o id e a data de emissao")
    void respostaExpoeId() {
        Date emissao = new Date(0L);
        Documento documento = new Documento();
        documento.setId(9L);
        documento.setTipo(TipoDocumento.CPF);
        documento.setDataEmissao(emissao);
        documento.setNumero("00000000001");

        DocumentoDto dto = conversor.paraResposta(documento);

        assertEquals(9L, dto.getId());
        assertEquals(TipoDocumento.CPF, dto.getTipo());
        assertEquals(emissao, dto.getDataEmissao());
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
