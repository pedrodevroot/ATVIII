package com.autobots.automanager.controles;

import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Calendar;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.autobots.automanager.conversores.DocumentoConversor;
import com.autobots.automanager.entidades.Documento;
import com.autobots.automanager.enumeracoes.TipoDocumento;
import com.autobots.automanager.excecoes.RecursoNaoEncontradoException;
import com.autobots.automanager.modelo.AdicionadorLinkDocumento;
import com.autobots.automanager.servicos.DocumentoServico;

@WebMvcTest(DocumentoControle.class)
@Import({ DocumentoConversor.class, AdicionadorLinkDocumento.class })
class DocumentoControleTest {

    @Autowired
    private MockMvc requisicao;

    @MockBean
    private DocumentoServico servico;

    private Documento documento(Long id, TipoDocumento tipo, String numero) {
        Documento documento = new Documento();
        documento.setId(id);
        documento.setTipo(tipo);
        documento.setNumero(numero);
        Calendar calendario = Calendar.getInstance();
        calendario.set(2010, Calendar.MARCH, 1, 0, 0, 0);
        documento.setDataEmissao(calendario.getTime());
        return documento;
    }

    @Test
    @DisplayName("GET /usuario/{id}/documentos: 200 com a lista")
    void listarOk() throws Exception {
        when(servico.listarPorUsuario(1L)).thenReturn(List.of(documento(1L, TipoDocumento.RG, "1500")));

        requisicao.perform(get("/usuario/1/documentos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].tipo").value("RG"))
                .andExpect(jsonPath("$[0].dataEmissao").exists());
    }

    @Test
    @DisplayName("GET /usuario/{id}/documentos: 404 quando o usuario nao existe")
    void listarUsuarioInexistente() throws Exception {
        when(servico.listarPorUsuario(999L))
                .thenThrow(new RecursoNaoEncontradoException("Usuario", 999L));

        requisicao.perform(get("/usuario/999/documentos"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /usuario/{id}/documentos: 201 com header Location")
    void criarOk() throws Exception {
        when(servico.criar(anyLong(), any(Documento.class)))
                .thenReturn(documento(5L, TipoDocumento.CNH, "55443322"));

        String corpo = """
                { "tipo": "CNH", "numero": "55443322", "dataEmissao": "2015-06-10" }
                """;

        requisicao.perform(post("/usuario/1/documentos")
                .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/documento/5"))
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    @DisplayName("POST /usuario/{id}/documentos: 409 quando o numero ja existe")
    void criarNumeroDuplicado() throws Exception {
        when(servico.criar(anyLong(), any(Documento.class)))
                .thenThrow(new DataIntegrityViolationException("unique violation"));

        String corpo = """
                { "tipo": "RG", "numero": "1500", "dataEmissao": "2015-06-10" }
                """;

        requisicao.perform(post("/usuario/1/documentos")
                .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("POST /usuario/{id}/documentos: 400 quando tipo, numero e data de emissao nao sao informados")
    void criarSemCampos() throws Exception {
        String corpo = """
                { }
                """;

        requisicao.perform(post("/usuario/1/documentos")
                .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros[*].campo", hasItem("tipo")))
                .andExpect(jsonPath("$.erros[*].campo", hasItem("numero")))
                .andExpect(jsonPath("$.erros[*].campo", hasItem("dataEmissao")));

        verifyNoInteractions(servico);
    }

    @Test
    @DisplayName("POST /usuario/{id}/documentos: 400 com os valores aceitos quando o tipo nao existe")
    void criarTipoInexistente() throws Exception {
        String corpo = """
                { "tipo": "TITULO", "numero": "123", "dataEmissao": "2015-06-10" }
                """;

        requisicao.perform(post("/usuario/1/documentos")
                .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value(
                        "O valor TITULO nao e aceito. Valores aceitos: [CPF, CNPJ, RG, CNH, PASSAPORTE]."));

        verifyNoInteractions(servico);
    }

    @Test
    @DisplayName("POST /usuario/{id}/documentos: 400 quando a data de emissao esta no futuro")
    void criarDataEmissaoFutura() throws Exception {
        String corpo = """
                { "tipo": "RG", "numero": "123", "dataEmissao": "2099-01-01" }
                """;

        requisicao.perform(post("/usuario/1/documentos")
                .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros[*].campo", hasItem("dataEmissao")));

        verifyNoInteractions(servico);
    }

    @Test
    @DisplayName("GET /documento/{id}: 200 com o documento")
    void obterPorIdOk() throws Exception {
        when(servico.obterPorId(2L)).thenReturn(documento(2L, TipoDocumento.CPF, "00000000001"));

        requisicao.perform(get("/documento/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("CPF"));
    }

    @Test
    @DisplayName("GET /documento/{id}: 404 quando nao existe")
    void obterPorIdInexistente() throws Exception {
        when(servico.obterPorId(999L))
                .thenThrow(new RecursoNaoEncontradoException("Documento", 999L));

        requisicao.perform(get("/documento/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Documento de id 999 nao foi encontrado"));
    }

    @Test
    @DisplayName("PUT /documento/{id}: 200 aceitando atualizacao parcial")
    void atualizarParcial() throws Exception {
        when(servico.atualizar(anyLong(), any(Documento.class)))
                .thenReturn(documento(2L, TipoDocumento.CNH, "1500"));

        String corpo = """
                { "tipo": "CNH" }
                """;

        requisicao.perform(put("/documento/2")
                .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("CNH"))
                .andExpect(jsonPath("$.numero").value("1500"));
    }

    @Test
    @DisplayName("PUT /documento/{id}: 409 quando o numero colide com outro documento")
    void atualizarNumeroDuplicado() throws Exception {
        when(servico.atualizar(anyLong(), any(Documento.class)))
                .thenThrow(new DataIntegrityViolationException("unique violation"));

        String corpo = """
                { "numero": "1500" }
                """;

        requisicao.perform(put("/documento/2")
                .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("PUT /documento/{id}: 404 quando nao existe")
    void atualizarInexistente() throws Exception {
        when(servico.atualizar(anyLong(), any(Documento.class)))
                .thenThrow(new RecursoNaoEncontradoException("Documento", 999L));

        String corpo = """
                { "tipo": "RG" }
                """;

        requisicao.perform(put("/documento/999")
                .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /documento/{id}: 204 sem corpo")
    void excluirOk() throws Exception {
        requisicao.perform(delete("/documento/2"))
                .andExpect(status().isNoContent());

        verify(servico).excluir(2L);
    }

    @Test
    @DisplayName("DELETE /documento/{id}: 404 quando nao existe")
    void excluirInexistente() throws Exception {
        doThrow(new RecursoNaoEncontradoException("Documento", 999L)).when(servico).excluir(999L);

        requisicao.perform(delete("/documento/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /documento/{id}: link self do documento")
    void obterPorIdComLinks() throws Exception {
        when(servico.obterPorId(2L)).thenReturn(documento(2L, TipoDocumento.CPF, "00000000001"));

        requisicao.perform(get("/documento/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links.self.href").value("http://localhost/documento/2"));
    }

    @Test
    @DisplayName("GET /usuario/{id}/documentos: link self em cada documento da lista")
    void listarComLinks() throws Exception {
        when(servico.listarPorUsuario(1L)).thenReturn(List.of(
                documento(1L, TipoDocumento.RG, "1500"), documento(2L, TipoDocumento.CPF, "00000000001")));

        requisicao.perform(get("/usuario/1/documentos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].links[0].rel").value("self"))
                .andExpect(jsonPath("$[0].links[0].href").value("http://localhost/documento/1"))
                .andExpect(jsonPath("$[1].links[0].rel").value("self"))
                .andExpect(jsonPath("$[1].links[0].href").value("http://localhost/documento/2"));
    }

    @Test
    @DisplayName("POST /usuario/{id}/documentos: resposta com link self do documento criado")
    void criarComLinks() throws Exception {
        when(servico.criar(anyLong(), any(Documento.class)))
                .thenReturn(documento(5L, TipoDocumento.CNH, "55443322"));

        String corpo = """
                { "tipo": "CNH", "numero": "55443322", "dataEmissao": "2015-06-10" }
                """;

        requisicao.perform(post("/usuario/1/documentos")
                .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$._links.self.href").value("http://localhost/documento/5"));
    }

    @Test
    @DisplayName("PUT /documento/{id}: resposta com link self do documento atualizado")
    void atualizarComLinks() throws Exception {
        when(servico.atualizar(anyLong(), any(Documento.class)))
                .thenReturn(documento(2L, TipoDocumento.CNH, "1500"));

        String corpo = """
                { "tipo": "CNH" }
                """;

        requisicao.perform(put("/documento/2")
                .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links.self.href").value("http://localhost/documento/2"));
    }

    @Test
    @DisplayName("GET /documento/{id}: resposta de erro nao tem links")
    void erroSemLinks() throws Exception {
        when(servico.obterPorId(999L))
                .thenThrow(new RecursoNaoEncontradoException("Documento", 999L));

        requisicao.perform(get("/documento/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$._links").doesNotExist())
                .andExpect(jsonPath("$.links").doesNotExist());
    }
}
