package com.autobots.automanager.servicos;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.autobots.automanager.entidades.Cliente;
import com.autobots.automanager.entidades.Telefone;
import com.autobots.automanager.excecoes.RecursoNaoEncontradoException;
import com.autobots.automanager.modelo.TelefoneAtualizador;
import com.autobots.automanager.repositorios.ClienteRepositorio;
import com.autobots.automanager.repositorios.TelefoneRepositorio;

@Service
public class TelefoneServico {

    @Autowired
    private TelefoneRepositorio repositorio;
    @Autowired
    private ClienteRepositorio clienteRepositorio;
    @Autowired
    private TelefoneAtualizador atualizador;

    public List<Telefone> listarPorCliente(long clienteId) {
        return obterCliente(clienteId).getTelefones();
    }

    @Transactional
    public Telefone criar(long clienteId, Telefone telefone) {
        Cliente cliente = obterCliente(clienteId);
        telefone.setId(null);
        Telefone criado = repositorio.save(telefone);
        cliente.getTelefones().add(criado);
        clienteRepositorio.save(cliente);
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

    private Cliente obterCliente(long clienteId) {
        return clienteRepositorio.findById(clienteId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente", clienteId));
    }
}
