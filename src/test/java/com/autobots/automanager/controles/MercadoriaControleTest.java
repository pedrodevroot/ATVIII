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

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.autobots.automanager.conversores.MercadoriaConversor;
import com.autobots.automanager.dtos.MercadoriaAtualizacaoDto;
import com.autobots.automanager.entidades.Mercadoria;
import com.autobots.automanager.excecoes.ConflitoException;
import com.autobots.automanager.excecoes.RecursoNaoEncontradoException;
import com.autobots.automanager.modelo.AdicionadorLinkMercadoria;
import com.autobots.automanager.servicos.MercadoriaServico;

@WebMvcTest(MercadoriaControle.class)
@Import({ MercadoriaConversor.class, AdicionadorLinkMercadoria.class })
class MercadoriaControleTest {

    private static final String CORPO = """
            { "nome": "Pneu", "fabricacao": "2025-01-15T00:00:00.000+00:00", "quantidade": 10, "valor": 300.00 }
            """;

    @Autowired
    private MockMvc requisicao;

    @MockBean
    private MercadoriaServico servico;

    private Mercadoria mercadoria(Long id) {
        Mercadoria mercadoria = new Mercadoria();
        mercadoria.setId(id);
        mercadoria.setNome("Pneu");
        mercadoria.setFabricacao(new Date(1000L));
        mercadoria.setCadastro(new Date(2000L));
        mercadoria.setQuantidade(10);
        mercadoria.setValor(new BigDecimal("300.00"));
        return mercadoria;
    }

    @Test
    @DisplayName("GET /empresa/{id}/mercadorias: 200 com a lista e link self em cada item")
    void listarOk() throws Exception {
        when(servico.listarPorEmpresa(1L)).thenReturn(List.of(mercadoria(10L)));

        requisicao.perform(get("/empresa/1/mercadorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nome").value("Pneu"))
                .andExpect(jsonPath("$[0].links[0].href").value("http://localhost/mercadoria/10"));
    }

    @Test
    @DisplayName("POST /empresa/{id}/mercadorias: 201 com header Location e link self")
    void criarOk() throws Exception {
        when(servico.criar(anyLong(), any(Mercadoria.class))).thenReturn(mercadoria(10L));

        requisicao.perform(post("/empresa/1/mercadorias")
                .contentType(MediaType.APPLICATION_JSON).content(CORPO))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/mercadoria/10"))
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.quantidade").value(10))
                .andExpect(jsonPath("$.valor").value(300.0))
                .andExpect(jsonPath("$._links.self.href").value("http://localhost/mercadoria/10"));
    }

    @Test
    @DisplayName("POST /empresa/{id}/mercadorias: 400 quando faltam os campos obrigatorios")
    void criarSemCampos() throws Exception {
        requisicao.perform(post("/empresa/1/mercadorias")
                .contentType(MediaType.APPLICATION_JSON).content("{ }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros[*].campo", hasItem("nome")))
                .andExpect(jsonPath("$.erros[*].campo", hasItem("fabricacao")))
                .andExpect(jsonPath("$.erros[*].campo", hasItem("quantidade")))
                .andExpect(jsonPath("$.erros[*].campo", hasItem("valor")));

        verifyNoInteractions(servico);
    }

    @Test
    @DisplayName("POST /empresa/{id}/mercadorias: 400 para fabricacao futura, quantidade e valor invalidos")
    void criarValoresInvalidos() throws Exception {
        String corpo = """
                { "nome": "Pneu", "fabricacao": "2099-01-15T00:00:00.000+00:00", "quantidade": -1, "valor": 1.234 }
                """;

        requisicao.perform(post("/empresa/1/mercadorias")
                .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros[*].campo", hasItem("fabricacao")))
                .andExpect(jsonPath("$.erros[*].campo", hasItem("quantidade")))
                .andExpect(jsonPath("$.erros[*].campo", hasItem("valor")));

        verifyNoInteractions(servico);
    }

    @Test
    @DisplayName("GET /mercadoria/{id}: 200 com a mercadoria")
    void obterOk() throws Exception {
        when(servico.obterPorId(10L)).thenReturn(mercadoria(10L));

        requisicao.perform(get("/mercadoria/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Pneu"))
                .andExpect(jsonPath("$._links.self.href").value("http://localhost/mercadoria/10"));
    }

    @Test
    @DisplayName("GET /mercadoria/{id}: 404 quando nao existe, sem links")
    void obterInexistente() throws Exception {
        when(servico.obterPorId(999L)).thenThrow(new RecursoNaoEncontradoException("Mercadoria", 999L));

        requisicao.perform(get("/mercadoria/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$._links").doesNotExist());
    }

    @Test
    @DisplayName("PUT /mercadoria/{id}: 200 com a mercadoria atualizada")
    void atualizarOk() throws Exception {
        when(servico.atualizar(anyLong(), any(MercadoriaAtualizacaoDto.class))).thenReturn(mercadoria(10L));

        requisicao.perform(put("/mercadoria/10")
                .contentType(MediaType.APPLICATION_JSON).content("{ \"quantidade\": 10 }"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidade").value(10));
    }

    @Test
    @DisplayName("PUT /mercadoria/{id}: 400 quando a validade e anterior a fabricacao")
    void atualizarDatasInvalidas() throws Exception {
        when(servico.atualizar(anyLong(), any(MercadoriaAtualizacaoDto.class)))
                .thenThrow(new IllegalArgumentException("A data de validade nao pode ser anterior a data de fabricacao"));

        requisicao.perform(put("/mercadoria/10")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ \"validade\": \"2000-01-01T00:00:00.000+00:00\" }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem")
                        .value("A data de validade nao pode ser anterior a data de fabricacao"));
    }

    @Test
    @DisplayName("DELETE /mercadoria/{id}: 204 sem corpo")
    void excluirOk() throws Exception {
        requisicao.perform(delete("/mercadoria/10"))
                .andExpect(status().isNoContent());

        verify(servico).excluir(10L);
    }

    @Test
    @DisplayName("DELETE /mercadoria/{id}: 409 quando a mercadoria possui vendas")
    void excluirComVendas() throws Exception {
        doThrow(new ConflitoException("A mercadoria de id 10 possui vendas registradas e nao pode ser excluida"))
                .when(servico).excluir(10L);

        requisicao.perform(delete("/mercadoria/10"))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("GET /usuario/{id}/mercadorias: 200 com as mercadorias do fornecedor")
    void listarDoFornecedor() throws Exception {
        when(servico.listarPorFornecedor(2L)).thenReturn(List.of(mercadoria(10L)));

        requisicao.perform(get("/usuario/2/mercadorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].links[0].href").value("http://localhost/mercadoria/10"));
    }

    @Test
    @DisplayName("PUT /usuario/{id}/mercadorias/{id}: 204 ao vincular o fornecedor")
    void vincularOk() throws Exception {
        requisicao.perform(put("/usuario/2/mercadorias/10"))
                .andExpect(status().isNoContent());

        verify(servico).vincularFornecedor(2L, 10L);
    }

    @Test
    @DisplayName("PUT /usuario/{id}/mercadorias/{id}: 400 quando o usuario nao e fornecedor")
    void vincularSemPerfil() throws Exception {
        doThrow(new IllegalArgumentException("O usuario de id 3 nao possui o perfil FORNECEDOR"))
                .when(servico).vincularFornecedor(3L, 10L);

        requisicao.perform(put("/usuario/3/mercadorias/10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("O usuario de id 3 nao possui o perfil FORNECEDOR"));
    }

    @Test
    @DisplayName("DELETE /usuario/{id}/mercadorias/{id}: 204 ao desvincular o fornecedor")
    void desvincularOk() throws Exception {
        requisicao.perform(delete("/usuario/2/mercadorias/10"))
                .andExpect(status().isNoContent());

        verify(servico).desvincularFornecedor(2L, 10L);
    }
}
