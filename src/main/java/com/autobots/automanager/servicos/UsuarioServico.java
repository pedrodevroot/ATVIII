package com.autobots.automanager.servicos;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.autobots.automanager.entidades.Credencial;
import com.autobots.automanager.entidades.Documento;
import com.autobots.automanager.entidades.Email;
import com.autobots.automanager.entidades.Telefone;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.enumeracoes.PerfilUsuario;
import com.autobots.automanager.excecoes.ConflitoException;
import com.autobots.automanager.excecoes.RecursoNaoEncontradoException;
import com.autobots.automanager.modelo.UsuarioAtualizador;
import com.autobots.automanager.modelo.VerificadorVinculo;
import com.autobots.automanager.repositorios.UsuarioRepositorio;
import com.autobots.automanager.repositorios.VendaRepositorio;

@Service
public class UsuarioServico {
    private VerificadorVinculo verificador = new VerificadorVinculo();

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
        verificarVinculos(usuario, atualizacao);
        verificarPerfis(usuario, atualizacao);
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

    private void verificarPerfis(Usuario usuario, Usuario atualizacao) {
        if (atualizacao.getPerfis().isEmpty()) {
            return;
        }
        Long id = usuario.getId();
        if (removeuPerfil(usuario, atualizacao, PerfilUsuario.CLIENTE)
                && vendaRepositorio.existsByClienteId(id)) {
            throw new ConflitoException("O usuario de id " + id
                    + " possui vendas como cliente e precisa manter o perfil CLIENTE");
        }
        if (removeuPerfil(usuario, atualizacao, PerfilUsuario.FUNCIONARIO)
                && vendaRepositorio.existsByFuncionarioId(id)) {
            throw new ConflitoException("O usuario de id " + id
                    + " possui vendas como funcionario e precisa manter o perfil FUNCIONARIO");
        }
        if (removeuPerfil(usuario, atualizacao, PerfilUsuario.FORNECEDOR)
                && !usuario.getMercadorias().isEmpty()) {
            throw new ConflitoException("O usuario de id " + id
                    + " fornece mercadorias e precisa manter o perfil FORNECEDOR");
        }
    }

    private boolean removeuPerfil(Usuario usuario, Usuario atualizacao, PerfilUsuario perfil) {
        return usuario.getPerfis().contains(perfil) && !atualizacao.getPerfis().contains(perfil);
    }

    private void verificarVinculos(Usuario usuario, Usuario atualizacao) {
        Long id = usuario.getId();
        if (atualizacao.getEndereco() != null && usuario.getEndereco() == null) {
            throw new RecursoNaoEncontradoException("O usuario de id " + id
                    + " nao possui endereco. Para cadastrar, use POST /usuario/" + id + "/endereco");
        }
        String dono = "ao usuario de id " + id;
        verificador.verificar(usuario.getDocumentos(), atualizacao.getDocumentos(),
                Documento::getId, "O documento", dono);
        verificador.verificar(usuario.getTelefones(), atualizacao.getTelefones(),
                Telefone::getId, "O telefone", dono);
        verificador.verificar(usuario.getEmails(), atualizacao.getEmails(),
                Email::getId, "O e-mail", dono);
    }
}
