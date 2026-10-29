package com.autobots.automanager.controles;

import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
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
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.autobots.automanager.conversores.CredencialConversor;
import com.autobots.automanager.dtos.CredencialAtualizacaoDto;
import com.autobots.automanager.entidades.Credencial;
import com.autobots.automanager.entidades.CredencialCodigoBarra;
import com.autobots.automanager.entidades.CredencialUsuarioSenha;
import com.autobots.automanager.excecoes.RecursoNaoEncontradoException;
import com.autobots.automanager.modelo.AdicionadorLinkCredencial;
import com.autobots.automanager.servicos.CredencialServico;

@WebMvcTest(CredencialControle.class)
@Import({ CredencialConversor.class, AdicionadorLinkCredencial.class })
class CredencialControleTest {

    @Autowired
    private MockMvc requisicao;

    @MockBean
    private CredencialServico servico;

    private CredencialUsuarioSenha usuarioSenha(Long id, String nomeUsuario) {
        CredencialUsuarioSenha credencial = new CredencialUsuarioSenha();
        credencial.setId(id);
        credencial.setNomeUsuario(nomeUsuario);
        credencial.setSenha("segredo1");
        credencial.setCriacao(Calendar.getInstance().getTime());
        return credencial;
    }

    private CredencialCodigoBarra codigoBarra(Long id, long codigo) {
        CredencialCodigoBarra credencial = new CredencialCodigoBarra();
        credencial.setId(id);
        credencial.setCodigo(codigo);
        credencial.setCriacao(Calendar.getInstance().getTime());
        return credencial;
    }

    @Test
    @DisplayName("GET /usuario/{id}/credenciais: 200 com os dois tipos, cada um com seus campos")
    void listarOk() throws Exception {
        when(servico.listarPorUsuario(1L))
                .thenReturn(List.of(usuarioSenha(1L, "maria"), codigoBarra(2L, 789123456L)));

        requisicao.perform(get("/usuario/1/credenciais"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].tipo").value("USUARIO_SENHA"))
                .andExpect(jsonPath("$[0].nomeUsuario").value("maria"))
                .andExpect(jsonPath("$[0].codigo").doesNotExist())
                .andExpect(jsonPath("$[1].tipo").value("CODIGO_BARRA"))
                .andExpect(jsonPath("$[1].codigo").value(789123456))
                .andExpect(jsonPath("$[1].nomeUsuario").doesNotExist())
                .andExpect(jsonPath("$[0].links[0].href").value("http://localhost/credencial/1"));
    }

    @Test
    @DisplayName("GET /credencial/{id}: a senha nunca aparece na resposta")
    void obterPorIdSemSenha() throws Exception {
        when(servico.obterPorId(1L)).thenReturn(usuarioSenha(1L, "maria"));

        requisicao.perform(get("/credencial/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nomeUsuario").value("maria"))
                .andExpect(jsonPath("$.inativo").value(false))
                .andExpect(jsonPath("$.senha").doesNotExist())
                .andExpect(jsonPath("$._links.self.href").value("http://localhost/credencial/1"));
    }

    @Test
    @DisplayName("POST /usuario/{id}/credenciais: 201 criando credencial de usuario e senha")
    void criarUsuarioSenha() throws Exception {
        when(servico.criar(anyLong(), any(Credencial.class))).thenReturn(usuarioSenha(5L, "maria"));

        String corpo = """
                { "tipo": "USUARIO_SENHA", "nomeUsuario": "maria", "senha": "segredo1" }
                """;

        requisicao.perform(post("/usuario/1/credenciais")
                .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/credencial/5"))
                .andExpect(jsonPath("$.tipo").value("USUARIO_SENHA"))
                .andExpect(jsonPath("$.senha").doesNotExist());

        ArgumentCaptor<Credencial> capturada = ArgumentCaptor.forClass(Credencial.class);
        verify(servico).criar(eq(1L), capturada.capture());
        CredencialUsuarioSenha recebida = assertInstanceOf(CredencialUsuarioSenha.class, capturada.getValue());
        assertEquals("segredo1", recebida.getSenha());
    }

    @Test
    @DisplayName("POST /usuario/{id}/credenciais: 201 criando credencial de codigo de barras")
    void criarCodigoBarra() throws Exception {
        when(servico.criar(anyLong(), any(Credencial.class))).thenReturn(codigoBarra(6L, 789123456L));

        String corpo = """
                { "tipo": "CODIGO_BARRA", "codigo": 789123456 }
                """;

        requisicao.perform(post("/usuario/1/credenciais")
                .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("CODIGO_BARRA"))
                .andExpect(jsonPath("$.codigo").value(789123456));

        ArgumentCaptor<Credencial> capturada = ArgumentCaptor.forClass(Credencial.class);
        verify(servico).criar(eq(1L), capturada.capture());
        assertInstanceOf(CredencialCodigoBarra.class, capturada.getValue());
    }

    @Test
    @DisplayName("POST /usuario/{id}/credenciais: 400 quando o tipo nao e informado")
    void criarSemTipo() throws Exception {
        String corpo = """
                { "nomeUsuario": "maria", "senha": "segredo1" }
                """;

        requisicao.perform(post("/usuario/1/credenciais")
                .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("O campo tipo esta ausente ou tem um valor nao aceito."));

        verifyNoInteractions(servico);
    }

    @Test
    @DisplayName("POST /usuario/{id}/credenciais: 400 quando o tipo nao existe")
    void criarTipoInexistente() throws Exception {
        String corpo = """
                { "tipo": "BIOMETRIA" }
                """;

        requisicao.perform(post("/usuario/1/credenciais")
                .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("O campo tipo esta ausente ou tem um valor nao aceito."));

        verifyNoInteractions(servico);
    }

    @Test
    @DisplayName("POST /usuario/{id}/credenciais: 400 quando a senha tem menos de 6 caracteres")
    void criarSenhaCurta() throws Exception {
        String corpo = """
                { "tipo": "USUARIO_SENHA", "nomeUsuario": "maria", "senha": "123" }
                """;

        requisicao.perform(post("/usuario/1/credenciais")
                .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros[*].campo", hasItem("senha")));

        verifyNoInteractions(servico);
    }

    @Test
    @DisplayName("POST /usuario/{id}/credenciais: 400 quando o codigo de barras falta")
    void criarCodigoBarraSemCodigo() throws Exception {
        String corpo = """
                { "tipo": "CODIGO_BARRA" }
                """;

        requisicao.perform(post("/usuario/1/credenciais")
                .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros[*].campo", hasItem("codigo")));

        verifyNoInteractions(servico);
    }

    @Test
    @DisplayName("POST /usuario/{id}/credenciais: 409 quando o nome de usuario ja existe")
    void criarNomeUsuarioDuplicado() throws Exception {
        when(servico.criar(anyLong(), any(Credencial.class)))
                .thenThrow(new DataIntegrityViolationException("unique violation"));

        String corpo = """
                { "tipo": "USUARIO_SENHA", "nomeUsuario": "maria", "senha": "segredo1" }
                """;

        requisicao.perform(post("/usuario/1/credenciais")
                .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("PUT /credencial/{id}: 200 repassando o DTO ao servico")
    void atualizarOk() throws Exception {
        CredencialUsuarioSenha desativada = usuarioSenha(1L, "maria");
        desativada.setInativo(true);
        when(servico.atualizar(anyLong(), any(CredencialAtualizacaoDto.class))).thenReturn(desativada);

        String corpo = """
                { "inativo": true }
                """;

        requisicao.perform(put("/credencial/1")
                .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inativo").value(true))
                .andExpect(jsonPath("$._links.self.href").value("http://localhost/credencial/1"));
    }

    @Test
    @DisplayName("PUT /credencial/{id}: 400 quando a nova senha e curta")
    void atualizarSenhaCurta() throws Exception {
        String corpo = """
                { "senha": "123" }
                """;

        requisicao.perform(put("/credencial/1")
                .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros[*].campo", hasItem("senha")));

        verifyNoInteractions(servico);
    }

    @Test
    @DisplayName("GET /credencial/{id}: 404 quando nao existe")
    void obterPorIdInexistente() throws Exception {
        when(servico.obterPorId(999L)).thenThrow(new RecursoNaoEncontradoException("Credencial", 999L));

        requisicao.perform(get("/credencial/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Credencial de id 999 nao foi encontrado"));
    }

    @Test
    @DisplayName("DELETE /credencial/{id}: 204 sem corpo")
    void excluirOk() throws Exception {
        requisicao.perform(delete("/credencial/2"))
                .andExpect(status().isNoContent());

        verify(servico).excluir(2L);
    }

    @Test
    @DisplayName("DELETE /credencial/{id}: 404 quando nao existe")
    void excluirInexistente() throws Exception {
        doThrow(new RecursoNaoEncontradoException("Credencial", 999L)).when(servico).excluir(999L);

        requisicao.perform(delete("/credencial/999"))
                .andExpect(status().isNotFound());
    }
}
