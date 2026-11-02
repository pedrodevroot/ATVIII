package com.autobots.automanager.servicos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.autobots.automanager.entidades.Empresa;
import com.autobots.automanager.entidades.Servico;
import com.autobots.automanager.excecoes.ConflitoException;
import com.autobots.automanager.excecoes.RecursoNaoEncontradoException;
import com.autobots.automanager.modelo.ServicoAtualizador;
import com.autobots.automanager.repositorios.EmpresaRepositorio;
import com.autobots.automanager.repositorios.ServicoRepositorio;
import com.autobots.automanager.repositorios.VendaRepositorio;

@ExtendWith(MockitoExtension.class)
class ServicoServicoTest {

    @Mock
    private ServicoRepositorio repositorio;
    @Mock
    private EmpresaRepositorio empresaRepositorio;
    @Mock
    private VendaRepositorio vendaRepositorio;
    @Mock
    private ServicoAtualizador atualizador;

    @InjectMocks
    private ServicoServico servico;

    private Servico novoServico(Long id) {
        Servico item = new Servico();
        item.setId(id);
        item.setNome("Alinhamento");
        item.setValor(new BigDecimal("50.00"));
        return item;
    }

    private Empresa empresa() {
        Empresa empresa = new Empresa();
        empresa.setId(1L);
        return empresa;
    }

    @Test
    @DisplayName("listarPorEmpresa: devolve os servicos da empresa")
    void listarPorEmpresa() {
        Empresa empresa = empresa();
        Servico existente = novoServico(20L);
        empresa.getServicos().add(existente);
        when(empresaRepositorio.findById(1L)).thenReturn(Optional.of(empresa));

        assertSame(existente, servico.listarPorEmpresa(1L).get(0));
    }

    @Test
    @DisplayName("listarPorEmpresa: lanca RecursoNaoEncontrado quando a empresa nao existe")
    void listarPorEmpresaInexistente() {
        when(empresaRepositorio.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> servico.listarPorEmpresa(999L));
    }

    @Test
    @DisplayName("criar: vincula a empresa e salva sempre como novo")
    void criarVinculaAEmpresa() {
        Empresa empresa = empresa();
        when(empresaRepositorio.findById(1L)).thenReturn(Optional.of(empresa));
        when(repositorio.save(any(Servico.class))).thenAnswer(chamada -> {
            Servico argumento = chamada.getArgument(0);
            assertNull(argumento.getId());
            argumento.setId(99L);
            return argumento;
        });

        Servico criado = servico.criar(1L, novoServico(7L));

        assertEquals(99L, criado.getId());
        assertSame(criado, empresa.getServicos().get(0));
        verify(empresaRepositorio).save(empresa);
    }

    @Test
    @DisplayName("criar: lanca RecursoNaoEncontrado quando a empresa nao existe")
    void criarEmpresaInexistente() {
        when(empresaRepositorio.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> servico.criar(999L, novoServico(null)));

        verify(repositorio, never()).save(any(Servico.class));
    }

    @Test
    @DisplayName("obterPorId: lanca RecursoNaoEncontrado quando o id nao existe")
    void obterPorIdInexistente() {
        when(repositorio.findById(999L)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException excecao = assertThrows(
                RecursoNaoEncontradoException.class, () -> servico.obterPorId(999L));

        assertEquals("Servico de id 999 nao foi encontrado", excecao.getMessage());
    }

    @Test
    @DisplayName("atualizar: delega ao ServicoAtualizador e salva")
    void atualizarDelega() {
        Servico existente = novoServico(20L);
        Servico atualizacao = new Servico();
        when(repositorio.findById(20L)).thenReturn(Optional.of(existente));
        when(repositorio.save(existente)).thenReturn(existente);

        assertSame(existente, servico.atualizar(20L, atualizacao));

        verify(atualizador).atualizar(existente, atualizacao);
    }

    @Test
    @DisplayName("excluir: remove o servico sem vendas")
    void excluirSemVendas() {
        Servico existente = novoServico(20L);
        when(repositorio.findById(20L)).thenReturn(Optional.of(existente));
        when(vendaRepositorio.existsByItensServicoServicoId(20L)).thenReturn(false);

        servico.excluir(20L);

        verify(repositorio).delete(existente);
    }

    @Test
    @DisplayName("excluir: lanca Conflito quando o servico possui vendas")
    void excluirComVendas() {
        when(repositorio.findById(20L)).thenReturn(Optional.of(novoServico(20L)));
        when(vendaRepositorio.existsByItensServicoServicoId(20L)).thenReturn(true);

        ConflitoException excecao = assertThrows(ConflitoException.class, () -> servico.excluir(20L));

        assertEquals("O servico de id 20 possui vendas registradas e nao pode ser excluido",
                excecao.getMessage());
        verify(repositorio, never()).delete(any(Servico.class));
    }
}
