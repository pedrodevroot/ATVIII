package com.autobots.automanager.servicos;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.autobots.automanager.entidades.Cliente;
import com.autobots.automanager.entidades.Endereco;
import com.autobots.automanager.excecoes.RecursoNaoEncontradoException;
import com.autobots.automanager.modelo.EnderecoAtualizador;
import com.autobots.automanager.repositorios.ClienteRepositorio;
import com.autobots.automanager.repositorios.EnderecoRepositorio;

@Service
public class EnderecoServico {

    @Autowired
    private EnderecoRepositorio repositorio;
    @Autowired
    private ClienteRepositorio clienteRepositorio;
    @Autowired
    private EnderecoAtualizador atualizador;

    public List<Endereco> listarPorCliente(long clienteId) {
        return obterCliente(clienteId).getEnderecos();
    }

    @Transactional
    public Endereco criar(long clienteId, Endereco endereco) {
        Cliente cliente = obterCliente(clienteId);
        endereco.setId(null);
        Endereco criado = repositorio.save(endereco);
        cliente.getEnderecos().add(criado);
        clienteRepositorio.save(cliente);
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
        repositorio.delete(obterPorId(id));
    }

    private Cliente obterCliente(long clienteId) {
        return clienteRepositorio.findById(clienteId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente", clienteId));
    }
}
