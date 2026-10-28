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

import com.autobots.automanager.entidades.Cliente;
import com.autobots.automanager.entidades.Documento;
import com.autobots.automanager.excecoes.RecursoNaoEncontradoException;
import com.autobots.automanager.modelo.DocumentoAtualizador;
import com.autobots.automanager.repositorios.ClienteRepositorio;
import com.autobots.automanager.repositorios.DocumentoRepositorio;

@ExtendWith(MockitoExtension.class)
class DocumentoServicoTest {

    @Mock
    private DocumentoRepositorio repositorio;
    @Mock
    private ClienteRepositorio clienteRepositorio;
    @Mock
    private DocumentoAtualizador atualizador;

    @InjectMocks
    private DocumentoServico servico;

    private Documento documento(Long id, String tipo, String numero) {
        Documento documento = new Documento();
        documento.setId(id);
        documento.setTipo(tipo);
        documento.setNumero(numero);
        return documento;
    }

    private Cliente clienteComDocumento(Documento documento) {
        Cliente cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNome("Maria");
        if (documento != null) {
            cliente.getDocumentos().add(documento);
        }
        return cliente;
    }

    @Test
    @DisplayName("listarPorCliente: devolve os documentos do cliente")
    void listarPorCliente() {
        Documento existente = documento(1L, "RG", "1500");
        when(clienteRepositorio.findById(1L))
                .thenReturn(Optional.of(clienteComDocumento(existente)));

        assertEquals(1, servico.listarPorCliente(1L).size());
    }

    @Test
    @DisplayName("listarPorCliente: lanca RecursoNaoEncontrado quando o cliente nao existe")
    void listarPorClienteInexistente() {
        when(clienteRepositorio.findById(999L)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException excecao = assertThrows(
                RecursoNaoEncontradoException.class, () -> servico.listarPorCliente(999L));

        assertEquals("Cliente de id 999 nao foi encontrado", excecao.getMessage());
    }

    @Test
    @DisplayName("criar: persiste o documento e o vincula ao cliente")
    void criarVinculaAoCliente() {
        Cliente cliente = clienteComDocumento(null);
        when(clienteRepositorio.findById(1L)).thenReturn(Optional.of(cliente));
        when(repositorio.save(any(Documento.class))).thenAnswer(chamada -> {
            Documento argumento = chamada.getArgument(0);
            argumento.setId(99L);
            return argumento;
        });

        Documento criado = servico.criar(1L, documento(null, "CNH", "55443322"));

        assertEquals(99L, criado.getId());
        assertEquals(1, cliente.getDocumentos().size());
        assertSame(criado, cliente.getDocumentos().get(0));
        verify(clienteRepositorio).save(cliente);
    }

    @Test
    @DisplayName("criar: lanca RecursoNaoEncontrado quando o cliente nao existe")
    void criarClienteInexistente() {
        when(clienteRepositorio.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class,
                () -> servico.criar(999L, documento(null, "CNH", "111")));

        verify(repositorio, never()).save(any(Documento.class));
    }

    @Test
    @DisplayName("obterPorId: devolve o documento encontrado")
    void obterPorId() {
        Documento existente = documento(2L, "CPF", "00000000001");
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
        Documento existente = documento(2L, "RG", "1500");
        Documento atualizacao = documento(null, "CNH", null);
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
                () -> servico.atualizar(999L, documento(null, "CNH", null)));

        verify(repositorio, never()).save(any(Documento.class));
    }

    @Test
    @DisplayName("excluir: remove o documento encontrado")
    void excluirExistente() {
        Documento existente = documento(2L, "RG", "1500");
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
