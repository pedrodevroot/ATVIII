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

import com.autobots.automanager.conversores.EnderecoConversor;
import com.autobots.automanager.entidades.Endereco;
import com.autobots.automanager.excecoes.RecursoNaoEncontradoException;
import com.autobots.automanager.modelo.AdicionadorLinkEndereco;
import com.autobots.automanager.servicos.EnderecoServico;

@WebMvcTest(EnderecoControle.class)
@Import({ EnderecoConversor.class, AdicionadorLinkEndereco.class })
class EnderecoControleTest {

    @Autowired
    private MockMvc requisicao;

    @MockBean
    private EnderecoServico servico;

    private Endereco endereco(Long id, String cidade) {
        Endereco endereco = new Endereco();
        endereco.setId(id);
        endereco.setEstado("Sao Paulo");
        endereco.setCidade(cidade);
        endereco.setBairro("Gonzaga");
        endereco.setRua("Avenida Ana Costa");
        endereco.setNumero("250");
        endereco.setCodigoPostal("11060002");
        return endereco;
    }

    @Test
    @DisplayName("GET /cliente/{id}/enderecos: 200 com a lista")
    void listarOk() throws Exception {
        when(servico.listarPorCliente(1L))
                .thenReturn(List.of(endereco(3L, "Santos"), endereco(4L, "Rio de Janeiro")));

        requisicao.perform(get("/cliente/1/enderecos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].cidade").value("Santos"))
                .andExpect(jsonPath("$[1].cidade").value("Rio de Janeiro"));
    }

    @Test
    @DisplayName("GET /cliente/{id}/enderecos: 404 quando o cliente nao existe")
    void listarClienteInexistente() throws Exception {
        when(servico.listarPorCliente(999L))
                .thenThrow(new RecursoNaoEncontradoException("Cliente", 999L));

        requisicao.perform(get("/cliente/999/enderecos"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Cliente de id 999 nao foi encontrado"));
    }

    @Test
    @DisplayName("POST /cliente/{id}/enderecos: 201 com header Location")
    void criarOk() throws Exception {
        when(servico.criar(anyLong(), any(Endereco.class))).thenReturn(endereco(5L, "Santos"));

        String corpo = """
                {
                  "estado": "Sao Paulo",
                  "cidade": "Santos",
                  "bairro": "Gonzaga",
                  "rua": "Avenida Ana Costa",
                  "numero": "250",
                  "codigoPostal": "11060002"
                }
                """;

        requisicao.perform(post("/cliente/1/enderecos")
                .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/endereco/5"))
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    @DisplayName("POST /cliente/{id}/enderecos: 404 quando o cliente nao existe")
    void criarClienteInexistente() throws Exception {
        when(servico.criar(anyLong(), any(Endereco.class)))
                .thenThrow(new RecursoNaoEncontradoException("Cliente", 999L));

        String corpo = """
                { "cidade": "Santos", "rua": "Rua XV", "numero": "1" }
                """;

        requisicao.perform(post("/cliente/999/enderecos")
                .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /cliente/{id}/enderecos: 400 quando cidade, rua ou numero faltam")
    void criarIncompleto() throws Exception {
        String corpo = """
                { "bairro": "Centro" }
                """;

        requisicao.perform(post("/cliente/1/enderecos")
                .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros[*].campo", hasItem("cidade")))
                .andExpect(jsonPath("$.erros[*].campo", hasItem("rua")))
                .andExpect(jsonPath("$.erros[*].campo", hasItem("numero")));

        verifyNoInteractions(servico);
    }

    @Test
    @DisplayName("POST /cliente/{id}/enderecos: 400 quando o codigo postal nao tem 8 digitos")
    void criarCodigoPostalInvalido() throws Exception {
        String corpo = """
                { "cidade": "Santos", "rua": "Rua XV", "numero": "1", "codigoPostal": "123" }
                """;

        requisicao.perform(post("/cliente/1/enderecos")
                .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros[*].campo", hasItem("codigoPostal")));

        verifyNoInteractions(servico);
    }

    @Test
    @DisplayName("GET /endereco/{id}: 200 com o endereco")
    void obterPorIdOk() throws Exception {
        when(servico.obterPorId(3L)).thenReturn(endereco(3L, "Santos"));

        requisicao.perform(get("/endereco/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.cidade").value("Santos"))
                .andExpect(jsonPath("$.codigoPostal").value("11060002"));
    }

    @Test
    @DisplayName("GET /endereco/{id}: 404 quando nao existe")
    void obterPorIdInexistente() throws Exception {
        when(servico.obterPorId(999L))
                .thenThrow(new RecursoNaoEncontradoException("Endereco", 999L));

        requisicao.perform(get("/endereco/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Endereco de id 999 nao foi encontrado"));
    }

    @Test
    @DisplayName("PUT /endereco/{id}: 200 aceitando atualizacao parcial")
    void atualizarParcial() throws Exception {
        when(servico.atualizar(anyLong(), any(Endereco.class)))
                .thenReturn(endereco(3L, "Rio de Janeiro"));

        String corpo = """
                { "cidade": "Rio de Janeiro" }
                """;

        requisicao.perform(put("/endereco/3")
                .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cidade").value("Rio de Janeiro"))
                .andExpect(jsonPath("$.rua").value("Avenida Ana Costa"));
    }

    @Test
    @DisplayName("PUT /endereco/{id}: 400 quando o codigo postal e invalido")
    void atualizarCodigoPostalInvalido() throws Exception {
        String corpo = """
                { "codigoPostal": "123" }
                """;

        requisicao.perform(put("/endereco/3")
                .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros[*].campo", hasItem("codigoPostal")));

        verifyNoInteractions(servico);
    }

    @Test
    @DisplayName("PUT /endereco/{id}: 404 quando nao existe")
    void atualizarInexistente() throws Exception {
        when(servico.atualizar(anyLong(), any(Endereco.class)))
                .thenThrow(new RecursoNaoEncontradoException("Endereco", 999L));

        String corpo = """
                { "cidade": "Santos" }
                """;

        requisicao.perform(put("/endereco/999")
                .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /endereco/{id}: 204 sem corpo")
    void excluirOk() throws Exception {
        requisicao.perform(delete("/endereco/3"))
                .andExpect(status().isNoContent());

        verify(servico).excluir(3L);
    }

    @Test
    @DisplayName("DELETE /endereco/{id}: 404 quando nao existe")
    void excluirInexistente() throws Exception {
        doThrow(new RecursoNaoEncontradoException("Endereco", 999L)).when(servico).excluir(999L);

        requisicao.perform(delete("/endereco/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /endereco/{id}: link self do endereco")
    void obterPorIdComLinks() throws Exception {
        when(servico.obterPorId(3L)).thenReturn(endereco(3L, "Santos"));

        requisicao.perform(get("/endereco/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links.self.href").value("http://localhost/endereco/3"));
    }

    @Test
    @DisplayName("GET /cliente/{id}/enderecos: link self em cada endereco da lista")
    void listarComLinks() throws Exception {
        when(servico.listarPorCliente(1L))
                .thenReturn(List.of(endereco(3L, "Santos"), endereco(4L, "Rio de Janeiro")));

        requisicao.perform(get("/cliente/1/enderecos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].links[0].rel").value("self"))
                .andExpect(jsonPath("$[0].links[0].href").value("http://localhost/endereco/3"))
                .andExpect(jsonPath("$[1].links[0].rel").value("self"))
                .andExpect(jsonPath("$[1].links[0].href").value("http://localhost/endereco/4"));
    }

    @Test
    @DisplayName("POST /cliente/{id}/enderecos: resposta com link self do endereco criado")
    void criarComLinks() throws Exception {
        when(servico.criar(anyLong(), any(Endereco.class))).thenReturn(endereco(5L, "Santos"));

        String corpo = """
                { "cidade": "Santos", "rua": "Avenida Ana Costa", "numero": "250" }
                """;

        requisicao.perform(post("/cliente/1/enderecos")
                .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$._links.self.href").value("http://localhost/endereco/5"));
    }

    @Test
    @DisplayName("PUT /endereco/{id}: resposta com link self do endereco atualizado")
    void atualizarComLinks() throws Exception {
        when(servico.atualizar(anyLong(), any(Endereco.class)))
                .thenReturn(endereco(3L, "Rio de Janeiro"));

        String corpo = """
                { "cidade": "Rio de Janeiro" }
                """;

        requisicao.perform(put("/endereco/3")
                .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links.self.href").value("http://localhost/endereco/3"));
    }

    @Test
    @DisplayName("GET /endereco/{id}: resposta de erro nao tem links")
    void erroSemLinks() throws Exception {
        when(servico.obterPorId(999L))
                .thenThrow(new RecursoNaoEncontradoException("Endereco", 999L));

        requisicao.perform(get("/endereco/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$._links").doesNotExist())
                .andExpect(jsonPath("$.links").doesNotExist());
    }
}
