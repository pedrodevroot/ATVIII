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

import java.util.Calendar;
import java.util.Date;
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

import com.autobots.automanager.entidades.Cliente;
import com.autobots.automanager.excecoes.RecursoNaoEncontradoException;
import com.autobots.automanager.modelo.ClienteAtualizador;
import com.autobots.automanager.repositorios.ClienteRepositorio;

@ExtendWith(MockitoExtension.class)
class ClienteServicoTest {

    @Mock
    private ClienteRepositorio repositorio;
    @Mock
    private ClienteAtualizador atualizador;

    @InjectMocks
    private ClienteServico servico;

    private Cliente cliente(Long id, String nome) {
        Cliente cliente = new Cliente();
        cliente.setId(id);
        cliente.setNome(nome);
        return cliente;
    }

    private Date data(int ano, int mes, int dia) {
        Calendar calendario = Calendar.getInstance();
        calendario.set(ano, mes, dia, 0, 0, 0);
        calendario.set(Calendar.MILLISECOND, 0);
        return calendario.getTime();
    }

    @Test
    @DisplayName("cadastrar: define dataCadastro quando ela nao vem preenchida")
    void cadastrarDefineDataCadastro() {
        when(repositorio.save(any(Cliente.class))).thenAnswer(chamada -> chamada.getArgument(0));

        Cliente salvo = servico.cadastrar(cliente(null, "Maria"));

        assertNotNull(salvo.getDataCadastro());
    }

    @Test
    @DisplayName("cadastrar: preserva dataCadastro quando ela ja vem preenchida")
    void cadastrarPreservaDataCadastroExistente() {
        when(repositorio.save(any(Cliente.class))).thenAnswer(chamada -> chamada.getArgument(0));
        Cliente entrada = cliente(null, "Maria");
        Date original = data(2020, Calendar.JANUARY, 1);
        entrada.setDataCadastro(original);

        Cliente salvo = servico.cadastrar(entrada);

        assertEquals(original, salvo.getDataCadastro());
    }

    @Test
    @DisplayName("cadastrar: zera o id recebido, para que save() insira em vez de atualizar")
    void cadastrarZeraId() {
        when(repositorio.save(any(Cliente.class))).thenAnswer(chamada -> chamada.getArgument(0));

        servico.cadastrar(cliente(1L, "Tentativa de sobrescrever"));

        ArgumentCaptor<Cliente> capturado = ArgumentCaptor.forClass(Cliente.class);
        verify(repositorio).save(capturado.capture());
        assertNull(capturado.getValue().getId());
    }

    @Test
    @DisplayName("listar: delega ao repositorio ordenando por id")
    void listarOrdenaPorId() {
        List<Cliente> esperado = List.of(cliente(1L, "Maria"), cliente(2L, "Joao"));
        when(repositorio.findAll(Sort.by("id"))).thenReturn(esperado);

        assertEquals(esperado, servico.listar());

        verify(repositorio).findAll(Sort.by("id"));
        verify(repositorio, never()).findAll();
    }

    @Test
    @DisplayName("obterPorId: busca no banco por id, sem carregar a tabela inteira")
    void obterPorIdBuscaPorId() {
        Cliente esperado = cliente(1L, "Maria");
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

        assertEquals("Cliente de id 999 nao foi encontrado", excecao.getMessage());
    }

    @Test
    @DisplayName("atualizar: delega ao ClienteAtualizador e salva")
    void atualizarDelegaAoAtualizador() {
        Cliente existente = cliente(1L, "Maria");
        Cliente atualizacao = cliente(1L, "Maria Silva");
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
                IllegalArgumentException.class, () -> servico.atualizar(cliente(null, "Maria")));

        assertEquals("O id do cliente e obrigatorio para atualizacao", excecao.getMessage());
        verify(repositorio, never()).save(any(Cliente.class));
    }

    @Test
    @DisplayName("atualizar: lanca RecursoNaoEncontrado quando o id nao existe")
    void atualizarIdInexistente() {
        when(repositorio.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class,
                () -> servico.atualizar(cliente(999L, "Fantasma")));

        verify(repositorio, never()).save(any(Cliente.class));
    }

    @Test
    @DisplayName("excluir: remove o cliente encontrado")
    void excluirClienteExistente() {
        Cliente existente = cliente(1L, "Maria");
        when(repositorio.findById(1L)).thenReturn(Optional.of(existente));

        servico.excluir(1L);

        verify(repositorio).delete(existente);
    }

    @Test
    @DisplayName("excluir: lanca RecursoNaoEncontrado quando o id nao existe")
    void excluirIdInexistente() {
        when(repositorio.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> servico.excluir(999L));

        verify(repositorio, never()).delete(any(Cliente.class));
    }

    @Test
    @DisplayName("excluir: nao usa deleteById, para poder validar a existencia antes")
    void excluirNaoUsaDeleteById() {
        when(repositorio.findById(1L)).thenReturn(Optional.of(cliente(1L, "Maria")));

        servico.excluir(1L);

        verify(repositorio, never()).deleteById(anyLong());
    }
}
