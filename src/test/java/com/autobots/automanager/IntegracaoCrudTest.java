package com.autobots.automanager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.autobots.automanager.repositorios.ClienteRepositorio;
import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@AutoConfigureMockMvc
class IntegracaoCrudTest {

    @Autowired
    private MockMvc requisicao;
    @Autowired
    private ClienteRepositorio repositorio;

    private long cadastrar(String corpo) throws Exception {
        String resposta = requisicao.perform(post("/cliente/cadastro")
                .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(resposta, "$.id")).longValue();
    }

    @Test
    @DisplayName("ciclo completo do cliente: cadastrar, consultar, atualizar e remover")
    void cicloCompletoDoCliente() throws Exception {
        long id = cadastrar("""
                {
                  "nome": "Joana Ferreira",
                  "nomeSocial": "Joana",
                  "documentos": [ { "tipo": "RG", "numero": "INT-001" } ],
                  "enderecos": [ {
                    "cidade": "Santos", "rua": "Avenida Ana Costa", "numero": "250",
                    "codigoPostal": "11060002"
                  } ],
                  "telefones": [ { "ddd": "13", "numero": "981234567" } ]
                }
                """);

        requisicao.perform(get("/cliente/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Joana Ferreira"))
                .andExpect(jsonPath("$.enderecos[0].cidade").value("Santos"))
                .andExpect(jsonPath("$.documentos[0].numero").value("INT-001"))
                .andExpect(jsonPath("$.telefones[0].ddd").value("13"));

        requisicao.perform(put("/cliente/atualizar")
                .contentType(MediaType.APPLICATION_JSON).content("""
                        { "id": %d, "nomeSocial": "Jo" }
                        """.formatted(id)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nomeSocial").value("Jo"))
                .andExpect(jsonPath("$.nome").value("Joana Ferreira"));

        requisicao.perform(delete("/cliente/excluir/" + id))
                .andExpect(status().isNoContent());

        requisicao.perform(get("/cliente/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("P14: a dataCadastro e definida pelo servidor, nao pelo cliente da API")
    void dataCadastroVemDoServidor() throws Exception {
        String resposta = requisicao.perform(post("/cliente/cadastro")
                .contentType(MediaType.APPLICATION_JSON).content("""
                        { "nome": "Sem Data", "dataCadastro": "1990-01-01T00:00:00.000+00:00" }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.dataCadastro").exists())
                .andReturn().getResponse().getContentAsString();

        String dataCadastro = JsonPath.read(resposta, "$.dataCadastro");
        assertNotEquals("1990-01-01T00:00:00.000+00:00", dataCadastro);
    }

    @Test
    @DisplayName("P5: POST com id cria um cliente novo e nao sobrescreve o existente")
    void postComIdNaoSobrescreve() throws Exception {
        long original = cadastrar("""
                { "nome": "Original" }
                """);

        long novo = cadastrar("""
                { "id": %d, "nome": "Tentativa de Sobrescrever" }
                """.formatted(original));

        assertNotEquals(original, novo);

        requisicao.perform(get("/cliente/" + original))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Original"));
    }

    @Test
    @DisplayName("409 no documento duplicado, e a transacao nao deixa cliente orfao")
    void documentoDuplicadoFazRollback() throws Exception {
        cadastrar("""
                { "nome": "Dono do Documento",
                  "documentos": [ { "tipo": "RG", "numero": "INT-DUP" } ] }
                """);

        long antes = repositorio.count();

        requisicao.perform(post("/cliente/cadastro")
                .contentType(MediaType.APPLICATION_JSON).content("""
                        { "nome": "Vai Falhar",
                          "documentos": [ { "tipo": "RG", "numero": "INT-DUP" } ] }
                        """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));

        assertEquals(antes, repositorio.count(), "o POST que falhou deixou cliente orfao");
    }

    @Test
    @DisplayName("ciclo completo do telefone nas rotas aninhadas")
    void cicloCompletoDoTelefone() throws Exception {
        long idCliente = cadastrar("""
                { "nome": "Dono do Telefone" }
                """);

        String resposta = requisicao.perform(post("/cliente/" + idCliente + "/telefones")
                .contentType(MediaType.APPLICATION_JSON).content("""
                        { "ddd": "13", "numero": "981234567" }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andReturn().getResponse().getContentAsString();
        long idTelefone = ((Number) JsonPath.read(resposta, "$.id")).longValue();

        requisicao.perform(get("/cliente/" + idCliente + "/telefones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value((int) idTelefone));

        requisicao.perform(put("/telefone/" + idTelefone)
                .contentType(MediaType.APPLICATION_JSON).content("""
                        { "ddd": "11" }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ddd").value("11"))
                .andExpect(jsonPath("$.numero").value("981234567"));

        requisicao.perform(delete("/telefone/" + idTelefone))
                .andExpect(status().isNoContent());

        requisicao.perform(get("/telefone/" + idTelefone))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("ciclo completo do documento nas rotas aninhadas")
    void cicloCompletoDoDocumento() throws Exception {
        long idCliente = cadastrar("""
                { "nome": "Dono do Documento 2" }
                """);

        String resposta = requisicao.perform(post("/cliente/" + idCliente + "/documentos")
                .contentType(MediaType.APPLICATION_JSON).content("""
                        { "tipo": "CNH", "numero": "INT-CNH" }
                        """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long idDocumento = ((Number) JsonPath.read(resposta, "$.id")).longValue();

        requisicao.perform(get("/cliente/" + idCliente + "/documentos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        requisicao.perform(put("/documento/" + idDocumento)
                .contentType(MediaType.APPLICATION_JSON).content("""
                        { "tipo": "CNH-E" }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("CNH-E"))
                .andExpect(jsonPath("$.numero").value("INT-CNH"));

        requisicao.perform(delete("/documento/" + idDocumento))
                .andExpect(status().isNoContent());

        requisicao.perform(get("/documento/" + idDocumento))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("ciclo completo do endereco nas rotas aninhadas, com N enderecos por cliente")
    void cicloCompletoDoEndereco() throws Exception {
        long idCliente = cadastrar("""
                { "nome": "Sem Endereco Ainda" }
                """);

        requisicao.perform(get("/cliente/" + idCliente + "/enderecos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        String resposta = requisicao.perform(post("/cliente/" + idCliente + "/enderecos")
                .contentType(MediaType.APPLICATION_JSON).content("""
                        { "cidade": "Santos", "rua": "Rua XV", "numero": "100",
                          "codigoPostal": "11010151" }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cidade").value("Santos"))
                .andReturn().getResponse().getContentAsString();
        long idEndereco = ((Number) JsonPath.read(resposta, "$.id")).longValue();

        requisicao.perform(post("/cliente/" + idCliente + "/enderecos")
                .contentType(MediaType.APPLICATION_JSON).content("""
                        { "cidade": "Rio de Janeiro", "rua": "Avenida Atlantica",
                          "numero": "1702" }
                        """))
                .andExpect(status().isCreated());

        requisicao.perform(get("/cliente/" + idCliente + "/enderecos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        requisicao.perform(get("/endereco/" + idEndereco))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigoPostal").value("11010151"));

        requisicao.perform(put("/endereco/" + idEndereco)
                .contentType(MediaType.APPLICATION_JSON).content("""
                        { "cidade": "Guaruja" }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cidade").value("Guaruja"))
                .andExpect(jsonPath("$.rua").value("Rua XV"));

        requisicao.perform(delete("/endereco/" + idEndereco))
                .andExpect(status().isNoContent());

        requisicao.perform(get("/endereco/" + idEndereco))
                .andExpect(status().isNotFound());

        requisicao.perform(get("/cliente/" + idCliente + "/enderecos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("excluir o cliente remove os filhos em cascata")
    void excluirClienteRemoveFilhos() throws Exception {
        long idCliente = cadastrar("""
                { "nome": "Com Filhos",
                  "documentos": [ { "tipo": "RG", "numero": "INT-CASCATA" } ],
                  "telefones": [ { "ddd": "13", "numero": "981234567" } ],
                  "enderecos": [ { "cidade": "Santos", "rua": "Rua XV", "numero": "1" } ] }
                """);

        String resposta = requisicao.perform(get("/cliente/" + idCliente))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        long idTelefone = ((Number) JsonPath.read(resposta, "$.telefones[0].id")).longValue();
        long idDocumento = ((Number) JsonPath.read(resposta, "$.documentos[0].id")).longValue();

        requisicao.perform(delete("/cliente/excluir/" + idCliente))
                .andExpect(status().isNoContent());

        requisicao.perform(get("/telefone/" + idTelefone))
                .andExpect(status().isNotFound());
        requisicao.perform(get("/documento/" + idDocumento))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("a carga inicial cria o cliente de demonstracao")
    void cargaInicialExecutou() throws Exception {
        requisicao.perform(get("/cliente/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Pedro Alcantara de Braganca e Bourbon"))
                .andExpect(jsonPath("$[0].documentos.length()").value(2));
    }

    @Test
    @DisplayName("P18: a listagem de clientes vem ordenada por id")
    void listagemOrdenadaPorId() throws Exception {
        cadastrar("""
                { "nome": "Ordem A" }
                """);
        cadastrar("""
                { "nome": "Ordem B" }
                """);

        String resposta = requisicao.perform(get("/cliente/clientes"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<Integer> ids = JsonPath.read(resposta, "$[*].id");
        List<Integer> ordenados = new ArrayList<>(ids);
        Collections.sort(ordenados);
        assertEquals(ordenados, ids, "a listagem nao veio ordenada por id");
    }

    @Test
    @DisplayName("HATEOAS: do cliente se chega ao telefone seguindo apenas os links")
    void navegacaoPelosLinks() throws Exception {
        long idCliente = cadastrar("""
                { "nome": "Navegador",
                  "telefones": [ { "ddd": "13", "numero": "981234567" } ] }
                """);

        String cliente = requisicao.perform(get("/cliente/" + idCliente))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String linkTelefones = JsonPath.read(cliente, "$._links.telefones.href");

        String telefones = requisicao.perform(get(linkTelefones))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andReturn().getResponse().getContentAsString();
        String linkTelefone = JsonPath.read(telefones, "$[0].links[0].href");

        String telefone = requisicao.perform(get(linkTelefone))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ddd").value("13"))
                .andReturn().getResponse().getContentAsString();
        String linkProprio = JsonPath.read(telefone, "$._links.self.href");

        assertEquals(linkTelefone, linkProprio);
    }
}
