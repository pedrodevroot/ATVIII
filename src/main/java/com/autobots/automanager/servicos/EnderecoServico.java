package com.autobots.automanager.servicos;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.autobots.automanager.entidades.Empresa;
import com.autobots.automanager.entidades.Endereco;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.excecoes.ConflitoException;
import com.autobots.automanager.excecoes.RecursoNaoEncontradoException;
import com.autobots.automanager.modelo.EnderecoAtualizador;
import com.autobots.automanager.repositorios.EmpresaRepositorio;
import com.autobots.automanager.repositorios.EnderecoRepositorio;
import com.autobots.automanager.repositorios.UsuarioRepositorio;

@Service
public class EnderecoServico {

    @Autowired
    private EnderecoRepositorio repositorio;
    @Autowired
    private UsuarioRepositorio usuarioRepositorio;
    @Autowired
    private EmpresaRepositorio empresaRepositorio;
    @Autowired
    private EnderecoAtualizador atualizador;

    public Endereco obterPorUsuario(long usuarioId) {
        Endereco endereco = obterUsuario(usuarioId).getEndereco();
        if (endereco == null) {
            throw new RecursoNaoEncontradoException(
                    "O usuario de id " + usuarioId + " nao possui endereco");
        }
        return endereco;
    }

    @Transactional
    public Endereco criar(long usuarioId, Endereco endereco) {
        Usuario usuario = obterUsuario(usuarioId);
        if (usuario.getEndereco() != null) {
            throw new ConflitoException("O usuario de id " + usuarioId
                    + " ja possui endereco. Para altera-lo, use PUT /endereco/"
                    + usuario.getEndereco().getId());
        }
        endereco.setId(null);
        Endereco criado = repositorio.save(endereco);
        usuario.setEndereco(criado);
        usuarioRepositorio.save(usuario);
        return criado;
    }

    public Endereco obterPorId(long id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Endereco", id));
    }

    @Transactional
    public Endereco atualizar(long id, Endereco atualizacao) {
        Endereco endereco = obterPorId(id);
        atualizador.atualizar(endereco, atualizacao);
        return repositorio.save(endereco);
    }

    public Endereco obterPorEmpresa(long empresaId) {
        Endereco endereco = obterEmpresa(empresaId).getEndereco();
        if (endereco == null) {
            throw new RecursoNaoEncontradoException(
                    "A empresa de id " + empresaId + " nao possui endereco");
        }
        return endereco;
    }

    @Transactional
    public Endereco criarNaEmpresa(long empresaId, Endereco endereco) {
        Empresa empresa = obterEmpresa(empresaId);
        if (empresa.getEndereco() != null) {
            throw new ConflitoException("A empresa de id " + empresaId
                    + " ja possui endereco. Para altera-lo, use PUT /endereco/"
                    + empresa.getEndereco().getId());
        }
        endereco.setId(null);
        Endereco criado = repositorio.save(endereco);
        empresa.setEndereco(criado);
        empresaRepositorio.save(empresa);
        return criado;
    }

    @Transactional
    public void excluir(long id) {
        Endereco endereco = obterPorId(id);
        Optional<Usuario> usuario = usuarioRepositorio.findByEnderecoId(id);
        Optional<Empresa> empresa = empresaRepositorio.findByEnderecoId(id);
        if (usuario.isPresent()) {
            usuario.get().setEndereco(null);
        } else if (empresa.isPresent()) {
            empresa.get().setEndereco(null);
        } else {
            repositorio.delete(endereco);
        }
    }

    private Usuario obterUsuario(long usuarioId) {
        return usuarioRepositorio.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario", usuarioId));
    }

    private Empresa obterEmpresa(long empresaId) {
        return empresaRepositorio.findById(empresaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Empresa", empresaId));
    }
}
