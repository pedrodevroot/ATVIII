package com.autobots.automanager.controles;

import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.autobots.automanager.conversores.VendaConversor;
import com.autobots.automanager.dtos.VendaAtualizacaoDto;
import com.autobots.automanager.dtos.VendaCadastroDto;
import com.autobots.automanager.entidades.Mercadoria;
import com.autobots.automanager.entidades.Servico;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.entidades.Veiculo;
import com.autobots.automanager.entidades.Venda;
import com.autobots.automanager.excecoes.RecursoNaoEncontradoException;
import com.autobots.automanager.modelo.AdicionadorLinkVenda;
import com.autobots.automanager.servicos.VendaServico;

@WebMvcTest(VendaControle.class)
@Import({ VendaConversor.class, AdicionadorLinkVenda.class })
class VendaControleTest {

    private static final String CORPO = """
            { "identificacao": "V-1", "clienteId": 3, "funcionarioId": 1, "veiculoId": 5,
              "mercadoriaIds": [ 10 ], "servicoIds": [ 20 ] }
            """;

    @Autowired
    private MockMvc requisicao;

    @MockBean
    private VendaServico servico;

    private Usuario usuario(long id) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        return usuario;
    }

    private Venda venda(Long id) {
        Venda venda = new Venda();
        venda.setId(id);
        venda.setCadastro(new Date(1000L));
        venda.setIdentificacao("V-1");
        venda.setCliente(usuario(3L));
        venda.setFuncionario(usuario(1L));
        Mercadoria mercadoria = new Mercadoria();
        mercadoria.setId(10L);
        venda.getMercadorias().add(mercadoria);
        Servico item = new Servico();
        item.setId(20L);
        venda.getServicos().add(item);
        return venda;
    }

    private Venda vendaComVeiculo(Long id) {
        Veiculo veiculo = new Veiculo();
        veiculo.setId(5L);
        Venda venda = venda(id);
        venda.setVeiculo(veiculo);
        return venda;
    }

    @Test
    @DisplayName("GET /empresa/{id}/vendas: 200 com a lista e link self em cada venda")
    void listarDaEmpresa() throws Exception {
        when(servico.listarPorEmpresa(1L)).thenReturn(List.of(venda(100L)));

        requisicao.perform(get("/empresa/1/vendas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].identificacao").value("V-1"))
                .andExpect(jsonPath("$[0].links[0].href").value("http://localhost/venda/100"));
    }

    @Test
    @DisplayName("GET /usuario/{id}/vendas: 200 com as vendas do usuario")
    void listarDoUsuario() throws Exception {
        when(servico.listarPorUsuario(3L)).thenReturn(List.of(venda(100L)));

        requisicao.perform(get("/usuario/3/vendas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].clienteId").value(3));
    }

    @Test
    @DisplayName("GET /veiculo/{id}/vendas: 200 com as vendas do veiculo")
    void listarDoVeiculo() throws Exception {
        when(servico.listarPorVeiculo(5L)).thenReturn(List.of(vendaComVeiculo(100L)));

        requisicao.perform(get("/veiculo/5/vendas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].veiculoId").value(5));
    }

    @Test
    @DisplayName("POST /empresa/{id}/vendas: 201 com Location e os links dos envolvidos e dos itens")
    void criarOk() throws Exception {
        when(servico.criar(anyLong(), any(VendaCadastroDto.class))).thenReturn(vendaComVeiculo(100L));

        requisicao.perform(post("/empresa/1/vendas")
                .contentType(MediaType.APPLICATION_JSON).content(CORPO))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/venda/100"))
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$._links.self.href").value("http://localhost/venda/100"))
                .andExpect(jsonPath("$._links.cliente.href").value("http://localhost/usuario/3"))
                .andExpect(jsonPath("$._links.funcionario.href").value("http://localhost/usuario/1"))
                .andExpect(jsonPath("$._links.veiculo.href").value("http://localhost/veiculo/5"))
                .andExpect(jsonPath("$.mercadorias[0]._links.self.href").value("http://localhost/mercadoria/10"))
                .andExpect(jsonPath("$.servicos[0]._links.self.href").value("http://localhost/servico/20"));
    }

    @Test
    @DisplayName("POST /empresa/{id}/vendas: 400 quando faltam os campos obrigatorios")
    void criarSemCampos() throws Exception {
        requisicao.perform(post("/empresa/1/vendas")
                .contentType(MediaType.APPLICATION_JSON).content("{ }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros[*].campo", hasItem("identificacao")))
                .andExpect(jsonPath("$.erros[*].campo", hasItem("clienteId")))
                .andExpect(jsonPath("$.erros[*].campo", hasItem("funcionarioId")));

        verifyNoInteractions(servico);
    }

    @Test
    @DisplayName("POST /empresa/{id}/vendas: 400 quando um item e repetido")
    void criarItemRepetido() throws Exception {
        String corpo = """
                { "identificacao": "V-1", "clienteId": 3, "funcionarioId": 1,
                  "mercadoriaIds": [ 10, 10 ], "servicoIds": [ 20, 20 ] }
                """;

        requisicao.perform(post("/empresa/1/vendas")
                .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros[*].campo", hasItem("mercadoriaIds")))
                .andExpect(jsonPath("$.erros[*].campo", hasItem("servicoIds")));

        verifyNoInteractions(servico);
    }

    @Test
    @DisplayName("POST /empresa/{id}/vendas: 400 quando o servico recusa a regra de negocio")
    void criarRegraRecusada() throws Exception {
        when(servico.criar(anyLong(), any(VendaCadastroDto.class)))
                .thenThrow(new IllegalArgumentException("O veiculo de id 5 nao pertence ao cliente de id 3"));

        requisicao.perform(post("/empresa/1/vendas")
                .contentType(MediaType.APPLICATION_JSON).content(CORPO))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("O veiculo de id 5 nao pertence ao cliente de id 3"));
    }

    @Test
    @DisplayName("GET /venda/{id}: 200 sem link de veiculo quando a venda nao tem veiculo")
    void obterSemVeiculo() throws Exception {
        when(servico.obterPorId(100L)).thenReturn(venda(100L));

        requisicao.perform(get("/venda/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.identificacao").value("V-1"))
                .andExpect(jsonPath("$.veiculoId").doesNotExist())
                .andExpect(jsonPath("$._links.veiculo").doesNotExist());
    }

    @Test
    @DisplayName("GET /venda/{id}: 404 quando nao existe, sem links")
    void obterInexistente() throws Exception {
        when(servico.obterPorId(999L)).thenThrow(new RecursoNaoEncontradoException("Venda", 999L));

        requisicao.perform(get("/venda/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$._links").doesNotExist());
    }

    @Test
    @DisplayName("PUT /venda/{id}: 200 com a venda atualizada")
    void atualizarOk() throws Exception {
        when(servico.atualizar(anyLong(), any(VendaAtualizacaoDto.class))).thenReturn(venda(100L));

        requisicao.perform(put("/venda/100")
                .contentType(MediaType.APPLICATION_JSON).content("{ \"identificacao\": \"V-1\" }"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.identificacao").value("V-1"))
                .andExpect(jsonPath("$._links.self.href").value("http://localhost/venda/100"));
    }

    @Test
    @DisplayName("PUT /venda/{id}: 400 quando um item e repetido")
    void atualizarItemRepetido() throws Exception {
        requisicao.perform(put("/venda/100")
                .contentType(MediaType.APPLICATION_JSON).content("{ \"servicoIds\": [ 20, 20 ] }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros[*].campo", hasItem("servicoIds")));

        verifyNoInteractions(servico);
    }

    @Test
    @DisplayName("DELETE /venda/{id}: 204 sem corpo")
    void excluirOk() throws Exception {
        requisicao.perform(delete("/venda/100"))
                .andExpect(status().isNoContent());

        verify(servico).excluir(100L);
    }

    @Test
    @DisplayName("DELETE /venda/{id}: 404 quando nao existe")
    void excluirInexistente() throws Exception {
        doThrow(new RecursoNaoEncontradoException("Venda", 999L)).when(servico).excluir(999L);

        requisicao.perform(delete("/venda/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /empresa/{id}/vendas: lista enviada como null vale como vazia")
    void criarComListasNulas() throws Exception {
        when(servico.criar(anyLong(), any(VendaCadastroDto.class))).thenReturn(venda(100L));

        String corpo = """
                { "identificacao": "V-1", "clienteId": 3, "funcionarioId": 1,
                  "mercadoriaIds": null, "servicoIds": null }
                """;

        requisicao.perform(post("/empresa/1/vendas")
                .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated());

        ArgumentCaptor<VendaCadastroDto> captor = ArgumentCaptor.forClass(VendaCadastroDto.class);
        verify(servico).criar(anyLong(), captor.capture());
        assertTrue(captor.getValue().getMercadoriaIds().isEmpty());
        assertTrue(captor.getValue().getServicoIds().isEmpty());
    }

    @Test
    @DisplayName("POST /empresa/{id}/vendas: 400 com id nulo em qualquer lista")
    void criarComIdNulo() throws Exception {
        String corpo = """
                { "identificacao": "V-1", "clienteId": 3, "funcionarioId": 1,
                  "mercadoriaIds": [ null ], "servicoIds": [ null ] }
                """;

        requisicao.perform(post("/empresa/1/vendas")
                .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros[*].campo", hasItem("mercadoriaIds[0]")))
                .andExpect(jsonPath("$.erros[*].campo", hasItem("servicoIds[0]")));

        verifyNoInteractions(servico);
    }

    @Test
    @DisplayName("PUT /venda/{id}: lista enviada como null vale como nao enviada, e nao remove os itens")
    void atualizarComListasNulas() throws Exception {
        when(servico.atualizar(anyLong(), any(VendaAtualizacaoDto.class))).thenReturn(venda(100L));

        requisicao.perform(put("/venda/100")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ \"mercadoriaIds\": null, \"servicoIds\": null }"))
                .andExpect(status().isOk());

        ArgumentCaptor<VendaAtualizacaoDto> captor = ArgumentCaptor.forClass(VendaAtualizacaoDto.class);
        verify(servico).atualizar(anyLong(), captor.capture());
        assertNull(captor.getValue().getMercadoriaIds());
        assertNull(captor.getValue().getServicoIds());
    }
}
