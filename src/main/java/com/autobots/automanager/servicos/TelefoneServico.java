package com.autobots.automanager.servicos;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.autobots.automanager.entidades.Telefone;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.excecoes.RecursoNaoEncontradoException;
import com.autobots.automanager.modelo.TelefoneAtualizador;
import com.autobots.automanager.repositorios.TelefoneRepositorio;
import com.autobots.automanager.repositorios.UsuarioRepositorio;

@Service
public class TelefoneServico {

    @Autowired
    private TelefoneRepositorio repositorio;
    @Autowired
    private UsuarioRepositorio usuarioRepositorio;
    @Autowired
    private TelefoneAtualizador atualizador;

    public List<Telefone> listarPorUsuario(long usuarioId) {
        return obterUsuario(usuarioId).getTelefones();
    }

    @Transactional
    public Telefone criar(long usuarioId, Telefone telefone) {
        Usuario usuario = obterUsuario(usuarioId);
        telefone.setId(null);
        Telefone criado = repositorio.save(telefone);
        usuario.getTelefones().add(criado);
        usuarioRepositorio.save(usuario);
        return criado;
    }

    public Telefone obterPorId(long id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Telefone", id));
    }

    @Transactional
    public Telefone atualizar(long id, Telefone atualizacao) {
        Telefone telefone = obterPorId(id);
        atualizador.atualizar(telefone, atualizacao);
        return repositorio.save(telefone);
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
