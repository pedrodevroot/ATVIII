package com.autobots.automanager.servicos;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.autobots.automanager.entidades.Empresa;
import com.autobots.automanager.entidades.Servico;
import com.autobots.automanager.excecoes.ConflitoException;
import com.autobots.automanager.excecoes.RecursoNaoEncontradoException;
import com.autobots.automanager.modelo.ServicoAtualizador;
import com.autobots.automanager.repositorios.EmpresaRepositorio;
import com.autobots.automanager.repositorios.ServicoRepositorio;
import com.autobots.automanager.repositorios.VendaRepositorio;

@Service
public class ServicoServico {

    @Autowired
    private ServicoRepositorio repositorio;
    @Autowired
    private EmpresaRepositorio empresaRepositorio;
    @Autowired
    private VendaRepositorio vendaRepositorio;
    @Autowired
    private ServicoAtualizador atualizador;

    public List<Servico> listarPorEmpresa(long empresaId) {
        return obterEmpresa(empresaId).getServicos();
    }

    @Transactional
    public Servico criar(long empresaId, Servico servico) {
        Empresa empresa = obterEmpresa(empresaId);
        servico.setId(null);
        Servico criado = repositorio.save(servico);
        empresa.getServicos().add(criado);
        empresaRepositorio.save(empresa);
        return criado;
    }

    public Servico obterPorId(long id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Servico", id));
    }

    @Transactional
    public Servico atualizar(long id, Servico atualizacao) {
        Servico servico = obterPorId(id);
        atualizador.atualizar(servico, atualizacao);
        return repositorio.save(servico);
    }

    @Transactional
    public void excluir(long id) {
        Servico servico = obterPorId(id);
        if (vendaRepositorio.existsByServicosId(id)) {
            throw new ConflitoException("O servico de id " + id
                    + " possui vendas registradas e nao pode ser excluido");
        }
        repositorio.delete(servico);
    }

    private Empresa obterEmpresa(long empresaId) {
        return empresaRepositorio.findById(empresaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Empresa", empresaId));
    }
}
