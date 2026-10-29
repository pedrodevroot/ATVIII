package com.autobots.automanager.servicos;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.autobots.automanager.entidades.Endereco;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.excecoes.ConflitoException;
import com.autobots.automanager.excecoes.RecursoNaoEncontradoException;
import com.autobots.automanager.modelo.EnderecoAtualizador;
import com.autobots.automanager.repositorios.EnderecoRepositorio;
import com.autobots.automanager.repositorios.UsuarioRepositorio;

@Service
public class EnderecoServico {

    @Autowired
    private EnderecoRepositorio repositorio;
    @Autowired
    private UsuarioRepositorio usuarioRepositorio;
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

    @Transactional
    public void excluir(long id) {
        Endereco endereco = obterPorId(id);
        usuarioRepositorio.findByEnderecoId(id).ifPresentOrElse(
                usuario -> usuario.setEndereco(null),
                () -> repositorio.delete(endereco));
    }

    private Usuario obterUsuario(long usuarioId) {
        return usuarioRepositorio.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario", usuarioId));
    }
}
