package com.autobots.automanager.servicos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.entidades.Telefone;
import com.autobots.automanager.excecoes.RecursoNaoEncontradoException;
import com.autobots.automanager.modelo.TelefoneAtualizador;
import com.autobots.automanager.repositorios.UsuarioRepositorio;
import com.autobots.automanager.repositorios.TelefoneRepositorio;

@ExtendWith(MockitoExtension.class)
class TelefoneServicoTest {

    @Mock
    private TelefoneRepositorio repositorio;
    @Mock
    private UsuarioRepositorio usuarioRepositorio;
    @Mock
    private TelefoneAtualizador atualizador;

    @InjectMocks
    private TelefoneServico servico;

    private Telefone telefone(Long id, String ddd, String numero) {
        Telefone telefone = new Telefone();
        telefone.setId(id);
        telefone.setDdd(ddd);
        telefone.setNumero(numero);
        return telefone;
    }

    private Usuario usuarioComTelefone(Telefone telefone) {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("Maria");
        if (telefone != null) {
            usuario.getTelefones().add(telefone);
        }
        return usuario;
    }

    @Test
    @DisplayName("listarPorUsuario: devolve os telefones do usuario")
    void listarPorUsuario() {
        Telefone existente = telefone(1L, "21", "981234576");
        when(usuarioRepositorio.findById(1L)).thenReturn(Optional.of(usuarioComTelefone(existente)));

        assertEquals(1, servico.listarPorUsuario(1L).size());
        assertSame(existente, servico.listarPorUsuario(1L).get(0));
    }

    @Test
    @DisplayName("listarPorUsuario: lanca RecursoNaoEncontrado quando o usuario nao existe")
    void listarPorUsuarioInexistente() {
        when(usuarioRepositorio.findById(999L)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException excecao = assertThrows(
                RecursoNaoEncontradoException.class, () -> servico.listarPorUsuario(999L));

        assertEquals("Usuario de id 999 nao foi encontrado", excecao.getMessage());
    }

    @Test
    @DisplayName("criar: persiste o telefone e o vincula ao usuario")
    void criarVinculaAoUsuario() {
        Usuario usuario = usuarioComTelefone(null);
        when(usuarioRepositorio.findById(1L)).thenReturn(Optional.of(usuario));
        when(repositorio.save(any(Telefone.class))).thenAnswer(chamada -> {
            Telefone argumento = chamada.getArgument(0);
            argumento.setId(99L);
            return argumento;
        });

        Telefone criado = servico.criar(1L, telefone(null, "13", "981234567"));

        assertEquals(99L, criado.getId());
        assertEquals(1, usuario.getTelefones().size());
        assertSame(criado, usuario.getTelefones().get(0));
        verify(usuarioRepositorio).save(usuario);
    }

    @Test
    @DisplayName("criar: zera um id enviado, para que o telefone seja sempre novo")
    void criarZeraId() {
        Usuario usuario = usuarioComTelefone(null);
        when(usuarioRepositorio.findById(1L)).thenReturn(Optional.of(usuario));
        when(repositorio.save(any(Telefone.class))).thenAnswer(chamada -> {
            Telefone argumento = chamada.getArgument(0);
            assertEquals(null, argumento.getId());
            argumento.setId(99L);
            return argumento;
        });

        Telefone criado = servico.criar(1L, telefone(7L, "13", "981234567"));

        assertEquals(99L, criado.getId());
    }

    @Test
    @DisplayName("criar: lanca RecursoNaoEncontrado quando o usuario nao existe")
    void criarUsuarioInexistente() {
        when(usuarioRepositorio.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class,
                () -> servico.criar(999L, telefone(null, "13", "981234567")));

        verify(repositorio, never()).save(any(Telefone.class));
    }

    @Test
    @DisplayName("obterPorId: devolve o telefone encontrado")
    void obterPorId() {
        Telefone existente = telefone(2L, "21", "981234576");
        when(repositorio.findById(2L)).thenReturn(Optional.of(existente));

        assertSame(existente, servico.obterPorId(2L));
    }

    @Test
    @DisplayName("obterPorId: lanca RecursoNaoEncontrado quando o id nao existe")
    void obterPorIdInexistente() {
        when(repositorio.findById(999L)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException excecao = assertThrows(
                RecursoNaoEncontradoException.class, () -> servico.obterPorId(999L));

        assertEquals("Telefone de id 999 nao foi encontrado", excecao.getMessage());
    }

    @Test
    @DisplayName("atualizar: delega ao TelefoneAtualizador e salva")
    void atualizarDelegaAoAtualizador() {
        Telefone existente = telefone(2L, "21", "981234576");
        Telefone atualizacao = telefone(null, "11", null);
        when(repositorio.findById(2L)).thenReturn(Optional.of(existente));
        when(repositorio.save(existente)).thenReturn(existente);

        servico.atualizar(2L, atualizacao);

        verify(atualizador).atualizar(existente, atualizacao);
        verify(repositorio).save(existente);
    }

    @Test
    @DisplayName("atualizar: lanca RecursoNaoEncontrado quando o id nao existe")
    void atualizarIdInexistente() {
        when(repositorio.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class,
                () -> servico.atualizar(999L, telefone(null, "11", null)));

        verify(repositorio, never()).save(any(Telefone.class));
    }

    @Test
    @DisplayName("excluir: remove o telefone encontrado")
    void excluirExistente() {
        Telefone existente = telefone(2L, "21", "981234576");
        when(repositorio.findById(2L)).thenReturn(Optional.of(existente));

        servico.excluir(2L);

        verify(repositorio).delete(existente);
    }

    @Test
    @DisplayName("excluir: lanca RecursoNaoEncontrado quando o id nao existe")
    void excluirIdInexistente() {
        when(repositorio.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> servico.excluir(999L));

        verify(repositorio, never()).delete(any(Telefone.class));
    }
}
