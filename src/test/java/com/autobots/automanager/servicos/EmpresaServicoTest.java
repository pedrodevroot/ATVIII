package com.autobots.automanager.servicos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import com.autobots.automanager.entidades.Empresa;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.excecoes.ConflitoException;
import com.autobots.automanager.excecoes.RecursoNaoEncontradoException;
import com.autobots.automanager.modelo.EmpresaAtualizador;
import com.autobots.automanager.repositorios.EmpresaRepositorio;
import com.autobots.automanager.repositorios.UsuarioRepositorio;

@ExtendWith(MockitoExtension.class)
class EmpresaServicoTest {

    @Mock
    private EmpresaRepositorio repositorio;
    @Mock
    private UsuarioRepositorio usuarioRepositorio;
    @Mock
    private EmpresaAtualizador atualizador;

    @InjectMocks
    private EmpresaServico servico;

    private Empresa empresa(Long id, String razaoSocial) {
        Empresa empresa = new Empresa();
        empresa.setId(id);
        empresa.setRazaoSocial(razaoSocial);
        return empresa;
    }

    private Usuario usuario(Long id, Empresa empresa) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setNome("Maria");
        usuario.setEmpresa(empresa);
        return usuario;
    }

    @Test
    @DisplayName("cadastrar: o servidor define a data de cadastro")
    void cadastrarDefineData() {
        when(repositorio.save(any(Empresa.class))).thenAnswer(chamada -> chamada.getArgument(0));

        assertNotNull(servico.cadastrar(empresa(null, "Nova")).getCadastro());
    }

    @Test
    @DisplayName("cadastrar: zera o id recebido, para que save() insira em vez de atualizar")
    void cadastrarZeraId() {
        when(repositorio.save(any(Empresa.class))).thenAnswer(chamada -> chamada.getArgument(0));

        servico.cadastrar(empresa(1L, "Tentativa de sobrescrever"));

        ArgumentCaptor<Empresa> capturada = ArgumentCaptor.forClass(Empresa.class);
        verify(repositorio).save(capturada.capture());
        assertNull(capturada.getValue().getId());
    }

    @Test
    @DisplayName("listar: delega ao repositorio ordenando por id")
    void listarOrdenaPorId() {
        List<Empresa> esperado = List.of(empresa(1L, "Primeira"));
        when(repositorio.findAll(Sort.by("id"))).thenReturn(esperado);

        assertEquals(esperado, servico.listar());
    }

    @Test
    @DisplayName("obterPorId: lanca RecursoNaoEncontrado quando o id nao existe")
    void obterPorIdInexistente() {
        when(repositorio.findById(999L)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException excecao = assertThrows(
                RecursoNaoEncontradoException.class, () -> servico.obterPorId(999L));

        assertEquals("Empresa de id 999 nao foi encontrado", excecao.getMessage());
    }

    @Test
    @DisplayName("atualizar: delega ao EmpresaAtualizador e salva")
    void atualizarDelegaAoAtualizador() {
        Empresa existente = empresa(1L, "Antiga");
        Empresa atualizacao = empresa(1L, "Nova");
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
                IllegalArgumentException.class, () -> servico.atualizar(empresa(null, "Nova")));

        assertEquals("O id da empresa e obrigatorio para atualizacao", excecao.getMessage());
        verify(repositorio, never()).save(any(Empresa.class));
    }

    @Test
    @DisplayName("excluir: limpa o vinculo de cada usuario e depois apaga a empresa")
    void excluirLimpaUsuarios() {
        Empresa existente = empresa(1L, "Loja");
        Usuario primeiro = usuario(5L, existente);
        Usuario segundo = usuario(6L, existente);
        existente.getUsuarios().add(primeiro);
        existente.getUsuarios().add(segundo);
        when(repositorio.findById(1L)).thenReturn(Optional.of(existente));

        servico.excluir(1L);

        assertNull(primeiro.getEmpresa());
        assertNull(segundo.getEmpresa());
        InOrder ordem = inOrder(repositorio);
        ordem.verify(repositorio).findById(1L);
        ordem.verify(repositorio).delete(existente);
    }

    @Test
    @DisplayName("excluir: lanca RecursoNaoEncontrado quando o id nao existe")
    void excluirInexistente() {
        when(repositorio.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> servico.excluir(999L));

        verify(repositorio, never()).delete(any(Empresa.class));
    }

    @Test
    @DisplayName("listarUsuarios: devolve os usuarios da empresa")
    void listarUsuarios() {
        Empresa existente = empresa(1L, "Loja");
        Usuario vinculado = usuario(5L, existente);
        existente.getUsuarios().add(vinculado);
        when(repositorio.findById(1L)).thenReturn(Optional.of(existente));

        assertSame(vinculado, servico.listarUsuarios(1L).get(0));
    }

    @Test
    @DisplayName("vincular: liga o usuario livre a empresa")
    void vincularUsuarioLivre() {
        Empresa existente = empresa(1L, "Loja");
        Usuario livre = usuario(5L, null);
        when(repositorio.findById(1L)).thenReturn(Optional.of(existente));
        when(usuarioRepositorio.findById(5L)).thenReturn(Optional.of(livre));

        servico.vincularUsuario(1L, 5L);

        assertSame(existente, livre.getEmpresa());
        verify(usuarioRepositorio).save(livre);
    }

    @Test
    @DisplayName("vincular: repetir o vinculo com a mesma empresa nao muda nada")
    void vincularMesmaEmpresa() {
        Empresa existente = empresa(200L, "Loja");
        Usuario vinculado = usuario(5L, empresa(200L, "Loja"));
        Empresa original = vinculado.getEmpresa();
        when(repositorio.findById(200L)).thenReturn(Optional.of(existente));
        when(usuarioRepositorio.findById(5L)).thenReturn(Optional.of(vinculado));

        servico.vincularUsuario(200L, 5L);

        assertSame(original, vinculado.getEmpresa());
        verify(usuarioRepositorio, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("vincular: lanca Conflito quando o usuario e de outra empresa")
    void vincularUsuarioDeOutraEmpresa() {
        Empresa outra = empresa(2L, "Outra");
        Usuario vinculado = usuario(5L, outra);
        when(repositorio.findById(1L)).thenReturn(Optional.of(empresa(1L, "Loja")));
        when(usuarioRepositorio.findById(5L)).thenReturn(Optional.of(vinculado));

        ConflitoException excecao = assertThrows(ConflitoException.class,
                () -> servico.vincularUsuario(1L, 5L));

        assertEquals("O usuario de id 5 ja esta vinculado a empresa de id 2. "
                + "Desvincule-o antes de vincular a outra empresa", excecao.getMessage());
        assertSame(outra, vinculado.getEmpresa());
    }

    @Test
    @DisplayName("vincular: lanca RecursoNaoEncontrado quando a empresa ou o usuario nao existem")
    void vincularInexistentes() {
        when(repositorio.findById(999L)).thenReturn(Optional.empty());
        when(repositorio.findById(1L)).thenReturn(Optional.of(empresa(1L, "Loja")));
        when(usuarioRepositorio.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> servico.vincularUsuario(999L, 5L));
        assertThrows(RecursoNaoEncontradoException.class, () -> servico.vincularUsuario(1L, 999L));
    }

    @Test
    @DisplayName("desvincular: tira o vinculo e mantem o usuario")
    void desvincular() {
        Empresa existente = empresa(1L, "Loja");
        Usuario vinculado = usuario(5L, existente);
        when(repositorio.findById(1L)).thenReturn(Optional.of(existente));
        when(usuarioRepositorio.findById(5L)).thenReturn(Optional.of(vinculado));

        servico.desvincularUsuario(1L, 5L);

        assertNull(vinculado.getEmpresa());
        verify(usuarioRepositorio).save(vinculado);
        verify(usuarioRepositorio, never()).delete(any(Usuario.class));
    }

    @Test
    @DisplayName("desvincular: lanca RecursoNaoEncontrado quando o usuario e de outra empresa")
    void desvincularUsuarioDeOutraEmpresa() {
        Empresa outra = empresa(2L, "Outra");
        Usuario vinculado = usuario(5L, outra);
        when(repositorio.findById(1L)).thenReturn(Optional.of(empresa(1L, "Loja")));
        when(usuarioRepositorio.findById(5L)).thenReturn(Optional.of(vinculado));

        RecursoNaoEncontradoException excecao = assertThrows(RecursoNaoEncontradoException.class,
                () -> servico.desvincularUsuario(1L, 5L));

        assertEquals("O usuario de id 5 nao esta vinculado a empresa de id 1", excecao.getMessage());
        assertSame(outra, vinculado.getEmpresa());
    }

    @Test
    @DisplayName("desvincular: lanca RecursoNaoEncontrado quando o usuario nao tem empresa")
    void desvincularUsuarioLivre() {
        when(repositorio.findById(1L)).thenReturn(Optional.of(empresa(1L, "Loja")));
        when(usuarioRepositorio.findById(5L)).thenReturn(Optional.of(usuario(5L, null)));

        assertThrows(RecursoNaoEncontradoException.class, () -> servico.desvincularUsuario(1L, 5L));

        verify(usuarioRepositorio, never()).save(any(Usuario.class));
    }
}
