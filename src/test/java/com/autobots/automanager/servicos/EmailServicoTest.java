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

import com.autobots.automanager.entidades.Email;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.excecoes.RecursoNaoEncontradoException;
import com.autobots.automanager.modelo.EmailAtualizador;
import com.autobots.automanager.repositorios.EmailRepositorio;
import com.autobots.automanager.repositorios.UsuarioRepositorio;

@ExtendWith(MockitoExtension.class)
class EmailServicoTest {

    @Mock
    private EmailRepositorio repositorio;
    @Mock
    private UsuarioRepositorio usuarioRepositorio;
    @Mock
    private EmailAtualizador atualizador;

    @InjectMocks
    private EmailServico servico;

    private Email email(Long id, String endereco) {
        Email email = new Email();
        email.setId(id);
        email.setEndereco(endereco);
        return email;
    }

    private Usuario usuarioComEmail(Email email) {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("Maria");
        if (email != null) {
            usuario.getEmails().add(email);
        }
        return usuario;
    }

    @Test
    @DisplayName("listarPorUsuario: devolve os e-mails do usuario")
    void listarPorUsuario() {
        Email existente = email(1L, "maria@email.com");
        when(usuarioRepositorio.findById(1L)).thenReturn(Optional.of(usuarioComEmail(existente)));

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
    @DisplayName("criar: persiste o e-mail e o vincula ao usuario, sempre como novo")
    void criarVinculaAoUsuario() {
        Usuario usuario = usuarioComEmail(null);
        when(usuarioRepositorio.findById(1L)).thenReturn(Optional.of(usuario));
        when(repositorio.save(any(Email.class))).thenAnswer(chamada -> {
            Email argumento = chamada.getArgument(0);
            assertEquals(null, argumento.getId());
            argumento.setId(99L);
            return argumento;
        });

        Email criado = servico.criar(1L, email(7L, "maria@email.com"));

        assertEquals(99L, criado.getId());
        assertSame(criado, usuario.getEmails().get(0));
        verify(usuarioRepositorio).save(usuario);
    }

    @Test
    @DisplayName("criar: lanca RecursoNaoEncontrado quando o usuario nao existe")
    void criarUsuarioInexistente() {
        when(usuarioRepositorio.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class,
                () -> servico.criar(999L, email(null, "maria@email.com")));

        verify(repositorio, never()).save(any(Email.class));
    }

    @Test
    @DisplayName("obterPorId: lanca RecursoNaoEncontrado quando o id nao existe")
    void obterPorIdInexistente() {
        when(repositorio.findById(999L)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException excecao = assertThrows(
                RecursoNaoEncontradoException.class, () -> servico.obterPorId(999L));

        assertEquals("Email de id 999 nao foi encontrado", excecao.getMessage());
    }

    @Test
    @DisplayName("atualizar: delega ao EmailAtualizador e salva")
    void atualizarDelegaAoAtualizador() {
        Email existente = email(2L, "maria@email.com");
        Email atualizacao = email(null, "maria.silva@email.com");
        when(repositorio.findById(2L)).thenReturn(Optional.of(existente));
        when(repositorio.save(existente)).thenReturn(existente);

        servico.atualizar(2L, atualizacao);

        verify(atualizador).atualizar(existente, atualizacao);
        verify(repositorio).save(existente);
    }

    @Test
    @DisplayName("excluir: remove o e-mail encontrado")
    void excluirExistente() {
        Email existente = email(2L, "maria@email.com");
        when(repositorio.findById(2L)).thenReturn(Optional.of(existente));

        servico.excluir(2L);

        verify(repositorio).delete(existente);
    }

    @Test
    @DisplayName("excluir: lanca RecursoNaoEncontrado quando o id nao existe")
    void excluirIdInexistente() {
        when(repositorio.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> servico.excluir(999L));

        verify(repositorio, never()).delete(any(Email.class));
    }
}
