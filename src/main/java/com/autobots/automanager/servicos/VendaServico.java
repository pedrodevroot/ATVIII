package com.autobots.automanager.servicos;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.autobots.automanager.dtos.ItemVendaCadastroDto;
import com.autobots.automanager.dtos.VendaAtualizacaoDto;
import com.autobots.automanager.dtos.VendaCadastroDto;
import com.autobots.automanager.entidades.Empresa;
import com.autobots.automanager.entidades.ItemServico;
import com.autobots.automanager.entidades.ItemVenda;
import com.autobots.automanager.entidades.Mercadoria;
import com.autobots.automanager.entidades.Servico;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.entidades.Veiculo;
import com.autobots.automanager.entidades.Venda;
import com.autobots.automanager.enumeracoes.PerfilUsuario;
import com.autobots.automanager.excecoes.ConflitoException;
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
        venda.setCliente(obterCliente(dto.getClienteId(), empresa));
        venda.setFuncionario(obterFuncionario(dto.getFuncionarioId(), empresa));
        if (dto.getVeiculoId() != null) {
            venda.setVeiculo(obterVeiculo(dto.getVeiculoId()));
        }
        adicionarItens(venda, dto.getItens(), empresa, Map.of());
        adicionarServicos(venda, dto.getServicoIds(), empresa, Map.of());
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
            venda.setCliente(obterCliente(dto.getClienteId(), empresa));
        }
        if (dto.getFuncionarioId() != null) {
            venda.setFuncionario(obterFuncionario(dto.getFuncionarioId(), empresa));
        }
        if (dto.getVeiculoId() != null && Boolean.TRUE.equals(dto.getRemoverVeiculo())) {
            throw new IllegalArgumentException("Informe veiculoId ou removerVeiculo, nao os dois");
        }
        if (dto.getVeiculoId() != null) {
            venda.setVeiculo(obterVeiculo(dto.getVeiculoId()));
        }
        if (Boolean.TRUE.equals(dto.getRemoverVeiculo())) {
            venda.setVeiculo(null);
        }
        if (dto.getItens() != null) {
            Map<Long, BigDecimal> valoresOriginais = valoresDosItens(venda);
            devolverEstoque(venda);
            venda.getItens().clear();
            adicionarItens(venda, dto.getItens(), empresa, valoresOriginais);
        }
        if (dto.getServicoIds() != null) {
            Map<Long, BigDecimal> valoresOriginais = valoresDosServicos(venda);
            venda.getItensServico().clear();
            adicionarServicos(venda, dto.getServicoIds(), empresa, valoresOriginais);
        }
        validar(venda);
        return repositorio.save(venda);
    }

    @Transactional
    public void excluir(long id) {
        Venda venda = obterPorId(id);
        devolverEstoque(venda);
        repositorio.delete(venda);
    }

    private void validar(Venda venda) {
        if (venda.getItens().isEmpty() && venda.getServicos().isEmpty()) {
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

    private Usuario obterCliente(long usuarioId, Empresa empresa) {
        Usuario cliente = obterComPerfil(usuarioId, PerfilUsuario.CLIENTE);
        if (cliente.getEmpresa() != null && !cliente.getEmpresa().getId().equals(empresa.getId())) {
            throw new IllegalArgumentException("O cliente de id " + usuarioId
                    + " esta vinculado a empresa de id " + cliente.getEmpresa().getId());
        }
        return cliente;
    }

    private Usuario obterFuncionario(long usuarioId, Empresa empresa) {
        Usuario funcionario = obterComPerfil(usuarioId, PerfilUsuario.FUNCIONARIO);
        if (funcionario.getEmpresa() == null || !funcionario.getEmpresa().getId().equals(empresa.getId())) {
            throw new IllegalArgumentException("O funcionario de id " + usuarioId
                    + " nao esta vinculado a empresa de id " + empresa.getId());
        }
        return funcionario;
    }

    private void adicionarItens(Venda venda, List<ItemVendaCadastroDto> itens, Empresa empresa,
            Map<Long, BigDecimal> valoresOriginais) {
        List<ItemVendaCadastroDto> ordenados = new ArrayList<>(itens);
        ordenados.sort(Comparator.comparing(ItemVendaCadastroDto::getMercadoriaId));
        Set<Long> informadas = new HashSet<>();
        for (ItemVendaCadastroDto item : ordenados) {
            Long id = item.getMercadoriaId();
            if (!informadas.add(id)) {
                throw new IllegalArgumentException("A mercadoria de id " + id
                        + " foi informada mais de uma vez");
            }
            Mercadoria mercadoria = obterMercadoria(id, empresa);
            if (mercadoriaRepositorio.baixarEstoque(id, item.getQuantidade()) == 0) {
                throw new ConflitoException("Estoque insuficiente para a mercadoria de id " + id
                        + ". Quantidade solicitada: " + item.getQuantidade());
            }
            ItemVenda novo = new ItemVenda();
            novo.setVenda(venda);
            novo.setMercadoria(mercadoria);
            novo.setQuantidade(item.getQuantidade());
            novo.setValorUnitario(valoresOriginais.getOrDefault(id, mercadoria.getValor()));
            venda.getItens().add(novo);
        }
    }

    private Map<Long, BigDecimal> valoresDosItens(Venda venda) {
        Map<Long, BigDecimal> valores = new HashMap<>();
        for (ItemVenda item : venda.getItens()) {
            valores.put(item.getMercadoria().getId(), item.getValorUnitario());
        }
        return valores;
    }

    private void devolverEstoque(Venda venda) {
        List<ItemVenda> ordenados = new ArrayList<>(venda.getItens());
        ordenados.sort(Comparator.comparing(item -> item.getMercadoria().getId()));
        for (ItemVenda item : ordenados) {
            mercadoriaRepositorio.devolverEstoque(item.getMercadoria().getId(), item.getQuantidade());
        }
    }

    private Mercadoria obterMercadoria(long id, Empresa empresa) {
        Mercadoria mercadoria = mercadoriaRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Mercadoria", id));
        if (empresa.getMercadorias().stream().noneMatch(item -> item.getId().equals(id))) {
            throw new IllegalArgumentException("A mercadoria de id " + id
                    + " nao pertence a empresa de id " + empresa.getId());
        }
        return mercadoria;
    }

    private void adicionarServicos(Venda venda, List<Long> ids, Empresa empresa,
            Map<Long, BigDecimal> valoresOriginais) {
        for (Long id : ids) {
            Servico servico = servicoRepositorio.findById(id)
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Servico", id));
            if (empresa.getServicos().stream().noneMatch(item -> item.getId().equals(id))) {
                throw new IllegalArgumentException("O servico de id " + id
                        + " nao pertence a empresa de id " + empresa.getId());
            }
            ItemServico novo = new ItemServico();
            novo.setVenda(venda);
            novo.setServico(servico);
            novo.setValorUnitario(valoresOriginais.getOrDefault(id, servico.getValor()));
            venda.getItensServico().add(novo);
        }
    }

    private Map<Long, BigDecimal> valoresDosServicos(Venda venda) {
        Map<Long, BigDecimal> valores = new HashMap<>();
        for (ItemServico item : venda.getItensServico()) {
            valores.put(item.getServico().getId(), item.getValorUnitario());
        }
        return valores;
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
