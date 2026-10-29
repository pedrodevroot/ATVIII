package com.autobots.automanager.servicos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import com.autobots.automanager.entidades.CredencialUsuarioSenha;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.excecoes.ConflitoException;
import com.autobots.automanager.excecoes.RecursoNaoEncontradoException;
import com.autobots.automanager.modelo.UsuarioAtualizador;
import com.autobots.automanager.repositorios.UsuarioRepositorio;
import com.autobots.automanager.repositorios.VendaRepositorio;

@ExtendWith(MockitoExtension.class)
class UsuarioServicoTest {

    @Mock
    private UsuarioRepositorio repositorio;
    @Mock
    private VendaRepositorio vendaRepositorio;
    @Mock
    private UsuarioAtualizador atualizador;

    @InjectMocks
    private UsuarioServico servico;

    private Usuario usuario(Long id, String nome) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setNome(nome);
        return usuario;
    }

    @Test
    @DisplayName("cadastrar: o servidor define a dataCadastro")
    void cadastrarDefineDataCadastro() {
        when(repositorio.save(any(Usuario.class))).thenAnswer(chamada -> chamada.getArgument(0));

        Usuario salvo = servico.cadastrar(usuario(null, "Maria"));

        assertNotNull(salvo.getDataCadastro());
    }

    @Test
    @DisplayName("cadastrar: o servidor define a data de criacao de cada credencial")
    void cadastrarDefineCriacaoDaCredencial() {
        when(repositorio.save(any(Usuario.class))).thenAnswer(chamada -> chamada.getArgument(0));
        Usuario entrada = usuario(null, "Maria");
        CredencialUsuarioSenha credencial = new CredencialUsuarioSenha();
        credencial.setNomeUsuario("maria");
        credencial.setSenha("segredo1");
        entrada.getCredenciais().add(credencial);

        Usuario salvo = servico.cadastrar(entrada);

        assertNotNull(salvo.getCredenciais().get(0).getCriacao());
        assertEquals(salvo.getDataCadastro(), salvo.getCredenciais().get(0).getCriacao());
    }

    @Test
    @DisplayName("cadastrar: zera o id recebido, para que save() insira em vez de atualizar")
    void cadastrarZeraId() {
        when(repositorio.save(any(Usuario.class))).thenAnswer(chamada -> chamada.getArgument(0));

        servico.cadastrar(usuario(1L, "Tentativa de sobrescrever"));

        ArgumentCaptor<Usuario> capturado = ArgumentCaptor.forClass(Usuario.class);
        verify(repositorio).save(capturado.capture());
        assertNull(capturado.getValue().getId());
    }

    @Test
    @DisplayName("listar: delega ao repositorio ordenando por id")
    void listarOrdenaPorId() {
        List<Usuario> esperado = List.of(usuario(1L, "Maria"), usuario(2L, "Joao"));
        when(repositorio.findAll(Sort.by("id"))).thenReturn(esperado);

        assertEquals(esperado, servico.listar());

        verify(repositorio).findAll(Sort.by("id"));
        verify(repositorio, never()).findAll();
    }

    @Test
    @DisplayName("obterPorId: busca no banco por id, sem carregar a tabela inteira")
    void obterPorIdBuscaPorId() {
        Usuario esperado = usuario(1L, "Maria");
        when(repositorio.findById(1L)).thenReturn(Optional.of(esperado));

        assertSame(esperado, servico.obterPorId(1L));
        verify(repositorio, never()).findAll();
        verify(repositorio, never()).findAll(Sort.by("id"));
    }

    @Test
    @DisplayName("obterPorId: lanca RecursoNaoEncontrado quando o id nao existe")
    void obterPorIdInexistente() {
        when(repositorio.findById(999L)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException excecao = assertThrows(
                RecursoNaoEncontradoException.class, () -> servico.obterPorId(999L));

        assertEquals("Usuario de id 999 nao foi encontrado", excecao.getMessage());
    }

    @Test
    @DisplayName("atualizar: delega ao UsuarioAtualizador e salva")
    void atualizarDelegaAoAtualizador() {
        Usuario existente = usuario(1L, "Maria");
        Usuario atualizacao = usuario(1L, "Maria Silva");
        when(repositorio.findById(1L)).thenReturn(Optional.of(existente));
        when(repositorio.save(existente)).thenReturn(existente);

        servico.atualizar(atualizacao);

        verify(atualizador).atualizar(existente, atualizacao);
        verify(repositorio).save(existente);
    }

    @Test
    @DisplayName("atualizar: lanca IllegalArgument quando o id nao e informado")
    void atualizarSemId() {
        IllegalArgumentException excecao = assertThrows(
                IllegalArgumentException.class, () -> servico.atualizar(usuario(null, "Maria")));

        assertEquals("O id do usuario e obrigatorio para atualizacao", excecao.getMessage());
        verify(repositorio, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("atualizar: lanca RecursoNaoEncontrado quando o id nao existe")
    void atualizarIdInexistente() {
        when(repositorio.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class,
                () -> servico.atualizar(usuario(999L, "Fantasma")));

        verify(repositorio, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("excluir: remove o usuario encontrado que nao tem vendas")
    void excluirUsuarioExistente() {
        Usuario existente = usuario(1L, "Maria");
        when(repositorio.findById(1L)).thenReturn(Optional.of(existente));
        when(vendaRepositorio.existsByClienteIdOrFuncionarioId(1L, 1L)).thenReturn(false);

        servico.excluir(1L);

        verify(repositorio).delete(existente);
    }

    @Test
    @DisplayName("excluir: lanca Conflito quando o usuario aparece em alguma venda")
    void excluirUsuarioComVendas() {
        when(repositorio.findById(1L)).thenReturn(Optional.of(usuario(1L, "Maria")));
        when(vendaRepositorio.existsByClienteIdOrFuncionarioId(1L, 1L)).thenReturn(true);

        ConflitoException excecao = assertThrows(ConflitoException.class, () -> servico.excluir(1L));

        assertEquals("O usuario de id 1 possui vendas registradas e nao pode ser excluido",
                excecao.getMessage());
        verify(repositorio, never()).delete(any(Usuario.class));
    }

    @Test
    @DisplayName("excluir: lanca RecursoNaoEncontrado quando o id nao existe")
    void excluirIdInexistente() {
        when(repositorio.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> servico.excluir(999L));

        verify(repositorio, never()).delete(any(Usuario.class));
    }

    @Test
    @DisplayName("excluir: nao usa deleteById, para poder validar a existencia antes")
    void excluirNaoUsaDeleteById() {
        when(repositorio.findById(1L)).thenReturn(Optional.of(usuario(1L, "Maria")));

        servico.excluir(1L);

        verify(repositorio, never()).deleteById(anyLong());
    }
}
