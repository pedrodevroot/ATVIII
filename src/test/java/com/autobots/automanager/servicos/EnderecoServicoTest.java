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

import com.autobots.automanager.entidades.Empresa;
import com.autobots.automanager.entidades.Endereco;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.excecoes.ConflitoException;
import com.autobots.automanager.excecoes.RecursoNaoEncontradoException;
import com.autobots.automanager.modelo.EnderecoAtualizador;
import com.autobots.automanager.repositorios.EmpresaRepositorio;
import com.autobots.automanager.repositorios.EnderecoRepositorio;
import com.autobots.automanager.repositorios.UsuarioRepositorio;

@ExtendWith(MockitoExtension.class)
class EnderecoServicoTest {

    @Mock
    private EnderecoRepositorio repositorio;
    @Mock
    private UsuarioRepositorio usuarioRepositorio;
    @Mock
    private EmpresaRepositorio empresaRepositorio;
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

    private Usuario usuarioComEndereco(Endereco endereco) {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("Maria");
        usuario.setEndereco(endereco);
        return usuario;
    }

    @Test
    @DisplayName("obterPorUsuario: devolve o endereco do usuario")
    void obterPorUsuario() {
        Endereco existente = endereco(3L, "Santos");
        when(usuarioRepositorio.findById(1L)).thenReturn(Optional.of(usuarioComEndereco(existente)));

        assertSame(existente, servico.obterPorUsuario(1L));
    }

    @Test
    @DisplayName("obterPorUsuario: lanca RecursoNaoEncontrado quando o usuario nao tem endereco")
    void obterPorUsuarioSemEndereco() {
        when(usuarioRepositorio.findById(1L)).thenReturn(Optional.of(usuarioComEndereco(null)));

        RecursoNaoEncontradoException excecao = assertThrows(
                RecursoNaoEncontradoException.class, () -> servico.obterPorUsuario(1L));

        assertEquals("O usuario de id 1 nao possui endereco", excecao.getMessage());
    }

    @Test
    @DisplayName("obterPorUsuario: lanca RecursoNaoEncontrado quando o usuario nao existe")
    void obterPorUsuarioInexistente() {
        when(usuarioRepositorio.findById(999L)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException excecao = assertThrows(
                RecursoNaoEncontradoException.class, () -> servico.obterPorUsuario(999L));

        assertEquals("Usuario de id 999 nao foi encontrado", excecao.getMessage());
    }

    @Test
    @DisplayName("criar: persiste o endereco e o vincula ao usuario")
    void criarVinculaAoUsuario() {
        Usuario usuario = usuarioComEndereco(null);
        when(usuarioRepositorio.findById(1L)).thenReturn(Optional.of(usuario));
        when(repositorio.save(any(Endereco.class))).thenAnswer(chamada -> {
            Endereco argumento = chamada.getArgument(0);
            argumento.setId(99L);
            return argumento;
        });

        Endereco criado = servico.criar(1L, endereco(null, "Santos"));

        assertEquals(99L, criado.getId());
        assertSame(criado, usuario.getEndereco());
        verify(usuarioRepositorio).save(usuario);
    }

    @Test
    @DisplayName("criar: lanca Conflito quando o usuario ja tem endereco, apontando a rota de alteracao")
    void criarSegundoEndereco() {
        Usuario usuario = usuarioComEndereco(endereco(3L, "Santos"));
        when(usuarioRepositorio.findById(1L)).thenReturn(Optional.of(usuario));

        ConflitoException excecao = assertThrows(ConflitoException.class,
                () -> servico.criar(1L, endereco(null, "Rio de Janeiro")));

        assertEquals("O usuario de id 1 ja possui endereco. Para altera-lo, use PUT /endereco/3",
                excecao.getMessage());
        assertEquals("Santos", usuario.getEndereco().getCidade());
        verify(repositorio, never()).save(any(Endereco.class));
    }

    @Test
    @DisplayName("criar: zera um id enviado, para que o endereco seja sempre novo")
    void criarZeraId() {
        Usuario usuario = usuarioComEndereco(null);
        when(usuarioRepositorio.findById(1L)).thenReturn(Optional.of(usuario));
        when(repositorio.save(any(Endereco.class))).thenAnswer(chamada -> {
            Endereco argumento = chamada.getArgument(0);
            assertEquals(null, argumento.getId());
            argumento.setId(99L);
            return argumento;
        });

        assertEquals(99L, servico.criar(1L, endereco(77L, "Santos")).getId());
    }

    @Test
    @DisplayName("criar: lanca RecursoNaoEncontrado quando o usuario nao existe")
    void criarUsuarioInexistente() {
        when(usuarioRepositorio.findById(999L)).thenReturn(Optional.empty());

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
    @DisplayName("excluir: desliga o endereco do dono, e o orphanRemoval apaga a linha")
    void excluirDesligaDoDono() {
        Endereco existente = endereco(3L, "Santos");
        Usuario dono = usuarioComEndereco(existente);
        when(repositorio.findById(3L)).thenReturn(Optional.of(existente));
        when(usuarioRepositorio.findByEnderecoId(3L)).thenReturn(Optional.of(dono));

        servico.excluir(3L);

        assertNull(dono.getEndereco());
        verify(repositorio, never()).delete(any(Endereco.class));
    }

    @Test
    @DisplayName("excluir: desliga o endereco da empresa dona")
    void excluirDesligaDaEmpresa() {
        Endereco existente = endereco(3L, "Santos");
        Empresa dona = new Empresa();
        dona.setEndereco(existente);
        when(repositorio.findById(3L)).thenReturn(Optional.of(existente));
        when(usuarioRepositorio.findByEnderecoId(3L)).thenReturn(Optional.empty());
        when(empresaRepositorio.findByEnderecoId(3L)).thenReturn(Optional.of(dona));

        servico.excluir(3L);

        assertNull(dona.getEndereco());
        verify(repositorio, never()).delete(any(Endereco.class));
    }

    @Test
    @DisplayName("obterPorEmpresa: devolve o endereco da empresa")
    void obterPorEmpresa() {
        Endereco existente = endereco(3L, "Santos");
        Empresa empresa = new Empresa();
        empresa.setEndereco(existente);
        when(empresaRepositorio.findById(1L)).thenReturn(Optional.of(empresa));

        assertSame(existente, servico.obterPorEmpresa(1L));
    }

    @Test
    @DisplayName("obterPorEmpresa: lanca RecursoNaoEncontrado quando a empresa nao tem endereco")
    void obterPorEmpresaSemEndereco() {
        when(empresaRepositorio.findById(1L)).thenReturn(Optional.of(new Empresa()));

        RecursoNaoEncontradoException excecao = assertThrows(
                RecursoNaoEncontradoException.class, () -> servico.obterPorEmpresa(1L));

        assertEquals("A empresa de id 1 nao possui endereco", excecao.getMessage());
    }

    @Test
    @DisplayName("criarNaEmpresa: persiste o endereco e o vincula a empresa")
    void criarVinculaAEmpresa() {
        Empresa empresa = new Empresa();
        when(empresaRepositorio.findById(1L)).thenReturn(Optional.of(empresa));
        when(repositorio.save(any(Endereco.class))).thenAnswer(chamada -> {
            Endereco argumento = chamada.getArgument(0);
            argumento.setId(99L);
            return argumento;
        });

        Endereco criado = servico.criarNaEmpresa(1L, endereco(null, "Santos"));

        assertSame(criado, empresa.getEndereco());
        verify(empresaRepositorio).save(empresa);
    }

    @Test
    @DisplayName("criarNaEmpresa: lanca Conflito quando a empresa ja tem endereco")
    void criarSegundoEnderecoNaEmpresa() {
        Empresa empresa = new Empresa();
        empresa.setEndereco(endereco(3L, "Santos"));
        when(empresaRepositorio.findById(1L)).thenReturn(Optional.of(empresa));

        ConflitoException excecao = assertThrows(ConflitoException.class,
                () -> servico.criarNaEmpresa(1L, endereco(null, "Rio de Janeiro")));

        assertEquals("A empresa de id 1 ja possui endereco. Para altera-lo, use PUT /endereco/3",
                excecao.getMessage());
        verify(repositorio, never()).save(any(Endereco.class));
    }

    @Test
    @DisplayName("excluir: apaga direto o endereco que nao pertence a ninguem")
    void excluirSemDono() {
        Endereco existente = endereco(3L, "Santos");
        when(repositorio.findById(3L)).thenReturn(Optional.of(existente));
        when(usuarioRepositorio.findByEnderecoId(3L)).thenReturn(Optional.empty());

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
