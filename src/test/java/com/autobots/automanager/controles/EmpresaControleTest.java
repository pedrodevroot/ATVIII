package com.autobots.automanager.controles;

import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.any;
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

import java.util.Calendar;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.autobots.automanager.conversores.EmpresaConversor;
import com.autobots.automanager.conversores.UsuarioConversor;
import com.autobots.automanager.entidades.Empresa;
import com.autobots.automanager.entidades.Endereco;
import com.autobots.automanager.entidades.Telefone;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.enumeracoes.PerfilUsuario;
import com.autobots.automanager.excecoes.ConflitoException;
import com.autobots.automanager.excecoes.RecursoNaoEncontradoException;
import com.autobots.automanager.modelo.AdicionadorLinkEmpresa;
import com.autobots.automanager.modelo.AdicionadorLinkUsuario;
import com.autobots.automanager.servicos.EmpresaServico;

@WebMvcTest(EmpresaControle.class)
@Import({ EmpresaConversor.class, UsuarioConversor.class, AdicionadorLinkEmpresa.class,
        AdicionadorLinkUsuario.class })
class EmpresaControleTest {

    @Autowired
    private MockMvc requisicao;

    @MockBean
    private EmpresaServico servico;

    private Empresa empresa(Long id, String razaoSocial) {
        Empresa empresa = new Empresa();
        empresa.setId(id);
        empresa.setRazaoSocial(razaoSocial);
        empresa.setNomeFantasia("Oficina");
        empresa.setCadastro(Calendar.getInstance().getTime());
        Telefone telefone = new Telefone();
        telefone.setId(1L);
        telefone.setDdd("11");
        telefone.setNumero("986454527");
        empresa.getTelefones().add(telefone);
        return empresa;
    }

    private Usuario usuario(Long id, Empresa empresa) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setNome("Maria");
        usuario.getPerfis().add(PerfilUsuario.CLIENTE);
        usuario.setEmpresa(empresa);
        return usuario;
    }

    @Test
    @DisplayName("GET /empresa/{id}: 200 com a empresa, sem expor as colecoes do negocio")
    void obterPorIdOk() throws Exception {
        when(servico.obterPorId(1L)).thenReturn(empresa(1L, "Car Service Ltda"));

        requisicao.perform(get("/empresa/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.razaoSocial").value("Car Service Ltda"))
                .andExpect(jsonPath("$.cadastro").exists())
                .andExpect(jsonPath("$.telefones[0].ddd").value("11"))
                .andExpect(jsonPath("$.usuarios").doesNotExist())
                .andExpect(jsonPath("$.vendas").doesNotExist());
    }

    @Test
    @DisplayName("GET /empresa/{id}: 404 com corpo de erro e sem links")
    void obterPorIdInexistente() throws Exception {
        when(servico.obterPorId(999L)).thenThrow(new RecursoNaoEncontradoException("Empresa", 999L));

        requisicao.perform(get("/empresa/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Empresa de id 999 nao foi encontrado"))
                .andExpect(jsonPath("$._links").doesNotExist());
    }

    @Test
    @DisplayName("GET /empresa/{id}: links self, empresas, usuarios e telefones")
    void obterPorIdComLinks() throws Exception {
        when(servico.obterPorId(1L)).thenReturn(empresa(1L, "Car Service Ltda"));

        requisicao.perform(get("/empresa/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links.self.href").value("http://localhost/empresa/1"))
                .andExpect(jsonPath("$._links.empresas.href").value("http://localhost/empresa/empresas"))
                .andExpect(jsonPath("$._links.usuarios.href").value("http://localhost/empresa/1/usuarios"))
                .andExpect(jsonPath("$._links.telefones.href").value("http://localhost/empresa/1/telefones"))
                .andExpect(jsonPath("$._links.endereco").doesNotExist());
    }

    @Test
    @DisplayName("GET /empresa/{id}: com link de endereco quando a empresa tem endereco")
    void obterPorIdComLinkDeEndereco() throws Exception {
        Empresa empresa = empresa(1L, "Car Service Ltda");
        Endereco endereco = new Endereco();
        endereco.setId(3L);
        endereco.setCidade("Sao Paulo");
        empresa.setEndereco(endereco);
        when(servico.obterPorId(1L)).thenReturn(empresa);

        requisicao.perform(get("/empresa/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.endereco.cidade").value("Sao Paulo"))
                .andExpect(jsonPath("$._links.endereco.href").value("http://localhost/empresa/1/endereco"));
    }

    @Test
    @DisplayName("GET /empresa/empresas: 200 com a lista e link self em cada empresa")
    void listarOk() throws Exception {
        when(servico.listar()).thenReturn(List.of(empresa(1L, "Primeira"), empresa(2L, "Segunda")));

        requisicao.perform(get("/empresa/empresas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].razaoSocial").value("Primeira"))
                .andExpect(jsonPath("$[0].links.length()").value(1))
                .andExpect(jsonPath("$[1].links[0].href").value("http://localhost/empresa/2"));
    }

    @Test
    @DisplayName("POST /empresa/cadastro: 201 com header Location e link self")
    void cadastrarOk() throws Exception {
        when(servico.cadastrar(any(Empresa.class))).thenReturn(empresa(2L, "Nova Ltda"));

        String corpo = """
                { "razaoSocial": "Nova Ltda" }
                """;

        requisicao.perform(post("/empresa/cadastro")
                .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/empresa/2"))
                .andExpect(jsonPath("$._links.self.href").value("http://localhost/empresa/2"));
    }

    @Test
    @DisplayName("POST /empresa/cadastro: 400 quando a razao social nao e informada")
    void cadastrarSemRazaoSocial() throws Exception {
        String corpo = """
                { "nomeFantasia": "So fantasia" }
                """;

        requisicao.perform(post("/empresa/cadastro")
                .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros[*].campo", hasItem("razaoSocial")));

        verifyNoInteractions(servico);
    }

    @Test
    @DisplayName("POST /empresa/cadastro: 400 com caminho indexado nos filhos invalidos")
    void cadastrarFilhosInvalidos() throws Exception {
        String corpo = """
                {
                  "razaoSocial": "Invalida",
                  "telefones": [ { "ddd": "1", "numero": "abc" } ],
                  "endereco": { "bairro": "Centro" }
                }
                """;

        requisicao.perform(post("/empresa/cadastro")
                .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros[*].campo", hasItem("telefones[0].ddd")))
                .andExpect(jsonPath("$.erros[*].campo", hasItem("endereco.cidade")));

        verifyNoInteractions(servico);
    }

    @Test
    @DisplayName("PUT /empresa/atualizar: 200 com a empresa atualizada")
    void atualizarOk() throws Exception {
        when(servico.atualizar(any(Empresa.class))).thenReturn(empresa(1L, "Nome Novo"));

        String corpo = """
                { "id": 1, "razaoSocial": "Nome Novo" }
                """;

        requisicao.perform(put("/empresa/atualizar")
                .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.razaoSocial").value("Nome Novo"))
                .andExpect(jsonPath("$._links.self.href").value("http://localhost/empresa/1"));
    }

    @Test
    @DisplayName("PUT /empresa/atualizar: 400 quando o id nao e informado")
    void atualizarSemId() throws Exception {
        String corpo = """
                { "razaoSocial": "Sem Id" }
                """;

        requisicao.perform(put("/empresa/atualizar")
                .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros[*].campo", hasItem("id")));

        verifyNoInteractions(servico);
    }

    @Test
    @DisplayName("PUT /empresa/atualizar: 404 quando a empresa nao existe")
    void atualizarInexistente() throws Exception {
        when(servico.atualizar(any(Empresa.class)))
                .thenThrow(new RecursoNaoEncontradoException("Empresa", 999L));

        String corpo = """
                { "id": 999, "razaoSocial": "Fantasma" }
                """;

        requisicao.perform(put("/empresa/atualizar")
                .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /empresa/excluir/{id}: 204 sem corpo")
    void excluirOk() throws Exception {
        requisicao.perform(delete("/empresa/excluir/1"))
                .andExpect(status().isNoContent());

        verify(servico).excluir(1L);
    }

    @Test
    @DisplayName("DELETE /empresa/excluir/{id}: 404 quando a empresa nao existe")
    void excluirInexistente() throws Exception {
        doThrow(new RecursoNaoEncontradoException("Empresa", 999L)).when(servico).excluir(999L);

        requisicao.perform(delete("/empresa/excluir/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /empresa/{id}/usuarios: 200 com os usuarios, o empresaId e o link self")
    void listarUsuariosOk() throws Exception {
        when(servico.listarUsuarios(1L)).thenReturn(List.of(usuario(5L, empresa(1L, "Car Service Ltda"))));

        requisicao.perform(get("/empresa/1/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(5))
                .andExpect(jsonPath("$[0].empresaId").value(1))
                .andExpect(jsonPath("$[0].links[0].href").value("http://localhost/usuario/5"));
    }

    @Test
    @DisplayName("GET /empresa/{id}/usuarios: 404 quando a empresa nao existe")
    void listarUsuariosEmpresaInexistente() throws Exception {
        when(servico.listarUsuarios(999L)).thenThrow(new RecursoNaoEncontradoException("Empresa", 999L));

        requisicao.perform(get("/empresa/999/usuarios"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /empresa/{id}/usuarios/{usuarioId}: 204 ao vincular")
    void vincularOk() throws Exception {
        requisicao.perform(put("/empresa/1/usuarios/5"))
                .andExpect(status().isNoContent());

        verify(servico).vincularUsuario(1L, 5L);
    }

    @Test
    @DisplayName("PUT /empresa/{id}/usuarios/{usuarioId}: 409 quando o usuario e de outra empresa")
    void vincularUsuarioDeOutraEmpresa() throws Exception {
        String mensagem = "O usuario de id 5 ja esta vinculado a empresa de id 2. "
                + "Desvincule-o antes de vincular a outra empresa";
        doThrow(new ConflitoException(mensagem)).when(servico).vincularUsuario(1L, 5L);

        requisicao.perform(put("/empresa/1/usuarios/5"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem").value(mensagem));
    }

    @Test
    @DisplayName("PUT /empresa/{id}/usuarios/{usuarioId}: 404 quando o usuario nao existe")
    void vincularUsuarioInexistente() throws Exception {
        doThrow(new RecursoNaoEncontradoException("Usuario", 999L)).when(servico).vincularUsuario(1L, 999L);

        requisicao.perform(put("/empresa/1/usuarios/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /empresa/{id}/usuarios/{usuarioId}: 204 ao desvincular")
    void desvincularOk() throws Exception {
        requisicao.perform(delete("/empresa/1/usuarios/5"))
                .andExpect(status().isNoContent());

        verify(servico).desvincularUsuario(1L, 5L);
    }

    @Test
    @DisplayName("DELETE /empresa/{id}/usuarios/{usuarioId}: 404 quando o usuario nao e da empresa")
    void desvincularUsuarioDeOutraEmpresa() throws Exception {
        String mensagem = "O usuario de id 5 nao esta vinculado a empresa de id 1";
        doThrow(new RecursoNaoEncontradoException(mensagem)).when(servico).desvincularUsuario(1L, 5L);

        requisicao.perform(delete("/empresa/1/usuarios/5"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value(mensagem));
    }
}
