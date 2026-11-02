package com.autobots.automanager.servicos;

import java.util.Calendar;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.autobots.automanager.entidades.Empresa;
import com.autobots.automanager.entidades.Telefone;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.excecoes.ConflitoException;
import com.autobots.automanager.excecoes.RecursoNaoEncontradoException;
import com.autobots.automanager.modelo.EmpresaAtualizador;
import com.autobots.automanager.modelo.VerificadorVinculo;
import com.autobots.automanager.repositorios.EmpresaRepositorio;
import com.autobots.automanager.repositorios.UsuarioRepositorio;

@Service
public class EmpresaServico {
    private VerificadorVinculo verificador = new VerificadorVinculo();

    @Autowired
    private EmpresaRepositorio repositorio;
    @Autowired
    private UsuarioRepositorio usuarioRepositorio;
    @Autowired
    private EmpresaAtualizador atualizador;

    @Transactional
    public Empresa cadastrar(Empresa empresa) {
        empresa.setId(null);
        empresa.setCadastro(Calendar.getInstance().getTime());
        return repositorio.save(empresa);
    }

    public List<Empresa> listar() {
        return repositorio.findAll(Sort.by("id"));
    }

    public Empresa obterPorId(long id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Empresa", id));
    }

    @Transactional
    public Empresa atualizar(Empresa atualizacao) {
        if (atualizacao.getId() == null) {
            throw new IllegalArgumentException(
                    "O id da empresa e obrigatorio para atualizacao");
        }
        Empresa empresa = obterPorId(atualizacao.getId());
        verificarVinculos(empresa, atualizacao);
        atualizador.atualizar(empresa, atualizacao);
        return repositorio.save(empresa);
    }

    @Transactional
    public void excluir(long id) {
        Empresa empresa = obterPorId(id);
        for (Usuario usuario : empresa.getUsuarios()) {
            usuario.setEmpresa(null);
        }
        empresa.getVendas().clear();
        repositorio.saveAndFlush(empresa);
        repositorio.delete(empresa);
    }

    public List<Usuario> listarUsuarios(long empresaId) {
        return obterPorId(empresaId).getUsuarios();
    }

    @Transactional
    public void vincularUsuario(long empresaId, long usuarioId) {
        Empresa empresa = obterPorId(empresaId);
        Usuario usuario = obterUsuario(usuarioId);
        Empresa vinculoAtual = usuario.getEmpresa();
        if (vinculoAtual != null) {
            if (vinculoAtual.getId().equals(empresaId)) {
                return;
            }
            throw new ConflitoException("O usuario de id " + usuarioId
                    + " ja esta vinculado a empresa de id " + vinculoAtual.getId()
                    + ". Desvincule-o antes de vincular a outra empresa");
        }
        usuario.setEmpresa(empresa);
        usuarioRepositorio.save(usuario);
    }

    @Transactional
    public void desvincularUsuario(long empresaId, long usuarioId) {
        obterPorId(empresaId);
        Usuario usuario = obterUsuario(usuarioId);
        if (usuario.getEmpresa() == null || !usuario.getEmpresa().getId().equals(empresaId)) {
            throw new RecursoNaoEncontradoException("O usuario de id " + usuarioId
                    + " nao esta vinculado a empresa de id " + empresaId);
        }
        usuario.setEmpresa(null);
        usuarioRepositorio.save(usuario);
    }

    private void verificarVinculos(Empresa empresa, Empresa atualizacao) {
        Long id = empresa.getId();
        if (atualizacao.getEndereco() != null && empresa.getEndereco() == null) {
            throw new RecursoNaoEncontradoException("A empresa de id " + id
                    + " nao possui endereco. Para cadastrar, use POST /empresa/" + id + "/endereco");
        }
        verificador.verificar(empresa.getTelefones(), atualizacao.getTelefones(),
                Telefone::getId, "O telefone", "a empresa de id " + id);
    }

    private Usuario obterUsuario(long usuarioId) {
        return usuarioRepositorio.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario", usuarioId));
    }
}
