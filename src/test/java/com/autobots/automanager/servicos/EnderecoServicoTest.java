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
import com.autobots.automanager.entidades.Endereco;
import com.autobots.automanager.excecoes.RecursoNaoEncontradoException;
import com.autobots.automanager.modelo.EnderecoAtualizador;
import com.autobots.automanager.repositorios.ClienteRepositorio;
import com.autobots.automanager.repositorios.EnderecoRepositorio;

@ExtendWith(MockitoExtension.class)
class EnderecoServicoTest {

    @Mock
    private EnderecoRepositorio repositorio;
    @Mock
    private ClienteRepositorio clienteRepositorio;
    @Mock
    private EnderecoAtualizador atualizador;

    @InjectMocks
    private EnderecoServico servico;

    private Endereco endereco(Long id, String cidade) {
        Endereco endereco = new Endereco();
        endereco.setId(id);
        endereco.setEstado("Sao Paulo");
        endereco.setCidade(cidade);
        endereco.setRua("Rua XV");
        endereco.setNumero("100");
        return endereco;
    }

    private Cliente clienteComEndereco(Endereco endereco) {
        Cliente cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNome("Maria");
        if (endereco != null) {
            cliente.getEnderecos().add(endereco);
        }
        return cliente;
    }

    @Test
    @DisplayName("listarPorCliente: devolve os enderecos do cliente")
    void listarPorCliente() {
        Endereco existente = endereco(3L, "Santos");
        when(clienteRepositorio.findById(1L))
                .thenReturn(Optional.of(clienteComEndereco(existente)));

        assertEquals(1, servico.listarPorCliente(1L).size());
        assertSame(existente, servico.listarPorCliente(1L).get(0));
    }

    @Test
    @DisplayName("listarPorCliente: devolve lista vazia quando o cliente nao tem endereco")
    void listarPorClienteSemEndereco() {
        when(clienteRepositorio.findById(1L)).thenReturn(Optional.of(clienteComEndereco(null)));

        assertEquals(0, servico.listarPorCliente(1L).size());
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
    @DisplayName("criar: persiste o endereco e o vincula ao cliente")
    void criarVinculaAoCliente() {
        Cliente cliente = clienteComEndereco(null);
        when(clienteRepositorio.findById(1L)).thenReturn(Optional.of(cliente));
        when(repositorio.save(any(Endereco.class))).thenAnswer(chamada -> {
            Endereco argumento = chamada.getArgument(0);
            argumento.setId(99L);
            return argumento;
        });

        Endereco criado = servico.criar(1L, endereco(null, "Santos"));

        assertEquals(99L, criado.getId());
        assertEquals(1, cliente.getEnderecos().size());
        assertSame(criado, cliente.getEnderecos().get(0));
        verify(clienteRepositorio).save(cliente);
    }

    @Test
    @DisplayName("criar: aceita um segundo endereco no mesmo cliente")
    void criarSegundoEndereco() {
        Cliente cliente = clienteComEndereco(endereco(3L, "Santos"));
        when(clienteRepositorio.findById(1L)).thenReturn(Optional.of(cliente));
        when(repositorio.save(any(Endereco.class))).thenAnswer(chamada -> {
            Endereco argumento = chamada.getArgument(0);
            argumento.setId(4L);
            return argumento;
        });

        servico.criar(1L, endereco(null, "Rio de Janeiro"));

        assertEquals(2, cliente.getEnderecos().size());
        assertEquals("Santos", cliente.getEnderecos().get(0).getCidade());
        assertEquals("Rio de Janeiro", cliente.getEnderecos().get(1).getCidade());
    }

    @Test
    @DisplayName("criar: zera um id enviado, para que o endereco seja sempre novo")
    void criarZeraId() {
        Cliente cliente = clienteComEndereco(null);
        when(clienteRepositorio.findById(1L)).thenReturn(Optional.of(cliente));
        when(repositorio.save(any(Endereco.class))).thenAnswer(chamada -> {
            Endereco argumento = chamada.getArgument(0);
            assertEquals(null, argumento.getId());
            argumento.setId(99L);
            return argumento;
        });

        assertEquals(99L, servico.criar(1L, endereco(77L, "Santos")).getId());
    }

    @Test
    @DisplayName("criar: lanca RecursoNaoEncontrado quando o cliente nao existe")
    void criarClienteInexistente() {
        when(clienteRepositorio.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class,
                () -> servico.criar(999L, endereco(null, "Santos")));

        verify(repositorio, never()).save(any(Endereco.class));
    }

    @Test
    @DisplayName("obterPorId: devolve o endereco encontrado")
    void obterPorId() {
        Endereco existente = endereco(3L, "Santos");
        when(repositorio.findById(3L)).thenReturn(Optional.of(existente));

        assertSame(existente, servico.obterPorId(3L));
    }

    @Test
    @DisplayName("obterPorId: lanca RecursoNaoEncontrado quando o id nao existe")
    void obterPorIdInexistente() {
        when(repositorio.findById(999L)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException excecao = assertThrows(
                RecursoNaoEncontradoException.class, () -> servico.obterPorId(999L));

        assertEquals("Endereco de id 999 nao foi encontrado", excecao.getMessage());
    }

    @Test
    @DisplayName("atualizar: delega ao EnderecoAtualizador e salva")
    void atualizarDelegaAoAtualizador() {
        Endereco existente = endereco(3L, "Santos");
        Endereco atualizacao = endereco(null, "Rio de Janeiro");
        when(repositorio.findById(3L)).thenReturn(Optional.of(existente));
        when(repositorio.save(existente)).thenReturn(existente);

        servico.atualizar(3L, atualizacao);

        verify(atualizador).atualizar(existente, atualizacao);
        verify(repositorio).save(existente);
    }

    @Test
    @DisplayName("atualizar: lanca RecursoNaoEncontrado quando o id nao existe")
    void atualizarIdInexistente() {
        when(repositorio.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class,
                () -> servico.atualizar(999L, endereco(null, "Santos")));

        verify(repositorio, never()).save(any(Endereco.class));
    }

    @Test
    @DisplayName("excluir: remove o endereco encontrado")
    void excluirExistente() {
        Endereco existente = endereco(3L, "Santos");
        when(repositorio.findById(3L)).thenReturn(Optional.of(existente));

        servico.excluir(3L);

        verify(repositorio).delete(existente);
    }

    @Test
    @DisplayName("excluir: lanca RecursoNaoEncontrado quando o id nao existe")
    void excluirIdInexistente() {
        when(repositorio.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> servico.excluir(999L));

        verify(repositorio, never()).delete(any(Endereco.class));
    }
}
