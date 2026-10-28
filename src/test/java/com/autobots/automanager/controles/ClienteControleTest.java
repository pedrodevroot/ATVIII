package com.autobots.automanager.controles;

import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
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

import com.autobots.automanager.conversores.ClienteConversor;
import com.autobots.automanager.entidades.Cliente;
import com.autobots.automanager.entidades.Telefone;
import com.autobots.automanager.excecoes.RecursoNaoEncontradoException;
import com.autobots.automanager.modelo.AdicionadorLinkCliente;
import com.autobots.automanager.servicos.ClienteServico;

@WebMvcTest(ClienteControle.class)
@Import({ ClienteConversor.class, AdicionadorLinkCliente.class })
class ClienteControleTest {

    @Autowired
    private MockMvc requisicao;

    @MockBean
    private ClienteServico servico;

    private Cliente cliente(Long id, String nome) {
        Cliente cliente = new Cliente();
        cliente.setId(id);
        cliente.setNome(nome);
        cliente.setNomeSocial("Dom Pedro");
        Calendar calendario = Calendar.getInstance();
        calendario.set(2020, Calendar.JANUARY, 1, 0, 0, 0);
        cliente.setDataCadastro(calendario.getTime());
        Telefone telefone = new Telefone();
        telefone.setId(1L);
        telefone.setDdd("21");
        telefone.setNumero("981234576");
        cliente.getTelefones().add(telefone);
        return cliente;
    }

    @Test
    @DisplayName("GET /cliente/{id}: 200 com o cliente serializado")
    void obterPorIdOk() throws Exception {
        when(servico.obterPorId(1L)).thenReturn(cliente(1L, "Pedro de Alcantara"));

        requisicao.perform(get("/cliente/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Pedro de Alcantara"))
                .andExpect(jsonPath("$.dataCadastro").exists())
                .andExpect(jsonPath("$.telefones[0].ddd").value("21"));
    }

    @Test
    @DisplayName("GET /cliente/{id}: 404 com corpo de erro quando nao existe")
    void obterPorIdInexistente() throws Exception {
        when(servico.obterPorId(999L))
                .thenThrow(new RecursoNaoEncontradoException("Cliente", 999L));

        requisicao.perform(get("/cliente/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.erro").value("Not Found"))
                .andExpect(jsonPath("$.mensagem").value("Cliente de id 999 nao foi encontrado"))
                .andExpect(jsonPath("$.caminho").value("/cliente/999"));
    }

    @Test
    @DisplayName("GET /cliente/cliente/{id}: 404, a rota duplicada nao existe mais")
    void rotaAntigaDuplicadaNaoExiste() throws Exception {
        requisicao.perform(get("/cliente/cliente/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /cliente/clientes: 200 com a lista")
    void listarOk() throws Exception {
        when(servico.listar()).thenReturn(List.of(cliente(1L, "Primeiro"), cliente(2L, "Segundo")));

        requisicao.perform(get("/cliente/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nome").value("Primeiro"))
                .andExpect(jsonPath("$[1].nome").value("Segundo"));
    }

    @Test
    @DisplayName("POST /cliente/cadastro: 201 com header Location")
    void cadastrarOk() throws Exception {
        when(servico.cadastrar(any(Cliente.class))).thenReturn(cliente(2L, "Maria Silva"));

        String corpo = """
                { "nome": "Maria Silva" }
                """;

        requisicao.perform(post("/cliente/cadastro")
                .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/cliente/2"))
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.nome").value("Maria Silva"));
    }

    @Test
    @DisplayName("POST /cliente/cadastro: 400 quando o nome nao e informado")
    void cadastrarSemNome() throws Exception {
        String corpo = """
                { "nomeSocial": "So apelido" }
                """;

        requisicao.perform(post("/cliente/cadastro")
                .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.erros[*].campo", hasItem("nome")));

        verifyNoInteractions(servico);
    }

    @Test
    @DisplayName("POST /cliente/cadastro: 400 quando a data de nascimento esta no futuro")
    void cadastrarDataNoFuturo() throws Exception {
        String corpo = """
                { "nome": "Futuro", "dataNascimento": "2099-01-01T00:00:00.000+00:00" }
                """;

        requisicao.perform(post("/cliente/cadastro")
                .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros[*].campo", hasItem("dataNascimento")));

        verifyNoInteractions(servico);
    }

    @Test
    @DisplayName("POST /cliente/cadastro: 400 com caminho indexado nos filhos invalidos")
    void cadastrarFilhosInvalidos() throws Exception {
        String corpo = """
                {
                  "nome": "Invalido",
                  "telefones": [ { "ddd": "1", "numero": "abc" } ],
                  "enderecos": [ { "bairro": "Centro" } ],
                  "documentos": [ { "tipo": "" } ]
                }
                """;

        requisicao.perform(post("/cliente/cadastro")
                .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros[*].campo", hasItem("telefones[0].ddd")))
                .andExpect(jsonPath("$.erros[*].campo", hasItem("telefones[0].numero")))
                .andExpect(jsonPath("$.erros[*].campo", hasItem("documentos[0].tipo")))
                .andExpect(jsonPath("$.erros[*].campo", hasItem("enderecos[0].cidade")))
                .andExpect(jsonPath("$.erros[*].campo", hasItem("enderecos[0].rua")))
                .andExpect(jsonPath("$.erros[*].campo", hasItem("enderecos[0].numero")));

        verifyNoInteractions(servico);
    }

    @Test
    @DisplayName("POST /cliente/cadastro: 409 quando o banco acusa violacao de integridade")
    void cadastrarDocumentoDuplicado() throws Exception {
        when(servico.cadastrar(any(Cliente.class)))
                .thenThrow(new DataIntegrityViolationException("unique violation"));

        String corpo = """
                { "nome": "Dup", "documentos": [ { "tipo": "RG", "numero": "1500" } ] }
                """;

        requisicao.perform(post("/cliente/cadastro")
                .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.erro").value("Conflict"));
    }

    @Test
    @DisplayName("PUT /cliente/atualizar: 200 com o cliente atualizado")
    void atualizarOk() throws Exception {
        when(servico.atualizar(any(Cliente.class))).thenReturn(cliente(1L, "Nome Novo"));

        String corpo = """
                { "id": 1, "nome": "Nome Novo" }
                """;

        requisicao.perform(put("/cliente/atualizar")
                .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Nome Novo"));
    }

    @Test
    @DisplayName("PUT /cliente/atualizar: 400 quando o id nao e informado")
    void atualizarSemId() throws Exception {
        String corpo = """
                { "nome": "Sem Id" }
                """;

        requisicao.perform(put("/cliente/atualizar")
                .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros[*].campo", hasItem("id")));

        verifyNoInteractions(servico);
    }

    @Test
    @DisplayName("PUT /cliente/atualizar: 400 quando o telefone e enviado sem id")
    void atualizarTelefoneSemId() throws Exception {
        String corpo = """
                { "id": 1, "telefones": [ { "ddd": "11", "numero": "999999999" } ] }
                """;

        requisicao.perform(put("/cliente/atualizar")
                .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros[*].campo", hasItem("telefones[0].id")));

        verifyNoInteractions(servico);
    }

    @Test
    @DisplayName("PUT /cliente/atualizar: 404 quando o cliente nao existe")
    void atualizarInexistente() throws Exception {
        when(servico.atualizar(any(Cliente.class)))
                .thenThrow(new RecursoNaoEncontradoException("Cliente", 999L));

        String corpo = """
                { "id": 999, "nome": "Fantasma" }
                """;

        requisicao.perform(put("/cliente/atualizar")
                .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Cliente de id 999 nao foi encontrado"));
    }

    @Test
    @DisplayName("DELETE /cliente/excluir/{id}: 204 sem corpo")
    void excluirOk() throws Exception {
        doNothing().when(servico).excluir(1L);

        requisicao.perform(delete("/cliente/excluir/1"))
                .andExpect(status().isNoContent());

        verify(servico).excluir(1L);
    }

    @Test
    @DisplayName("DELETE /cliente/excluir/{id}: 404 quando o cliente nao existe")
    void excluirInexistente() throws Exception {
        doThrow(new RecursoNaoEncontradoException("Cliente", 999L)).when(servico).excluir(999L);

        requisicao.perform(delete("/cliente/excluir/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Cliente de id 999 nao foi encontrado"));
    }

    @Test
    @DisplayName("DELETE /cliente/excluir: 405, o id e obrigatorio na URL")
    void excluirSemIdNaUrl() throws Exception {
        requisicao.perform(delete("/cliente/excluir"))
                .andExpect(status().isMethodNotAllowed());

        verify(servico, never()).excluir(anyLong());
    }

    @Test
    @DisplayName("GET /cliente/{id}: links self, clientes, telefones, documentos e enderecos")
    void obterPorIdComLinks() throws Exception {
        when(servico.obterPorId(1L)).thenReturn(cliente(1L, "Pedro de Alcantara"));

        requisicao.perform(get("/cliente/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links.self.href").value("http://localhost/cliente/1"))
                .andExpect(jsonPath("$._links.clientes.href").value("http://localhost/cliente/clientes"))
                .andExpect(jsonPath("$._links.telefones.href").value("http://localhost/cliente/1/telefones"))
                .andExpect(jsonPath("$._links.documentos.href").value("http://localhost/cliente/1/documentos"))
                .andExpect(jsonPath("$._links.enderecos.href").value("http://localhost/cliente/1/enderecos"));
    }

    @Test
    @DisplayName("GET /cliente/clientes: link self em cada cliente da lista")
    void listarComLinks() throws Exception {
        when(servico.listar()).thenReturn(List.of(cliente(1L, "Primeiro"), cliente(2L, "Segundo")));

        requisicao.perform(get("/cliente/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].links.length()").value(1))
                .andExpect(jsonPath("$[0].links[0].rel").value("self"))
                .andExpect(jsonPath("$[0].links[0].href").value("http://localhost/cliente/1"))
                .andExpect(jsonPath("$[1].links[0].rel").value("self"))
                .andExpect(jsonPath("$[1].links[0].href").value("http://localhost/cliente/2"));
    }

    @Test
    @DisplayName("POST /cliente/cadastro: resposta com link self do cliente criado")
    void cadastrarComLinks() throws Exception {
        when(servico.cadastrar(any(Cliente.class))).thenReturn(cliente(2L, "Maria Silva"));

        String corpo = """
                { "nome": "Maria Silva" }
                """;

        requisicao.perform(post("/cliente/cadastro")
                .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$._links.self.href").value("http://localhost/cliente/2"));
    }

    @Test
    @DisplayName("PUT /cliente/atualizar: resposta com link self do cliente atualizado")
    void atualizarComLinks() throws Exception {
        when(servico.atualizar(any(Cliente.class))).thenReturn(cliente(1L, "Nome Novo"));

        String corpo = """
                { "id": 1, "nome": "Nome Novo" }
                """;

        requisicao.perform(put("/cliente/atualizar")
                .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links.self.href").value("http://localhost/cliente/1"));
    }

    @Test
    @DisplayName("GET /cliente/{id}: resposta de erro nao tem links")
    void erroSemLinks() throws Exception {
        when(servico.obterPorId(999L))
                .thenThrow(new RecursoNaoEncontradoException("Cliente", 999L));

        requisicao.perform(get("/cliente/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$._links").doesNotExist())
                .andExpect(jsonPath("$.links").doesNotExist());
    }
}
