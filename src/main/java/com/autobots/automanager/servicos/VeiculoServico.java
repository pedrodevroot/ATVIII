package com.autobots.automanager.servicos;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.entidades.Veiculo;
import com.autobots.automanager.excecoes.ConflitoException;
import com.autobots.automanager.excecoes.RecursoNaoEncontradoException;
import com.autobots.automanager.modelo.VeiculoAtualizador;
import com.autobots.automanager.repositorios.UsuarioRepositorio;
import com.autobots.automanager.repositorios.VeiculoRepositorio;
import com.autobots.automanager.repositorios.VendaRepositorio;

@Service
public class VeiculoServico {

    @Autowired
    private VeiculoRepositorio repositorio;
    @Autowired
    private UsuarioRepositorio usuarioRepositorio;
    @Autowired
    private VendaRepositorio vendaRepositorio;
    @Autowired
    private VeiculoAtualizador atualizador;

    public List<Veiculo> listarPorUsuario(long usuarioId) {
        return obterUsuario(usuarioId).getVeiculos();
    }

    @Transactional
    public Veiculo criar(long usuarioId, Veiculo veiculo) {
        Usuario usuario = obterUsuario(usuarioId);
        veiculo.setId(null);
        veiculo.setProprietario(usuario);
        return repositorio.save(veiculo);
    }

    public Veiculo obterPorId(long id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Veiculo", id));
    }

    @Transactional
    public Veiculo atualizar(long id, Veiculo atualizacao) {
        Veiculo veiculo = obterPorId(id);
        atualizador.atualizar(veiculo, atualizacao);
        return repositorio.save(veiculo);
    }

    @Transactional
    public void excluir(long id) {
        Veiculo veiculo = obterPorId(id);
        if (vendaRepositorio.existsByVeiculoId(id)) {
            throw new ConflitoException("O veiculo de id " + id
                    + " possui vendas registradas e nao pode ser excluido");
        }
        repositorio.delete(veiculo);
    }

    private Usuario obterUsuario(long usuarioId) {
        return usuarioRepositorio.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario", usuarioId));
    }
}
