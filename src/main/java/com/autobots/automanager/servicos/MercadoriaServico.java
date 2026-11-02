package com.autobots.automanager.servicos;

import java.util.Calendar;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

@Service
public class MercadoriaServico {

    @Autowired
    private MercadoriaRepositorio repositorio;
    @Autowired
    private EmpresaRepositorio empresaRepositorio;
    @Autowired
    private UsuarioRepositorio usuarioRepositorio;
    @Autowired
    private VendaRepositorio vendaRepositorio;
    @Autowired
    private MercadoriaAtualizador atualizador;

    public List<Mercadoria> listarPorEmpresa(long empresaId) {
        return obterEmpresa(empresaId).getMercadorias();
    }

    @Transactional
    public Mercadoria criar(long empresaId, Mercadoria mercadoria) {
        Empresa empresa = obterEmpresa(empresaId);
        validarDatas(mercadoria);
        mercadoria.setId(null);
        mercadoria.setCadastro(Calendar.getInstance().getTime());
        Mercadoria criada = repositorio.save(mercadoria);
        empresa.getMercadorias().add(criada);
        empresaRepositorio.save(empresa);
        return criada;
    }

    public Mercadoria obterPorId(long id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Mercadoria", id));
    }

    @Transactional
    public Mercadoria atualizar(long id, MercadoriaAtualizacaoDto atualizacao) {
        Mercadoria mercadoria = obterPorId(id);
        atualizador.atualizar(mercadoria, atualizacao);
        validarDatas(mercadoria);
        return repositorio.save(mercadoria);
    }

    @Transactional
    public void excluir(long id) {
        Mercadoria mercadoria = obterPorId(id);
        if (vendaRepositorio.existsByItensMercadoriaId(id)) {
            throw new ConflitoException("A mercadoria de id " + id
                    + " possui vendas registradas e nao pode ser excluida");
        }
        repositorio.delete(mercadoria);
    }

    public List<Mercadoria> listarPorFornecedor(long usuarioId) {
        return obterUsuario(usuarioId).getMercadorias();
    }

    @Transactional
    public void vincularFornecedor(long usuarioId, long mercadoriaId) {
        Usuario usuario = obterUsuario(usuarioId);
        if (!usuario.getPerfis().contains(PerfilUsuario.FORNECEDOR)) {
            throw new IllegalArgumentException("O usuario de id " + usuarioId
                    + " nao possui o perfil FORNECEDOR");
        }
        Mercadoria mercadoria = obterPorId(mercadoriaId);
        Optional<Usuario> fornecedorAtual = usuarioRepositorio.findByMercadoriasId(mercadoriaId);
        if (fornecedorAtual.isPresent()) {
            if (fornecedorAtual.get().getId().equals(usuarioId)) {
                return;
            }
            throw new ConflitoException("A mercadoria de id " + mercadoriaId
                    + " ja esta vinculada ao fornecedor de id " + fornecedorAtual.get().getId()
                    + ". Desvincule-a antes de vincular a outro fornecedor");
        }
        usuario.getMercadorias().add(mercadoria);
        usuarioRepositorio.save(usuario);
    }

    @Transactional
    public void desvincularFornecedor(long usuarioId, long mercadoriaId) {
        Usuario usuario = obterUsuario(usuarioId);
        if (!usuario.getMercadorias().removeIf(mercadoria -> mercadoria.getId().equals(mercadoriaId))) {
            throw new RecursoNaoEncontradoException("A mercadoria de id " + mercadoriaId
                    + " nao esta vinculada ao usuario de id " + usuarioId);
        }
        usuarioRepositorio.save(usuario);
    }

    private void validarDatas(Mercadoria mercadoria) {
        if (mercadoria.getValidade() != null
                && mercadoria.getValidade().before(mercadoria.getFabricacao())) {
            throw new IllegalArgumentException(
                    "A data de validade nao pode ser anterior a data de fabricacao");
        }
    }

    private Empresa obterEmpresa(long empresaId) {
        return empresaRepositorio.findById(empresaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Empresa", empresaId));
    }

    private Usuario obterUsuario(long usuarioId) {
        return usuarioRepositorio.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario", usuarioId));
    }
}
