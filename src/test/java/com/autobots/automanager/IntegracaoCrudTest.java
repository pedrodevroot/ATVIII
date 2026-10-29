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

import com.autobots.automanager.repositorios.EmpresaRepositorio;
import com.autobots.automanager.repositorios.UsuarioRepositorio;
import com.autobots.automanager.repositorios.VendaRepositorio;
import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@AutoConfigureMockMvc
class IntegracaoCrudTest {

    @Autowired
    private MockMvc requisicao;
    @Autowired
    private UsuarioRepositorio repositorio;
    @Autowired
    private EmpresaRepositorio empresaRepositorio;
    @Autowired
    private VendaRepositorio vendaRepositorio;

    private long cadastrar(String corpo) throws Exception {
        String resposta = requisicao.perform(post("/usuario/cadastro")
                .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(resposta, "$.id")).longValue();
    }

    private long criarFilho(String url, String corpo) throws Exception {
        String resposta = requisicao.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(resposta, "$.id")).longValue();
    }

    @Test
    @DisplayName("ciclo completo do usuario: cadastrar, consultar, atualizar e remover")
    void cicloCompletoDoUsuario() throws Exception {
        long id = cadastrar("""
                {
                  "nome": "Joana Ferreira",
                  "nomeSocial": "Joana",
                  "perfis": [ "CLIENTE" ],
                  "documentos": [ { "tipo": "RG", "numero": "INT-001", "dataEmissao": "2015-06-10" } ],
                  "endereco": {
                    "cidade": "Santos", "rua": "Avenida Ana Costa", "numero": "250",
                    "codigoPostal": "11060002"
                  },
                  "telefones": [ { "ddd": "13", "numero": "981234567" } ],
                  "emails": [ { "endereco": "joana@email.com" } ],
                  "credenciais": [ { "tipo": "USUARIO_SENHA", "nomeUsuario": "int-joana", "senha": "segredo1" } ]
                }
                """);

        requisicao.perform(get("/usuario/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Joana Ferreira"))
                .andExpect(jsonPath("$.perfis[0]").value("CLIENTE"))
                .andExpect(jsonPath("$.endereco.cidade").value("Santos"))
                .andExpect(jsonPath("$.documentos[0].tipo").value("RG"))
                .andExpect(jsonPath("$.telefones[0].ddd").value("13"))
                .andExpect(jsonPath("$.emails[0].endereco").value("joana@email.com"))
                .andExpect(jsonPath("$.credenciais[0].nomeUsuario").value("int-joana"))
                .andExpect(jsonPath("$.credenciais[0].criacao").exists())
                .andExpect(jsonPath("$.credenciais[0].senha").doesNotExist());

        requisicao.perform(put("/usuario/atualizar")
                .contentType(MediaType.APPLICATION_JSON).content("""
                        { "id": %d, "nomeSocial": "Jo", "perfis": [ "CLIENTE", "FORNECEDOR" ],
                          "endereco": { "cidade": "Guaruja" } }
                        """.formatted(id)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nomeSocial").value("Jo"))
                .andExpect(jsonPath("$.nome").value("Joana Ferreira"))
                .andExpect(jsonPath("$.perfis.length()").value(2))
                .andExpect(jsonPath("$.endereco.cidade").value("Guaruja"))
                .andExpect(jsonPath("$.endereco.rua").value("Avenida Ana Costa"));

        requisicao.perform(delete("/usuario/excluir/" + id))
                .andExpect(status().isNoContent());

        requisicao.perform(get("/usuario/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("a dataCadastro e definida pelo servidor, nao pelo cliente da API")
    void dataCadastroVemDoServidor() throws Exception {
        String resposta = requisicao.perform(post("/usuario/cadastro")
                .contentType(MediaType.APPLICATION_JSON).content("""
                        { "nome": "Sem Data", "perfis": [ "CLIENTE" ],
                          "dataCadastro": "1990-01-01T00:00:00.000+00:00" }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.dataCadastro").exists())
                .andReturn().getResponse().getContentAsString();

        String dataCadastro = JsonPath.read(resposta, "$.dataCadastro");
        assertNotEquals("1990-01-01T00:00:00.000+00:00", dataCadastro);
    }

    @Test
    @DisplayName("POST com id cria um usuario novo e nao sobrescreve o existente")
    void postComIdNaoSobrescreve() throws Exception {
        long original = cadastrar("""
                { "nome": "Original", "perfis": [ "CLIENTE" ] }
                """);

        long novo = cadastrar("""
                { "id": %d, "nome": "Tentativa de Sobrescrever", "perfis": [ "CLIENTE" ] }
                """.formatted(original));

        assertNotEquals(original, novo);

        requisicao.perform(get("/usuario/" + original))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Original"));
    }

    @Test
    @DisplayName("409 no documento duplicado, e a transacao nao deixa usuario orfao")
    void documentoDuplicadoFazRollback() throws Exception {
        cadastrar("""
                { "nome": "Dono do Documento", "perfis": [ "CLIENTE" ],
                  "documentos": [ { "tipo": "RG", "numero": "INT-DUP", "dataEmissao": "2015-06-10" } ] }
                """);

        long antes = repositorio.count();

        requisicao.perform(post("/usuario/cadastro")
                .contentType(MediaType.APPLICATION_JSON).content("""
                        { "nome": "Vai Falhar", "perfis": [ "CLIENTE" ],
                          "documentos": [ { "tipo": "RG", "numero": "INT-DUP", "dataEmissao": "2015-06-10" } ] }
                        """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));

        assertEquals(antes, repositorio.count(), "o POST que falhou deixou usuario orfao");
    }

    @Test
    @DisplayName("400 legivel quando o JSON traz um valor de enum que nao existe")
    void enumInvalidoNoJson() throws Exception {
        requisicao.perform(post("/usuario/cadastro")
                .contentType(MediaType.APPLICATION_JSON).content("""
                        { "nome": "Perfil Errado", "perfis": [ "GERENTE" ] }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensagem").value(
                        "O valor GERENTE nao e aceito. Valores aceitos: [CLIENTE, FUNCIONARIO, FORNECEDOR]."));
    }

    @Test
    @DisplayName("ciclo completo do telefone nas rotas aninhadas")
    void cicloCompletoDoTelefone() throws Exception {
        long idUsuario = cadastrar("""
                { "nome": "Dono do Telefone", "perfis": [ "CLIENTE" ] }
                """);

        long idTelefone = criarFilho("/usuario/" + idUsuario + "/telefones", """
                { "ddd": "13", "numero": "981234567" }
                """);

        requisicao.perform(get("/usuario/" + idUsuario + "/telefones"))
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
        long idUsuario = cadastrar("""
                { "nome": "Dono do Documento 2", "perfis": [ "CLIENTE" ] }
                """);

        long idDocumento = criarFilho("/usuario/" + idUsuario + "/documentos", """
                { "tipo": "CNH", "numero": "INT-CNH", "dataEmissao": "2015-06-10" }
                """);

        requisicao.perform(get("/usuario/" + idUsuario + "/documentos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].dataEmissao").exists());

        requisicao.perform(put("/documento/" + idDocumento)
                .contentType(MediaType.APPLICATION_JSON).content("""
                        { "tipo": "PASSAPORTE" }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("PASSAPORTE"))
                .andExpect(jsonPath("$.numero").value("INT-CNH"));

        requisicao.perform(delete("/documento/" + idDocumento))
                .andExpect(status().isNoContent());

        requisicao.perform(get("/documento/" + idDocumento))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("ciclo completo do endereco: um por usuario, o segundo POST responde 409")
    void cicloCompletoDoEndereco() throws Exception {
        long idUsuario = cadastrar("""
                { "nome": "Sem Endereco Ainda", "perfis": [ "CLIENTE" ] }
                """);

        requisicao.perform(get("/usuario/" + idUsuario + "/endereco"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("O usuario de id " + idUsuario + " nao possui endereco"));

        long idEndereco = criarFilho("/usuario/" + idUsuario + "/endereco", """
                { "cidade": "Santos", "rua": "Rua XV", "numero": "100", "codigoPostal": "11010151" }
                """);

        requisicao.perform(post("/usuario/" + idUsuario + "/endereco")
                .contentType(MediaType.APPLICATION_JSON).content("""
                        { "cidade": "Rio de Janeiro", "rua": "Avenida Atlantica", "numero": "1702" }
                        """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem").value("O usuario de id " + idUsuario
                        + " ja possui endereco. Para altera-lo, use PUT /endereco/" + idEndereco));

        requisicao.perform(get("/usuario/" + idUsuario + "/endereco"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value((int) idEndereco))
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

        requisicao.perform(get("/usuario/" + idUsuario))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.endereco").doesNotExist())
                .andExpect(jsonPath("$._links.endereco").doesNotExist());
    }

    @Test
    @DisplayName("ciclo completo do e-mail nas rotas aninhadas")
    void cicloCompletoDoEmail() throws Exception {
        long idUsuario = cadastrar("""
                { "nome": "Dono do Email", "perfis": [ "CLIENTE" ] }
                """);

        long idEmail = criarFilho("/usuario/" + idUsuario + "/emails", """
                { "endereco": "dono@email.com" }
                """);

        requisicao.perform(put("/email/" + idEmail)
                .contentType(MediaType.APPLICATION_JSON).content("""
                        { "endereco": "novo@email.com" }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.endereco").value("novo@email.com"));

        requisicao.perform(get("/usuario/" + idUsuario + "/emails"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].endereco").value("novo@email.com"));

        requisicao.perform(delete("/email/" + idEmail))
                .andExpect(status().isNoContent());

        requisicao.perform(get("/email/" + idEmail))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("ciclo completo da credencial: criar, desativar, trocar a senha e remover")
    void cicloCompletoDaCredencial() throws Exception {
        long idUsuario = cadastrar("""
                { "nome": "Dono da Credencial", "perfis": [ "FUNCIONARIO" ] }
                """);

        long idCredencial = criarFilho("/usuario/" + idUsuario + "/credenciais", """
                { "tipo": "USUARIO_SENHA", "nomeUsuario": "int-credencial", "senha": "segredo1" }
                """);
        criarFilho("/usuario/" + idUsuario + "/credenciais", """
                { "tipo": "CODIGO_BARRA", "codigo": 789123456 }
                """);

        requisicao.perform(get("/usuario/" + idUsuario + "/credenciais"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].tipo").value("USUARIO_SENHA"))
                .andExpect(jsonPath("$[0].inativo").value(false))
                .andExpect(jsonPath("$[1].tipo").value("CODIGO_BARRA"))
                .andExpect(jsonPath("$[1].codigo").value(789123456));

        requisicao.perform(put("/credencial/" + idCredencial)
                .contentType(MediaType.APPLICATION_JSON).content("""
                        { "inativo": true, "senha": "outraSenha" }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inativo").value(true))
                .andExpect(jsonPath("$.senha").doesNotExist());

        requisicao.perform(post("/usuario/" + idUsuario + "/credenciais")
                .contentType(MediaType.APPLICATION_JSON).content("""
                        { "tipo": "USUARIO_SENHA", "nomeUsuario": "int-credencial", "senha": "segredo1" }
                        """))
                .andExpect(status().isConflict());

        requisicao.perform(delete("/credencial/" + idCredencial))
                .andExpect(status().isNoContent());

        requisicao.perform(get("/credencial/" + idCredencial))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("excluir o usuario remove os filhos em cascata")
    void excluirUsuarioRemoveFilhos() throws Exception {
        long idUsuario = cadastrar("""
                { "nome": "Com Filhos", "perfis": [ "CLIENTE" ],
                  "documentos": [ { "tipo": "RG", "numero": "INT-CASCATA", "dataEmissao": "2015-06-10" } ],
                  "telefones": [ { "ddd": "13", "numero": "981234567" } ],
                  "emails": [ { "endereco": "filhos@email.com" } ],
                  "endereco": { "cidade": "Santos", "rua": "Rua XV", "numero": "1" } }
                """);

        String resposta = requisicao.perform(get("/usuario/" + idUsuario))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        long idTelefone = ((Number) JsonPath.read(resposta, "$.telefones[0].id")).longValue();
        long idDocumento = ((Number) JsonPath.read(resposta, "$.documentos[0].id")).longValue();
        long idEmail = ((Number) JsonPath.read(resposta, "$.emails[0].id")).longValue();
        long idEndereco = ((Number) JsonPath.read(resposta, "$.endereco.id")).longValue();

        requisicao.perform(delete("/usuario/excluir/" + idUsuario))
                .andExpect(status().isNoContent());

        requisicao.perform(get("/telefone/" + idTelefone)).andExpect(status().isNotFound());
        requisicao.perform(get("/documento/" + idDocumento)).andExpect(status().isNotFound());
        requisicao.perform(get("/email/" + idEmail)).andExpect(status().isNotFound());
        requisicao.perform(get("/endereco/" + idEndereco)).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("a carga inicial cria a empresa, os 3 perfis de usuario e as 2 vendas")
    void cargaInicialExecutou() throws Exception {
        String resposta = requisicao.perform(get("/usuario/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Pedro Alcantara de Braganca e Bourbon"))
                .andExpect(jsonPath("$[0].perfis[0]").value("FUNCIONARIO"))
                .andExpect(jsonPath("$[1].perfis[0]").value("FORNECEDOR"))
                .andExpect(jsonPath("$[2].perfis[0]").value("CLIENTE"))
                .andExpect(jsonPath("$[2].documentos[0].tipo").value("CPF"))
                .andExpect(jsonPath("$[2].credenciais[0].senha").doesNotExist())
                .andReturn().getResponse().getContentAsString();

        assertEquals("dompedrocliente", JsonPath.read(resposta, "$[2].credenciais[0].nomeUsuario"));
        assertEquals(1, empresaRepositorio.count());
        assertEquals(2, vendaRepositorio.count());
    }

    @Test
    @DisplayName("o usuario que aparece em vendas nao pode ser excluido (409)")
    void excluirUsuarioComVendas() throws Exception {
        String resposta = requisicao.perform(get("/usuario/usuarios"))
                .andReturn().getResponse().getContentAsString();
        List<Integer> funcionarios = JsonPath.read(resposta, "$[?(@.nomeSocial == 'Dom Pedro')].id");
        int idFuncionario = funcionarios.get(0);

        requisicao.perform(delete("/usuario/excluir/" + idFuncionario))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem").value("O usuario de id " + idFuncionario
                        + " possui vendas registradas e nao pode ser excluido"));

        requisicao.perform(get("/usuario/" + idFuncionario))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("a listagem de usuarios vem ordenada por id")
    void listagemOrdenadaPorId() throws Exception {
        cadastrar("""
                { "nome": "Ordem A", "perfis": [ "CLIENTE" ] }
                """);
        cadastrar("""
                { "nome": "Ordem B", "perfis": [ "CLIENTE" ] }
                """);

        String resposta = requisicao.perform(get("/usuario/usuarios"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<Integer> ids = JsonPath.read(resposta, "$[*].id");
        List<Integer> ordenados = new ArrayList<>(ids);
        Collections.sort(ordenados);
        assertEquals(ordenados, ids, "a listagem nao veio ordenada por id");
    }

    @Test
    @DisplayName("HATEOAS: do usuario se chega ao telefone seguindo apenas os links")
    void navegacaoPelosLinks() throws Exception {
        long idUsuario = cadastrar("""
                { "nome": "Navegador", "perfis": [ "CLIENTE" ],
                  "telefones": [ { "ddd": "13", "numero": "981234567" } ] }
                """);

        String usuario = requisicao.perform(get("/usuario/" + idUsuario))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String linkTelefones = JsonPath.read(usuario, "$._links.telefones.href");

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
