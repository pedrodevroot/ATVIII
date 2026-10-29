package com.autobots.automanager.servicos;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.autobots.automanager.entidades.Credencial;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.excecoes.ConflitoException;
import com.autobots.automanager.excecoes.RecursoNaoEncontradoException;
import com.autobots.automanager.modelo.UsuarioAtualizador;
import com.autobots.automanager.repositorios.UsuarioRepositorio;
import com.autobots.automanager.repositorios.VendaRepositorio;

@Service
public class UsuarioServico {

    @Autowired
    private UsuarioRepositorio repositorio;
    @Autowired
    private VendaRepositorio vendaRepositorio;
    @Autowired
    private UsuarioAtualizador atualizador;

    @Transactional
    public Usuario cadastrar(Usuario usuario) {
        usuario.setId(null);
        Date agora = Calendar.getInstance().getTime();
        usuario.setDataCadastro(agora);
        for (Credencial credencial : usuario.getCredenciais()) {
            credencial.setCriacao(agora);
        }
        return repositorio.save(usuario);
    }

    public List<Usuario> listar() {
        return repositorio.findAll(Sort.by("id"));
    }

    public Usuario obterPorId(long id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario", id));
    }

    @Transactional
    public Usuario atualizar(Usuario atualizacao) {
        if (atualizacao.getId() == null) {
            throw new IllegalArgumentException(
                    "O id do usuario e obrigatorio para atualizacao");
        }
        Usuario usuario = obterPorId(atualizacao.getId());
        atualizador.atualizar(usuario, atualizacao);
        return repositorio.save(usuario);
    }

    @Transactional
    public void excluir(long id) {
        Usuario usuario = obterPorId(id);
        if (vendaRepositorio.existsByClienteIdOrFuncionarioId(id, id)) {
            throw new ConflitoException("O usuario de id " + id
                    + " possui vendas registradas e nao pode ser excluido");
        }
        repositorio.delete(usuario);
    }
}
