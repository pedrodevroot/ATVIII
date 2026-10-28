package com.autobots.automanager.servicos;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.autobots.automanager.entidades.Cliente;
import com.autobots.automanager.entidades.Documento;
import com.autobots.automanager.excecoes.RecursoNaoEncontradoException;
import com.autobots.automanager.modelo.DocumentoAtualizador;
import com.autobots.automanager.repositorios.ClienteRepositorio;
import com.autobots.automanager.repositorios.DocumentoRepositorio;

@Service
public class DocumentoServico {

    @Autowired
    private DocumentoRepositorio repositorio;
    @Autowired
    private ClienteRepositorio clienteRepositorio;
    @Autowired
    private DocumentoAtualizador atualizador;

    public List<Documento> listarPorCliente(long clienteId) {
        return obterCliente(clienteId).getDocumentos();
    }

    @Transactional
    public Documento criar(long clienteId, Documento documento) {
        Cliente cliente = obterCliente(clienteId);
        documento.setId(null);
        Documento criado = repositorio.save(documento);
        cliente.getDocumentos().add(criado);
        clienteRepositorio.save(cliente);
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

    private Cliente obterCliente(long clienteId) {
        return clienteRepositorio.findById(clienteId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente", clienteId));
    }
}
