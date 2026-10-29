package com.autobots.automanager.servicos;

import java.util.Calendar;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.autobots.automanager.dtos.CredencialAtualizacaoDto;
import com.autobots.automanager.entidades.Credencial;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.excecoes.RecursoNaoEncontradoException;
import com.autobots.automanager.modelo.CredencialAtualizador;
import com.autobots.automanager.repositorios.CredencialRepositorio;
import com.autobots.automanager.repositorios.UsuarioRepositorio;

@Service
public class CredencialServico {

    @Autowired
    private CredencialRepositorio repositorio;
    @Autowired
    private UsuarioRepositorio usuarioRepositorio;
    @Autowired
    private CredencialAtualizador atualizador;

    public List<Credencial> listarPorUsuario(long usuarioId) {
        return obterUsuario(usuarioId).getCredenciais();
    }

    @Transactional
    public Credencial criar(long usuarioId, Credencial credencial) {
        Usuario usuario = obterUsuario(usuarioId);
        credencial.setId(null);
        credencial.setCriacao(Calendar.getInstance().getTime());
        credencial.setInativo(false);
        Credencial criada = repositorio.save(credencial);
        usuario.getCredenciais().add(criada);
        usuarioRepositorio.save(usuario);
        return criada;
    }

    public Credencial obterPorId(long id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Credencial", id));
    }

    @Transactional
    public Credencial atualizar(long id, CredencialAtualizacaoDto atualizacao) {
        Credencial credencial = obterPorId(id);
        atualizador.atualizar(credencial, atualizacao);
        return repositorio.save(credencial);
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
