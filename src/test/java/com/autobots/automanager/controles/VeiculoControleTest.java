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

import com.autobots.automanager.conversores.VeiculoConversor;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.entidades.Veiculo;
import com.autobots.automanager.enumeracoes.TipoVeiculo;
import com.autobots.automanager.excecoes.ConflitoException;
import com.autobots.automanager.excecoes.RecursoNaoEncontradoException;
import com.autobots.automanager.modelo.AdicionadorLinkVeiculo;
import com.autobots.automanager.servicos.VeiculoServico;

@WebMvcTest(VeiculoControle.class)
@Import({ VeiculoConversor.class, AdicionadorLinkVeiculo.class })
class VeiculoControleTest {

    @Autowired
    private MockMvc requisicao;

    @MockBean
    private VeiculoServico servico;

    private Veiculo veiculo(Long id) {
        Usuario dono = new Usuario();
        dono.setId(1L);
        Veiculo veiculo = new Veiculo();
        veiculo.setId(id);
        veiculo.setTipo(TipoVeiculo.SUV);
        veiculo.setModelo("Corolla Cross");
        veiculo.setPlaca("ABC1D23");
        veiculo.setProprietario(dono);
        return veiculo;
    }

    @Test
    @DisplayName("GET /usuario/{id}/veiculos: 200 com a lista e link self em cada veiculo")
    void listarOk() throws Exception {
        when(servico.listarPorUsuario(1L)).thenReturn(List.of(veiculo(5L)));

        requisicao.perform(get("/usuario/1/veiculos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].placa").value("ABC1D23"))
                .andExpect(jsonPath("$[0].links[0].href").value("http://localhost/veiculo/5"));
    }

    @Test
    @DisplayName("GET /usuario/{id}/veiculos: 404 quando o usuario nao existe")
    void listarUsuarioInexistente() throws Exception {
        when(servico.listarPorUsuario(999L)).thenThrow(new RecursoNaoEncontradoException("Usuario", 999L));

        requisicao.perform(get("/usuario/999/veiculos"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Usuario de id 999 nao foi encontrado"));
    }

    @Test
    @DisplayName("POST /usuario/{id}/veiculos: 201 com header Location e links")
    void criarOk() throws Exception {
        when(servico.criar(anyLong(), any(Veiculo.class))).thenReturn(veiculo(5L));

        String corpo = """
                { "tipo": "SUV", "modelo": "Corolla Cross", "placa": "ABC1D23" }
                """;

        requisicao.perform(post("/usuario/1/veiculos")
                .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/veiculo/5"))
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.proprietarioId").value(1))
                .andExpect(jsonPath("$._links.self.href").value("http://localhost/veiculo/5"))
                .andExpect(jsonPath("$._links.proprietario.href").value("http://localhost/usuario/1"))
                .andExpect(jsonPath("$._links.vendas.href").value("http://localhost/veiculo/5/vendas"));
    }

    @Test
    @DisplayName("POST /usuario/{id}/veiculos: aceita os dois formatos de placa")
    void criarFormatosDePlaca() throws Exception {
        when(servico.criar(anyLong(), any(Veiculo.class))).thenReturn(veiculo(5L));

        for (String placa : List.of("ABC1234", "ABC1D23")) {
            requisicao.perform(post("/usuario/1/veiculos")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{ \"tipo\": \"SUV\", \"modelo\": \"Yaris\", \"placa\": \"" + placa + "\" }"))
                    .andExpect(status().isCreated());
        }
    }

    @Test
    @DisplayName("POST /usuario/{id}/veiculos: 400 quando faltam os campos obrigatorios")
    void criarSemCampos() throws Exception {
        requisicao.perform(post("/usuario/1/veiculos")
                .contentType(MediaType.APPLICATION_JSON).content("{ }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros[*].campo", hasItem("tipo")))
                .andExpect(jsonPath("$.erros[*].campo", hasItem("modelo")))
                .andExpect(jsonPath("$.erros[*].campo", hasItem("placa")));

        verifyNoInteractions(servico);
    }

    @Test
    @DisplayName("POST /usuario/{id}/veiculos: 400 quando a placa esta fora do formato")
    void criarPlacaInvalida() throws Exception {
        for (String placa : List.of("abc1d23", "ABC-1234", "ABCD123", "AB1234")) {
            requisicao.perform(post("/usuario/1/veiculos")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{ \"tipo\": \"SUV\", \"modelo\": \"Yaris\", \"placa\": \"" + placa + "\" }"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.erros[*].campo", hasItem("placa")));
        }

        verifyNoInteractions(servico);
    }

    @Test
    @DisplayName("POST /usuario/{id}/veiculos: 400 quando o tipo nao existe")
    void criarTipoInvalido() throws Exception {
        String corpo = """
                { "tipo": "AVIAO", "modelo": "Yaris", "placa": "ABC1D23" }
                """;

        requisicao.perform(post("/usuario/1/veiculos")
                .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(servico);
    }

    @Test
    @DisplayName("GET /veiculo/{id}: 200 com o veiculo")
    void obterOk() throws Exception {
        when(servico.obterPorId(5L)).thenReturn(veiculo(5L));

        requisicao.perform(get("/veiculo/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.modelo").value("Corolla Cross"))
                .andExpect(jsonPath("$._links.self.href").value("http://localhost/veiculo/5"));
    }

    @Test
    @DisplayName("GET /veiculo/{id}: 404 quando nao existe, sem links")
    void obterInexistente() throws Exception {
        when(servico.obterPorId(999L)).thenThrow(new RecursoNaoEncontradoException("Veiculo", 999L));

        requisicao.perform(get("/veiculo/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$._links").doesNotExist());
    }

    @Test
    @DisplayName("PUT /veiculo/{id}: 200 com o veiculo atualizado")
    void atualizarOk() throws Exception {
        when(servico.atualizar(anyLong(), any(Veiculo.class))).thenReturn(veiculo(5L));

        requisicao.perform(put("/veiculo/5")
                .contentType(MediaType.APPLICATION_JSON).content("{ \"modelo\": \"Corolla Cross\" }"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.modelo").value("Corolla Cross"));
    }

    @Test
    @DisplayName("PUT /veiculo/{id}: 400 quando a placa esta fora do formato")
    void atualizarPlacaInvalida() throws Exception {
        requisicao.perform(put("/veiculo/5")
                .contentType(MediaType.APPLICATION_JSON).content("{ \"placa\": \"123\" }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros[*].campo", hasItem("placa")));

        verifyNoInteractions(servico);
    }

    @Test
    @DisplayName("DELETE /veiculo/{id}: 204 sem corpo")
    void excluirOk() throws Exception {
        requisicao.perform(delete("/veiculo/5"))
                .andExpect(status().isNoContent());

        verify(servico).excluir(5L);
    }

    @Test
    @DisplayName("DELETE /veiculo/{id}: 409 quando o veiculo possui vendas")
    void excluirComVendas() throws Exception {
        doThrow(new ConflitoException("O veiculo de id 5 possui vendas registradas e nao pode ser excluido"))
                .when(servico).excluir(5L);

        requisicao.perform(delete("/veiculo/5"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem")
                        .value("O veiculo de id 5 possui vendas registradas e nao pode ser excluido"));
    }

    @Test
    @DisplayName("POST /usuario/{id}/veiculos: 400 com tipo informado como numero")
    void criarTipoNumerico() throws Exception {
        requisicao.perform(post("/usuario/1/veiculos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ \"tipo\": 0, \"modelo\": \"Yaris\", \"placa\": \"ABC1D23\" }"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(servico);
    }
}
