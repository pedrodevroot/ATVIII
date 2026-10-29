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

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.autobots.automanager.conversores.TelefoneConversor;
import com.autobots.automanager.entidades.Telefone;
import com.autobots.automanager.excecoes.RecursoNaoEncontradoException;
import com.autobots.automanager.modelo.AdicionadorLinkTelefone;
import com.autobots.automanager.servicos.TelefoneServico;

@WebMvcTest(TelefoneControle.class)
@Import({ TelefoneConversor.class, AdicionadorLinkTelefone.class })
class TelefoneControleTest {

    @Autowired
    private MockMvc requisicao;

    @MockBean
    private TelefoneServico servico;

    private Telefone telefone(Long id, String ddd, String numero) {
        Telefone telefone = new Telefone();
        telefone.setId(id);
        telefone.setDdd(ddd);
        telefone.setNumero(numero);
        return telefone;
    }

    @Test
    @DisplayName("GET /usuario/{id}/telefones: 200 com a lista")
    void listarOk() throws Exception {
        when(servico.listarPorUsuario(1L))
                .thenReturn(List.of(telefone(1L, "21", "981234576")));

        requisicao.perform(get("/usuario/1/telefones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].ddd").value("21"));
    }

    @Test
    @DisplayName("GET /usuario/{id}/telefones: 404 quando o usuario nao existe")
    void listarUsuarioInexistente() throws Exception {
        when(servico.listarPorUsuario(999L))
                .thenThrow(new RecursoNaoEncontradoException("Usuario", 999L));

        requisicao.perform(get("/usuario/999/telefones"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Usuario de id 999 nao foi encontrado"));
    }

    @Test
    @DisplayName("POST /usuario/{id}/telefones: 201 com header Location")
    void criarOk() throws Exception {
        when(servico.criar(anyLong(), any(Telefone.class)))
                .thenReturn(telefone(5L, "13", "981234567"));

        String corpo = """
                { "ddd": "13", "numero": "981234567" }
                """;

        requisicao.perform(post("/usuario/1/telefones")
                .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/telefone/5"))
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    @DisplayName("POST /usuario/{id}/telefones: 404 quando o usuario nao existe")
    void criarUsuarioInexistente() throws Exception {
        when(servico.criar(anyLong(), any(Telefone.class)))
                .thenThrow(new RecursoNaoEncontradoException("Usuario", 999L));

        String corpo = """
                { "ddd": "13", "numero": "981234567" }
                """;

        requisicao.perform(post("/usuario/999/telefones")
                .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /usuario/{id}/telefones: 400 quando ddd e numero sao invalidos")
    void criarInvalido() throws Exception {
        String corpo = """
                { "ddd": "1", "numero": "abc" }
                """;

        requisicao.perform(post("/usuario/1/telefones")
                .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros[*].campo", hasItem("ddd")))
                .andExpect(jsonPath("$.erros[*].campo", hasItem("numero")));

        verifyNoInteractions(servico);
    }

    @Test
    @DisplayName("POST /usuario/{id}/telefones: 400 quando o ddd nao e informado")
    void criarSemDdd() throws Exception {
        String corpo = """
                { "numero": "981234567" }
                """;

        requisicao.perform(post("/usuario/1/telefones")
                .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros[*].campo", hasItem("ddd")));

        verifyNoInteractions(servico);
    }

    @Test
    @DisplayName("GET /telefone/{id}: 200 com o telefone")
    void obterPorIdOk() throws Exception {
        when(servico.obterPorId(2L)).thenReturn(telefone(2L, "21", "981234576"));

        requisicao.perform(get("/telefone/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.numero").value("981234576"));
    }

    @Test
    @DisplayName("GET /telefone/{id}: 404 quando nao existe")
    void obterPorIdInexistente() throws Exception {
        when(servico.obterPorId(999L))
                .thenThrow(new RecursoNaoEncontradoException("Telefone", 999L));

        requisicao.perform(get("/telefone/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Telefone de id 999 nao foi encontrado"));
    }

    @Test
    @DisplayName("PUT /telefone/{id}: 200 aceitando atualizacao parcial")
    void atualizarParcial() throws Exception {
        when(servico.atualizar(anyLong(), any(Telefone.class)))
                .thenReturn(telefone(2L, "99", "981234576"));

        String corpo = """
                { "ddd": "99" }
                """;

        requisicao.perform(put("/telefone/2")
                .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ddd").value("99"))
                .andExpect(jsonPath("$.numero").value("981234576"));
    }

    @Test
    @DisplayName("PUT /telefone/{id}: 400 quando o numero e invalido")
    void atualizarInvalido() throws Exception {
        String corpo = """
                { "numero": "123" }
                """;

        requisicao.perform(put("/telefone/2")
                .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros[*].campo", hasItem("numero")));

        verifyNoInteractions(servico);
    }

    @Test
    @DisplayName("PUT /telefone/{id}: 404 quando nao existe")
    void atualizarInexistente() throws Exception {
        when(servico.atualizar(anyLong(), any(Telefone.class)))
                .thenThrow(new RecursoNaoEncontradoException("Telefone", 999L));

        String corpo = """
                { "ddd": "99" }
                """;

        requisicao.perform(put("/telefone/999")
                .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /telefone/{id}: 204 sem corpo")
    void excluirOk() throws Exception {
        requisicao.perform(delete("/telefone/2"))
                .andExpect(status().isNoContent());

        verify(servico).excluir(2L);
    }

    @Test
    @DisplayName("DELETE /telefone/{id}: 404 quando nao existe")
    void excluirInexistente() throws Exception {
        doThrow(new RecursoNaoEncontradoException("Telefone", 999L)).when(servico).excluir(999L);

        requisicao.perform(delete("/telefone/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /telefone/{id}: link self do telefone")
    void obterPorIdComLinks() throws Exception {
        when(servico.obterPorId(2L)).thenReturn(telefone(2L, "21", "981234576"));

        requisicao.perform(get("/telefone/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links.self.href").value("http://localhost/telefone/2"));
    }

    @Test
    @DisplayName("GET /usuario/{id}/telefones: link self em cada telefone da lista")
    void listarComLinks() throws Exception {
        when(servico.listarPorUsuario(1L))
                .thenReturn(List.of(telefone(1L, "21", "981234576"), telefone(2L, "13", "981234567")));

        requisicao.perform(get("/usuario/1/telefones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].links[0].rel").value("self"))
                .andExpect(jsonPath("$[0].links[0].href").value("http://localhost/telefone/1"))
                .andExpect(jsonPath("$[1].links[0].rel").value("self"))
                .andExpect(jsonPath("$[1].links[0].href").value("http://localhost/telefone/2"));
    }

    @Test
    @DisplayName("POST /usuario/{id}/telefones: resposta com link self do telefone criado")
    void criarComLinks() throws Exception {
        when(servico.criar(anyLong(), any(Telefone.class)))
                .thenReturn(telefone(5L, "13", "981234567"));

        String corpo = """
                { "ddd": "13", "numero": "981234567" }
                """;

        requisicao.perform(post("/usuario/1/telefones")
                .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$._links.self.href").value("http://localhost/telefone/5"));
    }

    @Test
    @DisplayName("PUT /telefone/{id}: resposta com link self do telefone atualizado")
    void atualizarComLinks() throws Exception {
        when(servico.atualizar(anyLong(), any(Telefone.class)))
                .thenReturn(telefone(2L, "99", "981234576"));

        String corpo = """
                { "ddd": "99" }
                """;

        requisicao.perform(put("/telefone/2")
                .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links.self.href").value("http://localhost/telefone/2"));
    }

    @Test
    @DisplayName("GET /telefone/{id}: resposta de erro nao tem links")
    void erroSemLinks() throws Exception {
        when(servico.obterPorId(999L))
                .thenThrow(new RecursoNaoEncontradoException("Telefone", 999L));

        requisicao.perform(get("/telefone/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$._links").doesNotExist())
                .andExpect(jsonPath("$.links").doesNotExist());
    }
}
