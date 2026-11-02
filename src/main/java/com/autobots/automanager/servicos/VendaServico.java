package com.autobots.automanager.servicos;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.autobots.automanager.dtos.VendaAtualizacaoDto;
import com.autobots.automanager.dtos.VendaCadastroDto;
import com.autobots.automanager.entidades.Empresa;
import com.autobots.automanager.entidades.Mercadoria;
import com.autobots.automanager.entidades.Servico;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.entidades.Veiculo;
import com.autobots.automanager.entidades.Venda;
import com.autobots.automanager.enumeracoes.PerfilUsuario;
import com.autobots.automanager.excecoes.RecursoNaoEncontradoException;
import com.autobots.automanager.modelo.StringVerificadorNulo;
import com.autobots.automanager.repositorios.EmpresaRepositorio;
import com.autobots.automanager.repositorios.MercadoriaRepositorio;
import com.autobots.automanager.repositorios.ServicoRepositorio;
import com.autobots.automanager.repositorios.UsuarioRepositorio;
import com.autobots.automanager.repositorios.VeiculoRepositorio;
import com.autobots.automanager.repositorios.VendaRepositorio;

@Service
public class VendaServico {
    private StringVerificadorNulo verificador = new StringVerificadorNulo();

    @Autowired
    private VendaRepositorio repositorio;
    @Autowired
    private EmpresaRepositorio empresaRepositorio;
    @Autowired
    private UsuarioRepositorio usuarioRepositorio;
    @Autowired
    private VeiculoRepositorio veiculoRepositorio;
    @Autowired
    private MercadoriaRepositorio mercadoriaRepositorio;
    @Autowired
    private ServicoRepositorio servicoRepositorio;

    public List<Venda> listarPorEmpresa(long empresaId) {
        return obterEmpresa(empresaId).getVendas();
    }

    public List<Venda> listarPorUsuario(long usuarioId) {
        obterUsuario(usuarioId);
        return repositorio.findByClienteIdOrFuncionarioIdOrderById(usuarioId, usuarioId);
    }

    public List<Venda> listarPorVeiculo(long veiculoId) {
        return obterVeiculo(veiculoId).getVendas();
    }

    @Transactional
    public Venda criar(long empresaId, VendaCadastroDto dto) {
        Empresa empresa = obterEmpresa(empresaId);
        Venda venda = new Venda();
        venda.setIdentificacao(dto.getIdentificacao());
        venda.setCadastro(Calendar.getInstance().getTime());
        venda.setCliente(obterComPerfil(dto.getClienteId(), PerfilUsuario.CLIENTE));
        venda.setFuncionario(obterFuncionario(dto.getFuncionarioId(), empresa));
        if (dto.getVeiculoId() != null) {
            venda.setVeiculo(obterVeiculo(dto.getVeiculoId()));
        }
        venda.setMercadorias(obterMercadorias(dto.getMercadoriaIds(), empresa));
        venda.setServicos(obterServicos(dto.getServicoIds(), empresa));
        validar(venda);
        Venda criada = repositorio.save(venda);
        empresa.getVendas().add(criada);
        empresaRepositorio.save(empresa);
        return criada;
    }

    public Venda obterPorId(long id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Venda", id));
    }

    @Transactional
    public Venda atualizar(long id, VendaAtualizacaoDto dto) {
        Venda venda = obterPorId(id);
        Empresa empresa = empresaRepositorio.findByVendasId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "A venda de id " + id + " nao pertence a nenhuma empresa"));
        if (!verificador.verificar(dto.getIdentificacao())) {
            venda.setIdentificacao(dto.getIdentificacao());
        }
        if (dto.getClienteId() != null) {
            venda.setCliente(obterComPerfil(dto.getClienteId(), PerfilUsuario.CLIENTE));
        }
        if (dto.getFuncionarioId() != null) {
            venda.setFuncionario(obterFuncionario(dto.getFuncionarioId(), empresa));
        }
        if (dto.getVeiculoId() != null) {
            venda.setVeiculo(obterVeiculo(dto.getVeiculoId()));
        }
        if (dto.getMercadoriaIds() != null) {
            venda.setMercadorias(obterMercadorias(dto.getMercadoriaIds(), empresa));
        }
        if (dto.getServicoIds() != null) {
            venda.setServicos(obterServicos(dto.getServicoIds(), empresa));
        }
        validar(venda);
        return repositorio.save(venda);
    }

    @Transactional
    public void excluir(long id) {
        repositorio.delete(obterPorId(id));
    }

    private void validar(Venda venda) {
        if (venda.getMercadorias().isEmpty() && venda.getServicos().isEmpty()) {
            throw new IllegalArgumentException("A venda deve ter ao menos uma mercadoria ou um servico");
        }
        Veiculo veiculo = venda.getVeiculo();
        if (veiculo != null && !veiculo.getProprietario().getId().equals(venda.getCliente().getId())) {
            throw new IllegalArgumentException("O veiculo de id " + veiculo.getId()
                    + " nao pertence ao cliente de id " + venda.getCliente().getId());
        }
    }

    private Usuario obterComPerfil(long usuarioId, PerfilUsuario perfil) {
        Usuario usuario = obterUsuario(usuarioId);
        if (!usuario.getPerfis().contains(perfil)) {
            throw new IllegalArgumentException("O usuario de id " + usuarioId
                    + " nao possui o perfil " + perfil);
        }
        return usuario;
    }

    private Usuario obterFuncionario(long usuarioId, Empresa empresa) {
        Usuario funcionario = obterComPerfil(usuarioId, PerfilUsuario.FUNCIONARIO);
        if (funcionario.getEmpresa() == null || !funcionario.getEmpresa().getId().equals(empresa.getId())) {
            throw new IllegalArgumentException("O funcionario de id " + usuarioId
                    + " nao esta vinculado a empresa de id " + empresa.getId());
        }
        return funcionario;
    }

    private List<Mercadoria> obterMercadorias(List<Long> ids, Empresa empresa) {
        List<Mercadoria> mercadorias = new ArrayList<>();
        for (Long id : ids) {
            Mercadoria mercadoria = mercadoriaRepositorio.findById(id)
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Mercadoria", id));
            if (empresa.getMercadorias().stream().noneMatch(item -> item.getId().equals(id))) {
                throw new IllegalArgumentException("A mercadoria de id " + id
                        + " nao pertence a empresa de id " + empresa.getId());
            }
            mercadorias.add(mercadoria);
        }
        return mercadorias;
    }

    private List<Servico> obterServicos(List<Long> ids, Empresa empresa) {
        List<Servico> servicos = new ArrayList<>();
        for (Long id : ids) {
            Servico servico = servicoRepositorio.findById(id)
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Servico", id));
            if (empresa.getServicos().stream().noneMatch(item -> item.getId().equals(id))) {
                throw new IllegalArgumentException("O servico de id " + id
                        + " nao pertence a empresa de id " + empresa.getId());
            }
            servicos.add(servico);
        }
        return servicos;
    }

    private Empresa obterEmpresa(long empresaId) {
        return empresaRepositorio.findById(empresaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Empresa", empresaId));
    }

    private Usuario obterUsuario(long usuarioId) {
        return usuarioRepositorio.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario", usuarioId));
    }

    private Veiculo obterVeiculo(long veiculoId) {
        return veiculoRepositorio.findById(veiculoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Veiculo", veiculoId));
    }
}
