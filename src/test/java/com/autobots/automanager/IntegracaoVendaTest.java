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

    private String venda(String identificacao, long cliente, long funcionario, Long veiculo, long mercadoria,
            long servico) {
        return "{ \"identificacao\": \"" + identificacao + "\", \"clienteId\": " + cliente
                + ", \"funcionarioId\": " + funcionario
                + (veiculo == null ? "" : ", \"veiculoId\": " + veiculo)
                + ", \"mercadoriaIds\": [ " + mercadoria + " ], \"servicoIds\": [ " + servico + " ] }";
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
                .andExpect(jsonPath("$.mercadorias[0].nome").value("Pneu"))
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

        enviarPut("/venda/" + id, "{ \"identificacao\": \"CICLO-2\", \"mercadoriaIds\": [] }")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.identificacao").value("CICLO-2"))
                .andExpect(jsonPath("$.mercadorias.length()").value(0))
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
                + funcionario + ", \"mercadoriaIds\": [ " + mercadoria + ", " + mercadoria + " ] }")
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
        enviarPut("/venda/" + id, "{ \"mercadoriaIds\": [ " + mercadoriaDeOutra + " ] }")
                .andExpect(status().isBadRequest());
        enviarPut("/venda/999999", "{ \"identificacao\": \"X\" }")
                .andExpect(status().isNotFound());
        requisicao.perform(get("/venda/" + id))
                .andExpect(jsonPath("$.clienteId").value(cliente))
                .andExpect(jsonPath("$.mercadorias[0].id").value(mercadoria));
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
                .andExpect(jsonPath("$.mercadorias[0]._links.self.href").exists())
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
                + ", \"funcionarioId\": " + funcionario + ", \"mercadoriaIds\": null, \"servicoIds\": [ " + servico + " ] }");
        enviarPost(url, "{ \"identificacao\": \"NULA-2\", \"clienteId\": " + cliente
                + ", \"funcionarioId\": " + funcionario + ", \"mercadoriaIds\": null, \"servicoIds\": null }")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("A venda deve ter ao menos uma mercadoria ou um servico"));

        enviarPut("/venda/" + id, "{ \"mercadoriaIds\": null, \"servicoIds\": null }")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.servicos.length()").value(1));
    }
}
