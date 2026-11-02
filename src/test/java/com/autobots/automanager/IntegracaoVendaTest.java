package com.autobots.automanager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.jayway.jsonpath.JsonPath;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:autobots-venda;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
class IntegracaoVendaTest {

    private static final String MERCADORIA = """
            { "nome": "Pneu", "fabricacao": "2025-01-15T00:00:00.000+00:00", "quantidade": 10, "valor": 300.00 }
            """;
    private static final String SERVICO = """
            { "nome": "Alinhamento", "valor": 50.00 }
            """;

    @Autowired
    private MockMvc requisicao;

    private long criar(String url, String corpo) throws Exception {
        String resposta = requisicao.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(resposta, "$.id")).longValue();
    }

    private ResultActions enviarPost(String url, String corpo) throws Exception {
        return requisicao.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(corpo));
    }

    private ResultActions enviarPut(String url, String corpo) throws Exception {
        return requisicao.perform(put(url).contentType(MediaType.APPLICATION_JSON).content(corpo));
    }

    private long empresa() throws Exception {
        return criar("/empresa/cadastro", """
                { "razaoSocial": "Oficina" }
                """);
    }

    private long usuario(String perfil) throws Exception {
        return criar("/usuario/cadastro", "{ \"nome\": \"Pessoa\", \"perfis\": [ \"" + perfil + "\" ] }");
    }

    private long veiculo(long donoId, String placa) throws Exception {
        return criar("/usuario/" + donoId + "/veiculos",
                "{ \"tipo\": \"SUV\", \"modelo\": \"Corolla Cross\", \"placa\": \"" + placa + "\" }");
    }

    private long funcionarioDa(long empresaId) throws Exception {
        long funcionario = usuario("FUNCIONARIO");
        requisicao.perform(put("/empresa/" + empresaId + "/usuarios/" + funcionario))
                .andExpect(status().isNoContent());
        return funcionario;
    }

    private long estoque(long mercadoria) throws Exception {
        String resposta = requisicao.perform(get("/mercadoria/" + mercadoria))
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(resposta, "$.quantidade")).longValue();
    }

    private String itens(long mercadoria, long quantidade) {
        return "\"itens\": [ { \"mercadoriaId\": " + mercadoria + ", \"quantidade\": " + quantidade + " } ]";
    }

    private String venda(String identificacao, long cliente, long funcionario, Long veiculo, long mercadoria,
            long servico) {
        return "{ \"identificacao\": \"" + identificacao + "\", \"clienteId\": " + cliente
                + ", \"funcionarioId\": " + funcionario
                + (veiculo == null ? "" : ", \"veiculoId\": " + veiculo)
                + ", \"itens\": [ { \"mercadoriaId\": " + mercadoria + ", \"quantidade\": 1 } ], \"servicoIds\": [ " + servico + " ] }";
    }

    @Test
    @DisplayName("ciclo completo do veiculo, com placa duplicada e placa invalida")
    void cicloDoVeiculo() throws Exception {
        long dono = usuario("CLIENTE");
        long id = veiculo(dono, "AAA1A11");

        requisicao.perform(get("/veiculo/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.placa").value("AAA1A11"))
                .andExpect(jsonPath("$.proprietarioId").value(dono))
                .andExpect(jsonPath("$._links.proprietario.href").exists())
                .andExpect(jsonPath("$._links.vendas.href").exists());
        requisicao.perform(get("/usuario/" + dono + "/veiculos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].links[0].rel").value("self"));
        enviarPost("/usuario/" + dono + "/veiculos",
                "{ \"tipo\": \"SUV\", \"modelo\": \"Outro\", \"placa\": \"AAA1A11\" }")
                .andExpect(status().isConflict());
        enviarPost("/usuario/" + dono + "/veiculos",
                "{ \"tipo\": \"SUV\", \"modelo\": \"Outro\", \"placa\": \"placa\" }")
                .andExpect(status().isBadRequest());
        enviarPut("/veiculo/" + id, "{ \"modelo\": \"Yaris\" }")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.modelo").value("Yaris"))
                .andExpect(jsonPath("$.placa").value("AAA1A11"));
        requisicao.perform(delete("/veiculo/" + id)).andExpect(status().isNoContent());
        requisicao.perform(get("/veiculo/" + id)).andExpect(status().isNotFound());
        requisicao.perform(get("/veiculo/999999")).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("ciclo completo da mercadoria e o vinculo com o fornecedor")
    void cicloDaMercadoria() throws Exception {
        long empresa = empresa();
        long id = criar("/empresa/" + empresa + "/mercadorias", MERCADORIA);

        requisicao.perform(get("/mercadoria/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Pneu"))
                .andExpect(jsonPath("$.quantidade").value(10))
                .andExpect(jsonPath("$.cadastro").exists());
        requisicao.perform(get("/empresa/" + empresa + "/mercadorias"))
                .andExpect(jsonPath("$.length()").value(1));
        enviarPut("/mercadoria/" + id, "{ \"quantidade\": 0 }")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidade").value(0))
                .andExpect(jsonPath("$.nome").value("Pneu"));
        enviarPut("/mercadoria/" + id, "{ \"validade\": \"2000-01-01T00:00:00.000+00:00\" }")
                .andExpect(status().isBadRequest());
        requisicao.perform(get("/mercadoria/" + id))
                .andExpect(jsonPath("$.validade").doesNotExist());
        enviarPost("/empresa/" + empresa + "/mercadorias", "{ \"nome\": \"Sem dados\" }")
                .andExpect(status().isBadRequest());

        long fornecedor = usuario("FORNECEDOR");
        long outroFornecedor = usuario("FORNECEDOR");
        long cliente = usuario("CLIENTE");
        requisicao.perform(put("/usuario/" + cliente + "/mercadorias/" + id))
                .andExpect(status().isBadRequest());
        requisicao.perform(put("/usuario/" + fornecedor + "/mercadorias/" + id))
                .andExpect(status().isNoContent());
        requisicao.perform(put("/usuario/" + fornecedor + "/mercadorias/" + id))
                .andExpect(status().isNoContent());
        requisicao.perform(put("/usuario/" + outroFornecedor + "/mercadorias/" + id))
                .andExpect(status().isConflict());
        requisicao.perform(get("/usuario/" + fornecedor + "/mercadorias"))
                .andExpect(jsonPath("$.length()").value(1));
        requisicao.perform(delete("/usuario/" + outroFornecedor + "/mercadorias/" + id))
                .andExpect(status().isNotFound());
        requisicao.perform(delete("/usuario/" + fornecedor + "/mercadorias/" + id))
                .andExpect(status().isNoContent());
        requisicao.perform(get("/usuario/" + fornecedor + "/mercadorias"))
                .andExpect(jsonPath("$.length()").value(0));

        requisicao.perform(get("/mercadoria/" + id)).andExpect(status().isOk());
        requisicao.perform(delete("/mercadoria/" + id)).andExpect(status().isNoContent());
        requisicao.perform(get("/mercadoria/" + id)).andExpect(status().isNotFound());
        requisicao.perform(get("/empresa/" + empresa + "/mercadorias"))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("ciclo completo do servico")
    void cicloDoServico() throws Exception {
        long empresa = empresa();
        long id = criar("/empresa/" + empresa + "/servicos", SERVICO);

        requisicao.perform(get("/servico/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Alinhamento"));
        requisicao.perform(get("/empresa/" + empresa + "/servicos"))
                .andExpect(jsonPath("$.length()").value(1));
        enviarPut("/servico/" + id, "{ \"valor\": 75.50 }")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valor").value(75.50))
                .andExpect(jsonPath("$.nome").value("Alinhamento"));
        enviarPost("/empresa/" + empresa + "/servicos", "{ \"nome\": \"Caro\", \"valor\": -1 }")
                .andExpect(status().isBadRequest());
        requisicao.perform(delete("/servico/" + id)).andExpect(status().isNoContent());
        requisicao.perform(get("/servico/" + id)).andExpect(status().isNotFound());
        requisicao.perform(get("/empresa/" + empresa + "/servicos"))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("ciclo completo da venda, com os links e o bloqueio de exclusao dos itens vendidos")
    void cicloDaVenda() throws Exception {
        long empresa = empresa();
        long cliente = usuario("CLIENTE");
        long funcionario = funcionarioDa(empresa);
        long carro = veiculo(cliente, "BBB1B11");
        long mercadoria = criar("/empresa/" + empresa + "/mercadorias", MERCADORIA);
        long servico = criar("/empresa/" + empresa + "/servicos", SERVICO);

        long id = criar("/empresa/" + empresa + "/vendas",
                venda("CICLO-1", cliente, funcionario, carro, mercadoria, servico));

        String resposta = requisicao.perform(get("/venda/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.identificacao").value("CICLO-1"))
                .andExpect(jsonPath("$.clienteId").value(cliente))
                .andExpect(jsonPath("$.funcionarioId").value(funcionario))
                .andExpect(jsonPath("$.veiculoId").value(carro))
                .andExpect(jsonPath("$.itens[0].nome").value("Pneu"))
                .andExpect(jsonPath("$.servicos[0].nome").value("Alinhamento"))
                .andReturn().getResponse().getContentAsString();
        assertEquals("http://localhost/usuario/" + cliente, JsonPath.read(resposta, "$._links.cliente.href"));
        assertEquals("http://localhost/veiculo/" + carro, JsonPath.read(resposta, "$._links.veiculo.href"));

        requisicao.perform(get("/empresa/" + empresa + "/vendas")).andExpect(jsonPath("$.length()").value(1));
        requisicao.perform(get("/usuario/" + cliente + "/vendas")).andExpect(jsonPath("$.length()").value(1));
        requisicao.perform(get("/usuario/" + funcionario + "/vendas")).andExpect(jsonPath("$.length()").value(1));
        requisicao.perform(get("/veiculo/" + carro + "/vendas")).andExpect(jsonPath("$.length()").value(1));

        requisicao.perform(delete("/mercadoria/" + mercadoria)).andExpect(status().isConflict());
        requisicao.perform(delete("/servico/" + servico)).andExpect(status().isConflict());
        requisicao.perform(delete("/veiculo/" + carro)).andExpect(status().isConflict());
        requisicao.perform(delete("/usuario/excluir/" + cliente)).andExpect(status().isConflict());

        enviarPut("/venda/" + id, "{ \"identificacao\": \"CICLO-2\", \"itens\": [] }")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.identificacao").value("CICLO-2"))
                .andExpect(jsonPath("$.itens.length()").value(0))
                .andExpect(jsonPath("$.servicos.length()").value(1));
        enviarPut("/venda/" + id, "{ \"servicoIds\": [] }")
                .andExpect(status().isBadRequest());
        requisicao.perform(get("/venda/" + id))
                .andExpect(jsonPath("$.servicos.length()").value(1));

        requisicao.perform(delete("/mercadoria/" + mercadoria)).andExpect(status().isNoContent());

        requisicao.perform(delete("/venda/" + id)).andExpect(status().isNoContent());
        requisicao.perform(get("/venda/" + id)).andExpect(status().isNotFound());
        requisicao.perform(get("/empresa/" + empresa + "/vendas")).andExpect(jsonPath("$.length()").value(0));
        requisicao.perform(delete("/servico/" + servico)).andExpect(status().isNoContent());
        requisicao.perform(delete("/veiculo/" + carro)).andExpect(status().isNoContent());
        requisicao.perform(delete("/usuario/excluir/" + cliente)).andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("as regras da venda recusam dados incoerentes")
    void regrasDaVenda() throws Exception {
        long empresa = empresa();
        long outraEmpresa = empresa();
        long cliente = usuario("CLIENTE");
        long outroCliente = usuario("CLIENTE");
        long funcionario = funcionarioDa(empresa);
        long funcionarioDeOutra = funcionarioDa(outraEmpresa);
        long semVinculo = usuario("FUNCIONARIO");
        long carro = veiculo(cliente, "CCC1C11");
        long carroDeOutro = veiculo(outroCliente, "DDD1D11");
        long mercadoria = criar("/empresa/" + empresa + "/mercadorias", MERCADORIA);
        long servico = criar("/empresa/" + empresa + "/servicos", SERVICO);
        long mercadoriaDeOutra = criar("/empresa/" + outraEmpresa + "/mercadorias", MERCADORIA);
        long servicoDeOutra = criar("/empresa/" + outraEmpresa + "/servicos", SERVICO);
        String url = "/empresa/" + empresa + "/vendas";

        enviarPost(url, venda("R1", funcionario, funcionario, carro, mercadoria, servico))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("O usuario de id " + funcionario
                        + " nao possui o perfil CLIENTE"));
        enviarPost(url, venda("R2", cliente, cliente, carro, mercadoria, servico))
                .andExpect(status().isBadRequest());
        enviarPost(url, venda("R3", cliente, funcionarioDeOutra, carro, mercadoria, servico))
                .andExpect(status().isBadRequest());
        enviarPost(url, venda("R4", cliente, semVinculo, carro, mercadoria, servico))
                .andExpect(status().isBadRequest());
        enviarPost(url, venda("R5", cliente, funcionario, carroDeOutro, mercadoria, servico))
                .andExpect(status().isBadRequest());
        enviarPost(url, venda("R6", cliente, funcionario, carro, mercadoriaDeOutra, servico))
                .andExpect(status().isBadRequest());
        enviarPost(url, venda("R7", cliente, funcionario, carro, mercadoria, servicoDeOutra))
                .andExpect(status().isBadRequest());
        enviarPost(url, "{ \"identificacao\": \"R8\", \"clienteId\": " + cliente
                + ", \"funcionarioId\": " + funcionario + " }")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("A venda deve ter ao menos uma mercadoria ou um servico"));
        enviarPost(url, "{ \"identificacao\": \"R9\", \"clienteId\": " + cliente + ", \"funcionarioId\": "
                + funcionario + ", \"itens\": [ { \"mercadoriaId\": " + mercadoria + ", \"quantidade\": 1 }, "
                + "{ \"mercadoriaId\": " + mercadoria + ", \"quantidade\": 2 } ] }")
                .andExpect(status().isBadRequest());
        enviarPost(url, "{ \"clienteId\": " + cliente + " }")
                .andExpect(status().isBadRequest());
        enviarPost(url, venda("R10", 999999L, funcionario, null, mercadoria, servico))
                .andExpect(status().isNotFound());
        enviarPost(url, venda("R11", cliente, funcionario, null, 999999L, servico))
                .andExpect(status().isNotFound());
        enviarPost("/empresa/999999/vendas", venda("R12", cliente, funcionario, null, mercadoria, servico))
                .andExpect(status().isNotFound());
        requisicao.perform(get(url)).andExpect(jsonPath("$.length()").value(0));

        long id = criar(url, venda("R13", cliente, funcionario, null, mercadoria, servico));
        enviarPost(url, venda("R13", cliente, funcionario, null, mercadoria, servico))
                .andExpect(status().isConflict());
        enviarPut("/venda/" + id, "{ \"veiculoId\": " + carroDeOutro + " }")
                .andExpect(status().isBadRequest());
        enviarPut("/venda/" + id, "{ \"veiculoId\": " + carro + " }")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.veiculoId").value(carro));
        enviarPut("/venda/" + id, "{ \"clienteId\": " + outroCliente + " }")
                .andExpect(status().isBadRequest());
        enviarPut("/venda/" + id, "{ \"itens\": [ { \"mercadoriaId\": " + mercadoriaDeOutra + ", \"quantidade\": 1 } ] }")
                .andExpect(status().isBadRequest());
        enviarPut("/venda/999999", "{ \"identificacao\": \"X\" }")
                .andExpect(status().isNotFound());
        requisicao.perform(get("/venda/" + id))
                .andExpect(jsonPath("$.clienteId").value(cliente))
                .andExpect(jsonPath("$.itens[0].mercadoriaId").value(mercadoria));
    }

    @Test
    @DisplayName("excluir a empresa apaga as vendas e o catalogo sem apagar os usuarios")
    void excluirEmpresaComVendas() throws Exception {
        long empresa = empresa();
        long cliente = usuario("CLIENTE");
        long funcionario = funcionarioDa(empresa);
        long mercadoria = criar("/empresa/" + empresa + "/mercadorias", MERCADORIA);
        long servico = criar("/empresa/" + empresa + "/servicos", SERVICO);
        long venda = criar("/empresa/" + empresa + "/vendas",
                venda("EXCLUIR-1", cliente, funcionario, null, mercadoria, servico));

        requisicao.perform(delete("/empresa/excluir/" + empresa)).andExpect(status().isNoContent());

        requisicao.perform(get("/venda/" + venda)).andExpect(status().isNotFound());
        requisicao.perform(get("/mercadoria/" + mercadoria)).andExpect(status().isNotFound());
        requisicao.perform(get("/servico/" + servico)).andExpect(status().isNotFound());
        requisicao.perform(get("/usuario/" + cliente)).andExpect(status().isOk());
    }

    @Test
    @DisplayName("HATEOAS: da empresa se chega a venda, e da venda ao cliente e ao veiculo, pelos links")
    void navegacaoAteAVenda() throws Exception {
        String empresas = requisicao.perform(get("/empresa/empresas"))
                .andReturn().getResponse().getContentAsString();
        String linkEmpresa = JsonPath.read(empresas, "$[0].links[0].href");

        String empresa = requisicao.perform(get(linkEmpresa))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        requisicao.perform(get((String) JsonPath.read(empresa, "$._links.mercadorias.href")))
                .andExpect(status().isOk());
        requisicao.perform(get((String) JsonPath.read(empresa, "$._links.servicos.href")))
                .andExpect(status().isOk());
        String vendas = requisicao.perform(get((String) JsonPath.read(empresa, "$._links.vendas.href")))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String venda = requisicao.perform(get((String) JsonPath.read(vendas, "$[0].links[0].href")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itens[0]._links.mercadoria.href").exists())
                .andExpect(jsonPath("$.servicos[0]._links.self.href").exists())
                .andReturn().getResponse().getContentAsString();
        requisicao.perform(get((String) JsonPath.read(venda, "$._links.funcionario.href")))
                .andExpect(status().isOk());
        String veiculo = requisicao.perform(get((String) JsonPath.read(venda, "$._links.veiculo.href")))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String cliente = requisicao.perform(get((String) JsonPath.read(veiculo, "$._links.proprietario.href")))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        requisicao.perform(get((String) JsonPath.read(cliente, "$._links.veiculos.href")))
                .andExpect(status().isOk());
        requisicao.perform(get((String) JsonPath.read(cliente, "$._links.vendas.href")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
        requisicao.perform(get((String) JsonPath.read(cliente, "$._links.mercadorias.href")))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("perfil nulo e recusado sem criar usuario nem apagar os perfis de quem ja existe")
    void perfilNuloNaoAlteraDados() throws Exception {
        long cliente = usuario("CLIENTE");
        String antes = requisicao.perform(get("/usuario/usuarios"))
                .andReturn().getResponse().getContentAsString();
        int quantidade = JsonPath.read(antes, "$.length()");

        enviarPost("/usuario/cadastro", "{ \"nome\": \"Fantasma\", \"perfis\": [ null ] }")
                .andExpect(status().isBadRequest());
        enviarPut("/usuario/atualizar", "{ \"id\": " + cliente + ", \"perfis\": [ null ] }")
                .andExpect(status().isBadRequest());

        requisicao.perform(get("/usuario/usuarios"))
                .andExpect(jsonPath("$.length()").value(quantidade));
        requisicao.perform(get("/usuario/" + cliente))
                .andExpect(jsonPath("$.perfis[0]").value("CLIENTE"));
    }

    @Test
    @DisplayName("listas nulas na venda: no cadastro valem como vazias, e na atualizacao nao removem itens")
    void listasNulasNaVenda() throws Exception {
        long empresa = empresa();
        long cliente = usuario("CLIENTE");
        long funcionario = funcionarioDa(empresa);
        long servico = criar("/empresa/" + empresa + "/servicos", SERVICO);
        String url = "/empresa/" + empresa + "/vendas";

        long id = criar(url, "{ \"identificacao\": \"NULA-1\", \"clienteId\": " + cliente
                + ", \"funcionarioId\": " + funcionario + ", \"itens\": null, \"servicoIds\": [ " + servico + " ] }");
        enviarPost(url, "{ \"identificacao\": \"NULA-2\", \"clienteId\": " + cliente
                + ", \"funcionarioId\": " + funcionario + ", \"itens\": null, \"servicoIds\": null }")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("A venda deve ter ao menos uma mercadoria ou um servico"));

        enviarPut("/venda/" + id, "{ \"itens\": null, \"servicoIds\": null }")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.servicos.length()").value(1));
    }

    @Test
    @DisplayName("estoque: a venda baixa a quantidade, o PUT ajusta e o DELETE devolve")
    void cicloDoEstoque() throws Exception {
        long empresa = empresa();
        long cliente = usuario("CLIENTE");
        long funcionario = funcionarioDa(empresa);
        long mercadoria = criar("/empresa/" + empresa + "/mercadorias", MERCADORIA);
        long servico = criar("/empresa/" + empresa + "/servicos", SERVICO);
        String pessoas = "\"clienteId\": " + cliente + ", \"funcionarioId\": " + funcionario;
        String url = "/empresa/" + empresa + "/vendas";
        assertEquals(10, estoque(mercadoria));

        long id = criar(url, "{ \"identificacao\": \"EST-1\", " + pessoas + ", " + itens(mercadoria, 4) + " }");
        assertEquals(6, estoque(mercadoria));
        requisicao.perform(get("/venda/" + id))
                .andExpect(jsonPath("$.itens.length()").value(1))
                .andExpect(jsonPath("$.itens[0].quantidade").value(4));

        enviarPost(url, "{ \"identificacao\": \"EST-2\", " + pessoas + ", " + itens(mercadoria, 7) + " }")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem").value("Estoque insuficiente para a mercadoria de id "
                        + mercadoria + ". Quantidade solicitada: 7"));
        assertEquals(6, estoque(mercadoria));

        enviarPut("/venda/" + id, "{ " + itens(mercadoria, 9) + " }")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itens.length()").value(1))
                .andExpect(jsonPath("$.itens[0].id").exists())
                .andExpect(jsonPath("$.itens[0].quantidade").value(9));
        assertEquals(1, estoque(mercadoria));
        requisicao.perform(get("/venda/" + id))
                .andExpect(jsonPath("$.itens.length()").value(1));

        enviarPut("/venda/" + id, "{ " + itens(mercadoria, 11) + " }")
                .andExpect(status().isConflict());
        assertEquals(1, estoque(mercadoria));
        requisicao.perform(get("/venda/" + id))
                .andExpect(jsonPath("$.itens[0].quantidade").value(9));

        enviarPut("/venda/" + id, "{ \"itens\": [], \"servicoIds\": [ " + servico + " ] }")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itens.length()").value(0));
        assertEquals(10, estoque(mercadoria));

        enviarPut("/venda/" + id, "{ " + itens(mercadoria, 10) + " }")
                .andExpect(status().isOk());
        assertEquals(0, estoque(mercadoria));
        enviarPost(url, "{ \"identificacao\": \"EST-3\", " + pessoas + ", " + itens(mercadoria, 1) + " }")
                .andExpect(status().isConflict());

        requisicao.perform(delete("/venda/" + id)).andExpect(status().isNoContent());
        assertEquals(10, estoque(mercadoria));
    }

    @Test
    @DisplayName("estoque: se um item falha, nenhum outro da mesma venda e descontado")
    void vendaComEstoqueParcial() throws Exception {
        long empresa = empresa();
        long cliente = usuario("CLIENTE");
        long funcionario = funcionarioDa(empresa);
        long primeira = criar("/empresa/" + empresa + "/mercadorias", MERCADORIA);
        long segunda = criar("/empresa/" + empresa + "/mercadorias", MERCADORIA);
        String pessoas = "\"clienteId\": " + cliente + ", \"funcionarioId\": " + funcionario;

        enviarPost("/empresa/" + empresa + "/vendas", "{ \"identificacao\": \"PAR-1\", " + pessoas
                + ", \"itens\": [ { \"mercadoriaId\": " + primeira + ", \"quantidade\": 5 }, "
                + "{ \"mercadoriaId\": " + segunda + ", \"quantidade\": 11 } ] }")
                .andExpect(status().isConflict());

        assertEquals(10, estoque(primeira));
        assertEquals(10, estoque(segunda));
        requisicao.perform(get("/empresa/" + empresa + "/vendas"))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("estoque: a venda com identificacao repetida nao desconta nada")
    void identificacaoRepetidaNaoBaixaEstoque() throws Exception {
        long empresa = empresa();
        long cliente = usuario("CLIENTE");
        long funcionario = funcionarioDa(empresa);
        long mercadoria = criar("/empresa/" + empresa + "/mercadorias", MERCADORIA);
        String corpo = "{ \"identificacao\": \"REP-1\", \"clienteId\": " + cliente
                + ", \"funcionarioId\": " + funcionario + ", " + itens(mercadoria, 2) + " }";

        criar("/empresa/" + empresa + "/vendas", corpo);
        enviarPost("/empresa/" + empresa + "/vendas", corpo).andExpect(status().isConflict());

        assertEquals(8, estoque(mercadoria));
    }

    @Test
    @DisplayName("dados de demonstracao: as duas vendas levaram 4 rodas cada, e o estoque mostra isso")
    void estoqueDaCargaInicial() throws Exception {
        String empresas = requisicao.perform(get("/empresa/empresas"))
                .andReturn().getResponse().getContentAsString();
        int empresa = JsonPath.read(empresas, "$[0].id");
        String mercadorias = requisicao.perform(get("/empresa/" + empresa + "/mercadorias"))
                .andReturn().getResponse().getContentAsString();
        assertEquals(22, ((Number) JsonPath.read(mercadorias, "$[0].quantidade")).intValue());
        requisicao.perform(get("/empresa/" + empresa + "/vendas"))
                .andExpect(jsonPath("$[0].itens[0].quantidade").value(4))
                .andExpect(jsonPath("$[1].itens[0].quantidade").value(4));
    }

    @Test
    @DisplayName("preco: a venda guarda o valor da epoca, mesmo que a mercadoria mude de preco depois")
    void precoDaEpoca() throws Exception {
        long empresa = empresa();
        long cliente = usuario("CLIENTE");
        long funcionario = funcionarioDa(empresa);
        long mercadoria = criar("/empresa/" + empresa + "/mercadorias", MERCADORIA);
        long outra = criar("/empresa/" + empresa + "/mercadorias", MERCADORIA);
        long id = criar("/empresa/" + empresa + "/vendas", "{ \"identificacao\": \"PRECO-1\", \"clienteId\": "
                + cliente + ", \"funcionarioId\": " + funcionario + ", " + itens(mercadoria, 2) + " }");

        enviarPut("/mercadoria/" + mercadoria, "{ \"valor\": 999.00 }").andExpect(status().isOk());
        enviarPut("/mercadoria/" + outra, "{ \"valor\": 500.00 }").andExpect(status().isOk());

        requisicao.perform(get("/venda/" + id))
                .andExpect(jsonPath("$.itens[0].valorUnitario").value(300.0));

        enviarPut("/venda/" + id, "{ \"itens\": [ { \"mercadoriaId\": " + mercadoria + ", \"quantidade\": 3 }, "
                + "{ \"mercadoriaId\": " + outra + ", \"quantidade\": 1 } ] }")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itens[0].valorUnitario").value(300.0))
                .andExpect(jsonPath("$.itens[1].valorUnitario").value(500.0));
    }

    @Test
    @DisplayName("regra do cliente: quem esta vinculado a outra empresa nao compra, os demais sim")
    void clienteDeOutraEmpresa() throws Exception {
        long empresa = empresa();
        long outra = empresa();
        long funcionario = funcionarioDa(empresa);
        long servico = criar("/empresa/" + empresa + "/servicos", SERVICO);
        long semEmpresa = usuario("CLIENTE");
        long daEmpresa = usuario("CLIENTE");
        long deOutra = usuario("CLIENTE");
        requisicao.perform(put("/empresa/" + empresa + "/usuarios/" + daEmpresa)).andExpect(status().isNoContent());
        requisicao.perform(put("/empresa/" + outra + "/usuarios/" + deOutra)).andExpect(status().isNoContent());
        String url = "/empresa/" + empresa + "/vendas";
        String resto = ", \"funcionarioId\": " + funcionario + ", \"servicoIds\": [ " + servico + " ] }";

        enviarPost(url, "{ \"identificacao\": \"CLI-1\", \"clienteId\": " + deOutra + resto)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("O cliente de id " + deOutra
                        + " esta vinculado a empresa de id " + outra));
        criar(url, "{ \"identificacao\": \"CLI-2\", \"clienteId\": " + semEmpresa + resto);
        long id = criar(url, "{ \"identificacao\": \"CLI-3\", \"clienteId\": " + daEmpresa + resto);
        enviarPut("/venda/" + id, "{ \"clienteId\": " + deOutra + " }")
                .andExpect(status().isBadRequest());
        requisicao.perform(get("/venda/" + id))
                .andExpect(jsonPath("$.clienteId").value(daEmpresa));
    }

    @Test
    @DisplayName("PUT de usuario: endereco inexistente e filho alheio respondem 404, sem alterar nada")
    void atualizacaoDeUsuarioIgnoradaVira404() throws Exception {
        String corpo = """
                { "nome": "Dona", "perfis": [ "CLIENTE" ], "emails": [ { "endereco": "dona@email.com" } ] }
                """;
        String resposta = enviarPost("/usuario/cadastro", corpo)
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        long dona = ((Number) JsonPath.read(resposta, "$.id")).longValue();
        long email = ((Number) JsonPath.read(resposta, "$.emails[0].id")).longValue();
        long outra = usuario("CLIENTE");

        enviarPut("/usuario/atualizar", "{ \"id\": " + outra + ", \"nome\": \"Trocado\", \"endereco\": { \"cidade\": \"Santos\" } }")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("O usuario de id " + outra
                        + " nao possui endereco. Para cadastrar, use POST /usuario/" + outra + "/endereco"));
        enviarPut("/usuario/atualizar", "{ \"id\": " + outra + ", \"nome\": \"Trocado\", \"emails\": [ { \"id\": "
                + email + ", \"endereco\": \"invasor@email.com\" } ] }")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("O e-mail de id " + email
                        + " nao pertence ao usuario de id " + outra));
        requisicao.perform(get("/usuario/" + outra))
                .andExpect(jsonPath("$.nome").value("Pessoa"));
        requisicao.perform(get("/email/" + email))
                .andExpect(jsonPath("$.endereco").value("dona@email.com"));

        enviarPut("/usuario/atualizar", "{ \"id\": " + dona + ", \"emails\": [ { \"id\": "
                + email + ", \"endereco\": \"nova@email.com\" } ] }")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.emails[0].endereco").value("nova@email.com"));
    }

    @Test
    @DisplayName("PUT de empresa: endereco inexistente e telefone alheio respondem 404")
    void atualizacaoDeEmpresaIgnoradaVira404() throws Exception {
        long empresa = empresa();
        String resposta = enviarPost("/empresa/cadastro",
                "{ \"razaoSocial\": \"Dona\", \"telefones\": [ { \"ddd\": \"11\", \"numero\": \"12345678\" } ] }")
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        long telefone = ((Number) JsonPath.read(resposta, "$.telefones[0].id")).longValue();

        enviarPut("/empresa/atualizar", "{ \"id\": " + empresa + ", \"endereco\": { \"cidade\": \"Santos\" } }")
                .andExpect(status().isNotFound());
        enviarPut("/empresa/atualizar", "{ \"id\": " + empresa + ", \"telefones\": [ { \"id\": " + telefone
                + ", \"ddd\": \"99\" } ] }")
                .andExpect(status().isNotFound());
        requisicao.perform(get("/telefone/" + telefone))
                .andExpect(jsonPath("$.ddd").value("11"));
    }

    @Test
    @DisplayName("preco: o servico da venda guarda o valor da epoca, e o total soma tudo")
    void precoDoServicoETotal() throws Exception {
        long empresa = empresa();
        long cliente = usuario("CLIENTE");
        long funcionario = funcionarioDa(empresa);
        long mercadoria = criar("/empresa/" + empresa + "/mercadorias", MERCADORIA);
        long servico = criar("/empresa/" + empresa + "/servicos", SERVICO);
        long outro = criar("/empresa/" + empresa + "/servicos", SERVICO);
        long id = criar("/empresa/" + empresa + "/vendas", "{ \"identificacao\": \"TOTAL-1\", \"clienteId\": " + cliente
                + ", \"funcionarioId\": " + funcionario + ", " + itens(mercadoria, 2) + ", \"servicoIds\": [ " + servico + " ] }");

        requisicao.perform(get("/venda/" + id))
                .andExpect(jsonPath("$.valorTotal").value(650.0));
        enviarPut("/servico/" + servico, "{ \"valor\": 999.00 }").andExpect(status().isOk());
        enviarPut("/servico/" + outro, "{ \"valor\": 10.00 }").andExpect(status().isOk());

        requisicao.perform(get("/venda/" + id))
                .andExpect(jsonPath("$.servicos[0].valor").value(50.0))
                .andExpect(jsonPath("$.valorTotal").value(650.0));
        enviarPut("/venda/" + id, "{ \"servicoIds\": [ " + servico + ", " + outro + " ] }")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.servicos.length()").value(2))
                .andExpect(jsonPath("$.servicos[0].valor").value(50.0))
                .andExpect(jsonPath("$.servicos[1].valor").value(10.0))
                .andExpect(jsonPath("$.valorTotal").value(660.0));
        requisicao.perform(get("/venda/" + id))
                .andExpect(jsonPath("$.servicos.length()").value(2));
    }

    @Test
    @DisplayName("veiculo da venda: removerVeiculo tira o veiculo e libera a exclusao dele")
    void removerVeiculoDaVenda() throws Exception {
        long empresa = empresa();
        long cliente = usuario("CLIENTE");
        long funcionario = funcionarioDa(empresa);
        long carro = veiculo(cliente, "EEE1E11");
        long servico = criar("/empresa/" + empresa + "/servicos", SERVICO);
        long id = criar("/empresa/" + empresa + "/vendas", "{ \"identificacao\": \"VEI-1\", \"clienteId\": " + cliente
                + ", \"funcionarioId\": " + funcionario + ", \"veiculoId\": " + carro + ", \"servicoIds\": [ " + servico + " ] }");

        requisicao.perform(delete("/veiculo/" + carro)).andExpect(status().isConflict());
        enviarPut("/venda/" + id, "{ \"veiculoId\": " + carro + ", \"removerVeiculo\": true }")
                .andExpect(status().isBadRequest());
        enviarPut("/venda/" + id, "{ \"removerVeiculo\": true }")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.veiculoId").doesNotExist())
                .andExpect(jsonPath("$._links.veiculo").doesNotExist());
        requisicao.perform(delete("/veiculo/" + carro)).andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("perfil em uso: nao sai de quem depende dele, e sai quando a dependencia acaba")
    void perfilEmUso() throws Exception {
        long empresa = empresa();
        long cliente = usuario("CLIENTE");
        long funcionario = funcionarioDa(empresa);
        long fornecedor = usuario("FORNECEDOR");
        long mercadoria = criar("/empresa/" + empresa + "/mercadorias", MERCADORIA);
        long id = criar("/empresa/" + empresa + "/vendas", "{ \"identificacao\": \"PERFIL-1\", \"clienteId\": " + cliente
                + ", \"funcionarioId\": " + funcionario + ", " + itens(mercadoria, 1) + " }");
        requisicao.perform(put("/usuario/" + fornecedor + "/mercadorias/" + mercadoria)).andExpect(status().isNoContent());

        enviarPut("/usuario/atualizar", "{ \"id\": " + cliente + ", \"perfis\": [ \"FORNECEDOR\" ] }")
                .andExpect(status().isConflict());
        enviarPut("/usuario/atualizar", "{ \"id\": " + funcionario + ", \"perfis\": [ \"CLIENTE\" ] }")
                .andExpect(status().isConflict());
        enviarPut("/usuario/atualizar", "{ \"id\": " + fornecedor + ", \"perfis\": [ \"CLIENTE\" ] }")
                .andExpect(status().isConflict());
        requisicao.perform(get("/usuario/" + cliente)).andExpect(jsonPath("$.perfis[0]").value("CLIENTE"));

        enviarPut("/usuario/atualizar", "{ \"id\": " + cliente + ", \"perfis\": [ \"CLIENTE\", \"FORNECEDOR\" ] }")
                .andExpect(status().isOk());

        requisicao.perform(delete("/venda/" + id)).andExpect(status().isNoContent());
        enviarPut("/usuario/atualizar", "{ \"id\": " + cliente + ", \"perfis\": [ \"FORNECEDOR\" ] }")
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("dados de demonstracao: o total das vendas soma as rodas e os servicos")
    void totalDaCargaInicial() throws Exception {
        String empresas = requisicao.perform(get("/empresa/empresas"))
                .andReturn().getResponse().getContentAsString();
        int empresa = JsonPath.read(empresas, "$[0].id");

        requisicao.perform(get("/empresa/" + empresa + "/vendas"))
                .andExpect(jsonPath("$[0].valorTotal").value(1300.0))
                .andExpect(jsonPath("$[1].valorTotal").value(1280.0));
    }
}
