package com.autobots.automanager.servicos;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.autobots.automanager.entidades.Documento;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.excecoes.RecursoNaoEncontradoException;
import com.autobots.automanager.modelo.DocumentoAtualizador;
import com.autobots.automanager.repositorios.DocumentoRepositorio;
import com.autobots.automanager.repositorios.UsuarioRepositorio;

@Service
public class DocumentoServico {

    @Autowired
    private DocumentoRepositorio repositorio;
    @Autowired
    private UsuarioRepositorio usuarioRepositorio;
    @Autowired
    private DocumentoAtualizador atualizador;

    public List<Documento> listarPorUsuario(long usuarioId) {
        return obterUsuario(usuarioId).getDocumentos();
    }

    @Transactional
    public Documento criar(long usuarioId, Documento documento) {
        Usuario usuario = obterUsuario(usuarioId);
        documento.setId(null);
        Documento criado = repositorio.save(documento);
        usuario.getDocumentos().add(criado);
        usuarioRepositorio.save(usuario);
        return criado;
    }

    public Documento obterPorId(long id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Documento", id));
    }

    @Transactional
    public Documento atualizar(long id, Documento atualizacao) {
        Documento documento = obterPorId(id);
        atualizador.atualizar(documento, atualizacao);
        return repositorio.save(documento);
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
