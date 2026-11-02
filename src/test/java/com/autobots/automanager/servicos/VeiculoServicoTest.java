package com.autobots.automanager.servicos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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
import com.autobots.automanager.entidades.Veiculo;
import com.autobots.automanager.enumeracoes.TipoVeiculo;
import com.autobots.automanager.excecoes.ConflitoException;
import com.autobots.automanager.excecoes.RecursoNaoEncontradoException;
import com.autobots.automanager.modelo.VeiculoAtualizador;
import com.autobots.automanager.repositorios.UsuarioRepositorio;
import com.autobots.automanager.repositorios.VeiculoRepositorio;
import com.autobots.automanager.repositorios.VendaRepositorio;

@ExtendWith(MockitoExtension.class)
class VeiculoServicoTest {

    @Mock
    private VeiculoRepositorio repositorio;
    @Mock
    private UsuarioRepositorio usuarioRepositorio;
    @Mock
    private VendaRepositorio vendaRepositorio;
    @Mock
    private VeiculoAtualizador atualizador;

    @InjectMocks
    private VeiculoServico servico;

    private Veiculo veiculo(Long id) {
        Veiculo veiculo = new Veiculo();
        veiculo.setId(id);
        veiculo.setTipo(TipoVeiculo.SUV);
        veiculo.setModelo("Corolla Cross");
        veiculo.setPlaca("ABC1D23");
        return veiculo;
    }

    private Usuario dono() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        return usuario;
    }

    @Test
    @DisplayName("listarPorUsuario: devolve os veiculos do usuario")
    void listarPorUsuario() {
        Usuario usuario = dono();
        Veiculo existente = veiculo(5L);
        usuario.getVeiculos().add(existente);
        when(usuarioRepositorio.findById(1L)).thenReturn(Optional.of(usuario));

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
    @DisplayName("criar: define o proprietario e salva sempre como novo")
    void criarDefineProprietario() {
        Usuario usuario = dono();
        when(usuarioRepositorio.findById(1L)).thenReturn(Optional.of(usuario));
        when(repositorio.save(any(Veiculo.class))).thenAnswer(chamada -> {
            Veiculo argumento = chamada.getArgument(0);
            assertNull(argumento.getId());
            argumento.setId(99L);
            return argumento;
        });

        Veiculo criado = servico.criar(1L, veiculo(7L));

        assertEquals(99L, criado.getId());
        assertSame(usuario, criado.getProprietario());
    }

    @Test
    @DisplayName("criar: lanca RecursoNaoEncontrado quando o usuario nao existe")
    void criarUsuarioInexistente() {
        when(usuarioRepositorio.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> servico.criar(999L, veiculo(null)));

        verify(repositorio, never()).save(any(Veiculo.class));
    }

    @Test
    @DisplayName("obterPorId: lanca RecursoNaoEncontrado quando o id nao existe")
    void obterPorIdInexistente() {
        when(repositorio.findById(999L)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException excecao = assertThrows(
                RecursoNaoEncontradoException.class, () -> servico.obterPorId(999L));

        assertEquals("Veiculo de id 999 nao foi encontrado", excecao.getMessage());
    }

    @Test
    @DisplayName("atualizar: delega ao VeiculoAtualizador e salva")
    void atualizarDelega() {
        Veiculo existente = veiculo(5L);
        Veiculo atualizacao = new Veiculo();
        when(repositorio.findById(5L)).thenReturn(Optional.of(existente));
        when(repositorio.save(existente)).thenReturn(existente);

        assertSame(existente, servico.atualizar(5L, atualizacao));

        verify(atualizador).atualizar(existente, atualizacao);
    }

    @Test
    @DisplayName("excluir: remove o veiculo sem vendas")
    void excluirSemVendas() {
        Veiculo existente = veiculo(5L);
        when(repositorio.findById(5L)).thenReturn(Optional.of(existente));
        when(vendaRepositorio.existsByVeiculoId(5L)).thenReturn(false);

        servico.excluir(5L);

        verify(repositorio).delete(existente);
    }

    @Test
    @DisplayName("excluir: lanca Conflito quando o veiculo possui vendas")
    void excluirComVendas() {
        Veiculo existente = veiculo(5L);
        when(repositorio.findById(5L)).thenReturn(Optional.of(existente));
        when(vendaRepositorio.existsByVeiculoId(5L)).thenReturn(true);

        ConflitoException excecao = assertThrows(ConflitoException.class, () -> servico.excluir(5L));

        assertEquals("O veiculo de id 5 possui vendas registradas e nao pode ser excluido",
                excecao.getMessage());
        verify(repositorio, never()).delete(any(Veiculo.class));
    }

    @Test
    @DisplayName("excluir: lanca RecursoNaoEncontrado quando o id nao existe")
    void excluirInexistente() {
        when(repositorio.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> servico.excluir(999L));

        verify(repositorio, never()).delete(any(Veiculo.class));
    }
}
