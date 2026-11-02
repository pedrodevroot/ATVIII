package com.autobots.automanager.servicos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.autobots.automanager.dtos.MercadoriaAtualizacaoDto;
import com.autobots.automanager.entidades.Empresa;
import com.autobots.automanager.entidades.Mercadoria;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.enumeracoes.PerfilUsuario;
import com.autobots.automanager.excecoes.ConflitoException;
import com.autobots.automanager.excecoes.RecursoNaoEncontradoException;
import com.autobots.automanager.modelo.MercadoriaAtualizador;
import com.autobots.automanager.repositorios.EmpresaRepositorio;
import com.autobots.automanager.repositorios.MercadoriaRepositorio;
import com.autobots.automanager.repositorios.UsuarioRepositorio;
import com.autobots.automanager.repositorios.VendaRepositorio;

@ExtendWith(MockitoExtension.class)
class MercadoriaServicoTest {

    @Mock
    private MercadoriaRepositorio repositorio;
    @Mock
    private EmpresaRepositorio empresaRepositorio;
    @Mock
    private UsuarioRepositorio usuarioRepositorio;
    @Mock
    private VendaRepositorio vendaRepositorio;
    @Mock
    private MercadoriaAtualizador atualizador;

    @InjectMocks
    private MercadoriaServico servico;

    private Mercadoria mercadoria(Long id) {
        Mercadoria mercadoria = new Mercadoria();
        mercadoria.setId(id);
        mercadoria.setNome("Pneu");
        mercadoria.setFabricacao(new Date(2000L));
        mercadoria.setQuantidade(10);
        mercadoria.setValor(new BigDecimal("300.00"));
        return mercadoria;
    }

    private Usuario usuario(long id, PerfilUsuario perfil) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.getPerfis().add(perfil);
        return usuario;
    }

    private Empresa empresa() {
        Empresa empresa = new Empresa();
        empresa.setId(1L);
        return empresa;
    }

    @Test
    @DisplayName("listarPorEmpresa: devolve as mercadorias da empresa")
    void listarPorEmpresa() {
        Empresa empresa = empresa();
        Mercadoria existente = mercadoria(10L);
        empresa.getMercadorias().add(existente);
        when(empresaRepositorio.findById(1L)).thenReturn(Optional.of(empresa));

        assertSame(existente, servico.listarPorEmpresa(1L).get(0));
    }

    @Test
    @DisplayName("listarPorEmpresa: lanca RecursoNaoEncontrado quando a empresa nao existe")
    void listarPorEmpresaInexistente() {
        when(empresaRepositorio.findById(999L)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException excecao = assertThrows(
                RecursoNaoEncontradoException.class, () -> servico.listarPorEmpresa(999L));

        assertEquals("Empresa de id 999 nao foi encontrado", excecao.getMessage());
    }

    @Test
    @DisplayName("criar: define a data de cadastro e vincula a empresa, sempre como nova")
    void criarVinculaAEmpresa() {
        Empresa empresa = empresa();
        when(empresaRepositorio.findById(1L)).thenReturn(Optional.of(empresa));
        when(repositorio.save(any(Mercadoria.class))).thenAnswer(chamada -> {
            Mercadoria argumento = chamada.getArgument(0);
            assertNull(argumento.getId());
            argumento.setId(99L);
            return argumento;
        });

        Mercadoria criada = servico.criar(1L, mercadoria(7L));

        assertEquals(99L, criada.getId());
        assertNotNull(criada.getCadastro());
        assertSame(criada, empresa.getMercadorias().get(0));
        verify(empresaRepositorio).save(empresa);
    }

    @Test
    @DisplayName("criar: recusa validade anterior a fabricacao")
    void criarValidadeAnterior() {
        when(empresaRepositorio.findById(1L)).thenReturn(Optional.of(empresa()));
        Mercadoria invalida = mercadoria(null);
        invalida.setValidade(new Date(1000L));

        IllegalArgumentException excecao = assertThrows(
                IllegalArgumentException.class, () -> servico.criar(1L, invalida));

        assertEquals("A data de validade nao pode ser anterior a data de fabricacao", excecao.getMessage());
        verify(repositorio, never()).save(any(Mercadoria.class));
    }

    @Test
    @DisplayName("criar: lanca RecursoNaoEncontrado quando a empresa nao existe")
    void criarEmpresaInexistente() {
        when(empresaRepositorio.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> servico.criar(999L, mercadoria(null)));

        verify(repositorio, never()).save(any(Mercadoria.class));
    }

    @Test
    @DisplayName("obterPorId: lanca RecursoNaoEncontrado quando o id nao existe")
    void obterPorIdInexistente() {
        when(repositorio.findById(999L)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException excecao = assertThrows(
                RecursoNaoEncontradoException.class, () -> servico.obterPorId(999L));

        assertEquals("Mercadoria de id 999 nao foi encontrado", excecao.getMessage());
    }

    @Test
    @DisplayName("atualizar: delega ao MercadoriaAtualizador e salva")
    void atualizarDelega() {
        Mercadoria existente = mercadoria(10L);
        MercadoriaAtualizacaoDto atualizacao = new MercadoriaAtualizacaoDto();
        when(repositorio.findById(10L)).thenReturn(Optional.of(existente));
        when(repositorio.save(existente)).thenReturn(existente);

        assertSame(existente, servico.atualizar(10L, atualizacao));

        verify(atualizador).atualizar(existente, atualizacao);
    }

    @Test
    @DisplayName("atualizar: confere as datas depois de aplicar a alteracao")
    void atualizarValidadeAnterior() {
        Mercadoria existente = mercadoria(10L);
        existente.setValidade(new Date(1000L));
        when(repositorio.findById(10L)).thenReturn(Optional.of(existente));

        assertThrows(IllegalArgumentException.class,
                () -> servico.atualizar(10L, new MercadoriaAtualizacaoDto()));

        verify(repositorio, never()).save(any(Mercadoria.class));
    }

    @Test
    @DisplayName("excluir: remove a mercadoria sem vendas")
    void excluirSemVendas() {
        Mercadoria existente = mercadoria(10L);
        when(repositorio.findById(10L)).thenReturn(Optional.of(existente));
        when(vendaRepositorio.existsByMercadoriasId(10L)).thenReturn(false);

        servico.excluir(10L);

        verify(repositorio).delete(existente);
    }

    @Test
    @DisplayName("excluir: lanca Conflito quando a mercadoria possui vendas")
    void excluirComVendas() {
        when(repositorio.findById(10L)).thenReturn(Optional.of(mercadoria(10L)));
        when(vendaRepositorio.existsByMercadoriasId(10L)).thenReturn(true);

        ConflitoException excecao = assertThrows(ConflitoException.class, () -> servico.excluir(10L));

        assertEquals("A mercadoria de id 10 possui vendas registradas e nao pode ser excluida",
                excecao.getMessage());
        verify(repositorio, never()).delete(any(Mercadoria.class));
    }

    @Test
    @DisplayName("listarPorFornecedor: devolve as mercadorias do usuario")
    void listarPorFornecedor() {
        Usuario fornecedor = usuario(2L, PerfilUsuario.FORNECEDOR);
        Mercadoria existente = mercadoria(10L);
        fornecedor.getMercadorias().add(existente);
        when(usuarioRepositorio.findById(2L)).thenReturn(Optional.of(fornecedor));

        assertSame(existente, servico.listarPorFornecedor(2L).get(0));
    }

    @Test
    @DisplayName("vincularFornecedor: adiciona a mercadoria ao fornecedor")
    void vincularOk() {
        Usuario fornecedor = usuario(2L, PerfilUsuario.FORNECEDOR);
        Mercadoria existente = mercadoria(10L);
        when(usuarioRepositorio.findById(2L)).thenReturn(Optional.of(fornecedor));
        when(repositorio.findById(10L)).thenReturn(Optional.of(existente));
        when(usuarioRepositorio.findByMercadoriasId(10L)).thenReturn(Optional.empty());

        servico.vincularFornecedor(2L, 10L);

        assertSame(existente, fornecedor.getMercadorias().get(0));
        verify(usuarioRepositorio).save(fornecedor);
    }

    @Test
    @DisplayName("vincularFornecedor: recusa usuario sem o perfil FORNECEDOR")
    void vincularSemPerfil() {
        when(usuarioRepositorio.findById(3L)).thenReturn(Optional.of(usuario(3L, PerfilUsuario.CLIENTE)));

        IllegalArgumentException excecao = assertThrows(
                IllegalArgumentException.class, () -> servico.vincularFornecedor(3L, 10L));

        assertEquals("O usuario de id 3 nao possui o perfil FORNECEDOR", excecao.getMessage());
        verify(usuarioRepositorio, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("vincularFornecedor: repetir o mesmo vinculo nao altera nada")
    void vincularRepetido() {
        Usuario fornecedor = usuario(2L, PerfilUsuario.FORNECEDOR);
        when(usuarioRepositorio.findById(2L)).thenReturn(Optional.of(fornecedor));
        when(repositorio.findById(10L)).thenReturn(Optional.of(mercadoria(10L)));
        when(usuarioRepositorio.findByMercadoriasId(10L)).thenReturn(Optional.of(fornecedor));

        servico.vincularFornecedor(2L, 10L);

        verify(usuarioRepositorio, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("vincularFornecedor: lanca Conflito quando a mercadoria ja tem outro fornecedor")
    void vincularOutroFornecedor() {
        when(usuarioRepositorio.findById(2L)).thenReturn(Optional.of(usuario(2L, PerfilUsuario.FORNECEDOR)));
        when(repositorio.findById(10L)).thenReturn(Optional.of(mercadoria(10L)));
        when(usuarioRepositorio.findByMercadoriasId(10L))
                .thenReturn(Optional.of(usuario(8L, PerfilUsuario.FORNECEDOR)));

        assertThrows(ConflitoException.class, () -> servico.vincularFornecedor(2L, 10L));

        verify(usuarioRepositorio, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("desvincularFornecedor: retira a mercadoria do fornecedor")
    void desvincularOk() {
        Usuario fornecedor = usuario(2L, PerfilUsuario.FORNECEDOR);
        fornecedor.getMercadorias().add(mercadoria(10L));
        when(usuarioRepositorio.findById(2L)).thenReturn(Optional.of(fornecedor));

        servico.desvincularFornecedor(2L, 10L);

        assertTrue(fornecedor.getMercadorias().isEmpty());
        verify(usuarioRepositorio).save(fornecedor);
    }

    @Test
    @DisplayName("desvincularFornecedor: lanca RecursoNaoEncontrado quando nao ha vinculo")
    void desvincularSemVinculo() {
        when(usuarioRepositorio.findById(2L)).thenReturn(Optional.of(usuario(2L, PerfilUsuario.FORNECEDOR)));

        RecursoNaoEncontradoException excecao = assertThrows(
                RecursoNaoEncontradoException.class, () -> servico.desvincularFornecedor(2L, 10L));

        assertEquals("A mercadoria de id 10 nao esta vinculada ao usuario de id 2", excecao.getMessage());
        verify(usuarioRepositorio, never()).save(any(Usuario.class));
    }
}
