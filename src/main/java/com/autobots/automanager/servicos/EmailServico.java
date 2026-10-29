package com.autobots.automanager.servicos;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.autobots.automanager.entidades.Email;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.excecoes.RecursoNaoEncontradoException;
import com.autobots.automanager.modelo.EmailAtualizador;
import com.autobots.automanager.repositorios.EmailRepositorio;
import com.autobots.automanager.repositorios.UsuarioRepositorio;

@Service
public class EmailServico {

    @Autowired
    private EmailRepositorio repositorio;
    @Autowired
    private UsuarioRepositorio usuarioRepositorio;
    @Autowired
    private EmailAtualizador atualizador;

    public List<Email> listarPorUsuario(long usuarioId) {
        return obterUsuario(usuarioId).getEmails();
    }

    @Transactional
    public Email criar(long usuarioId, Email email) {
        Usuario usuario = obterUsuario(usuarioId);
        email.setId(null);
        Email criado = repositorio.save(email);
        usuario.getEmails().add(criado);
        usuarioRepositorio.save(usuario);
        return criado;
    }

    public Email obterPorId(long id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Email", id));
    }

    @Transactional
    public Email atualizar(long id, Email atualizacao) {
        Email email = obterPorId(id);
        atualizador.atualizar(email, atualizacao);
        return repositorio.save(email);
    }

    @Transactional
    public void excluir(long id) {
        repositorio.delete(obterPorId(id));
    }

    private Usuario obterUsuario(long usuarioId) {
        return usuarioRepositorio.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario", usuarioId));
    }
}
