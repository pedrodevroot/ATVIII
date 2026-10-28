package com.autobots.automanager.servicos;

import java.util.Calendar;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.autobots.automanager.entidades.Cliente;
import com.autobots.automanager.excecoes.RecursoNaoEncontradoException;
import com.autobots.automanager.modelo.ClienteAtualizador;
import com.autobots.automanager.repositorios.ClienteRepositorio;

@Service
public class ClienteServico {

    @Autowired
    private ClienteRepositorio repositorio;
    @Autowired
    private ClienteAtualizador atualizador;

    @Transactional
    public Cliente cadastrar(Cliente cliente) {

        cliente.setId(null);

        if (cliente.getDataCadastro() == null) {
            cliente.setDataCadastro(Calendar.getInstance().getTime());
        }

        return repositorio.save(cliente);
    }

    public List<Cliente> listar() {
        return repositorio.findAll(Sort.by("id"));
    }

    public Cliente obterPorId(long id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente", id));
    }

    @Transactional
    public Cliente atualizar(Cliente atualizacao) {
        if (atualizacao.getId() == null) {
            throw new IllegalArgumentException(
                    "O id do cliente e obrigatorio para atualizacao");
        }
        Cliente cliente = repositorio.findById(atualizacao.getId())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Cliente", atualizacao.getId()));
        atualizador.atualizar(cliente, atualizacao);
        return repositorio.save(cliente);
    }

    @Transactional
    public void excluir(long id) {
        Cliente cliente = repositorio.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente", id));
        repositorio.delete(cliente);
    }
}
