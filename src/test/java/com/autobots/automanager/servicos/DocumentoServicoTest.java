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
import com.autobots.automanager.enumeracoes.TipoDocumento;
import com.autobots.automanager.entidades.Documento;
import com.autobots.automanager.excecoes.RecursoNaoEncontradoException;
import com.autobots.automanager.modelo.DocumentoAtualizador;
import com.autobots.automanager.repositorios.UsuarioRepositorio;
import com.autobots.automanager.repositorios.DocumentoRepositorio;

@ExtendWith(MockitoExtension.class)
class DocumentoServicoTest {

    @Mock
    private DocumentoRepositorio repositorio;
    @Mock
    private UsuarioRepositorio usuarioRepositorio;
    @Mock
    private DocumentoAtualizador atualizador;

    @InjectMocks
    private DocumentoServico servico;

    private Documento documento(Long id, TipoDocumento tipo, String numero) {
        Documento documento = new Documento();
        documento.setId(id);
        documento.setTipo(tipo);
        documento.setNumero(numero);
        return documento;
    }

    private Usuario usuarioComDocumento(Documento documento) {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("Maria");
        if (documento != null) {
            usuario.getDocumentos().add(documento);
        }
        return usuario;
    }

    @Test
    @DisplayName("listarPorUsuario: devolve os documentos do usuario")
    void listarPorUsuario() {
        Documento existente = documento(1L, TipoDocumento.RG, "1500");
        when(usuarioRepositorio.findById(1L))
                .thenReturn(Optional.of(usuarioComDocumento(existente)));

        assertEquals(1, servico.listarPorUsuario(1L).size());
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
    @DisplayName("criar: persiste o documento e o vincula ao usuario")
    void criarVinculaAoUsuario() {
        Usuario usuario = usuarioComDocumento(null);
        when(usuarioRepositorio.findById(1L)).thenReturn(Optional.of(usuario));
        when(repositorio.save(any(Documento.class))).thenAnswer(chamada -> {
            Documento argumento = chamada.getArgument(0);
            argumento.setId(99L);
            return argumento;
        });

        Documento criado = servico.criar(1L, documento(null, TipoDocumento.CNH, "55443322"));

        assertEquals(99L, criado.getId());
        assertEquals(1, usuario.getDocumentos().size());
        assertSame(criado, usuario.getDocumentos().get(0));
        verify(usuarioRepositorio).save(usuario);
    }

    @Test
    @DisplayName("criar: lanca RecursoNaoEncontrado quando o usuario nao existe")
    void criarUsuarioInexistente() {
        when(usuarioRepositorio.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class,
                () -> servico.criar(999L, documento(null, TipoDocumento.CNH, "111")));

        verify(repositorio, never()).save(any(Documento.class));
    }

    @Test
    @DisplayName("obterPorId: devolve o documento encontrado")
    void obterPorId() {
        Documento existente = documento(2L, TipoDocumento.CPF, "00000000001");
        when(repositorio.findById(2L)).thenReturn(Optional.of(existente));

        assertSame(existente, servico.obterPorId(2L));
    }

    @Test
    @DisplayName("obterPorId: lanca RecursoNaoEncontrado quando o id nao existe")
    void obterPorIdInexistente() {
        when(repositorio.findById(999L)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException excecao = assertThrows(
                RecursoNaoEncontradoException.class, () -> servico.obterPorId(999L));

        assertEquals("Documento de id 999 nao foi encontrado", excecao.getMessage());
    }

    @Test
    @DisplayName("atualizar: delega ao DocumentoAtualizador e salva")
    void atualizarDelegaAoAtualizador() {
        Documento existente = documento(2L, TipoDocumento.RG, "1500");
        Documento atualizacao = documento(null, TipoDocumento.CNH, null);
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
                () -> servico.atualizar(999L, documento(null, TipoDocumento.CNH, null)));

        verify(repositorio, never()).save(any(Documento.class));
    }

    @Test
    @DisplayName("excluir: remove o documento encontrado")
    void excluirExistente() {
        Documento existente = documento(2L, TipoDocumento.RG, "1500");
        when(repositorio.findById(2L)).thenReturn(Optional.of(existente));

        servico.excluir(2L);

        verify(repositorio).delete(existente);
    }

    @Test
    @DisplayName("excluir: lanca RecursoNaoEncontrado quando o id nao existe")
    void excluirIdInexistente() {
        when(repositorio.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> servico.excluir(999L));

        verify(repositorio, never()).delete(any(Documento.class));
    }
}
