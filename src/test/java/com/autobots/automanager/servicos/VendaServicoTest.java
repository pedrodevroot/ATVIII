package com.autobots.automanager.servicos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
import com.autobots.automanager.repositorios.EmpresaRepositorio;
import com.autobots.automanager.repositorios.MercadoriaRepositorio;
import com.autobots.automanager.repositorios.ServicoRepositorio;
import com.autobots.automanager.repositorios.UsuarioRepositorio;
import com.autobots.automanager.repositorios.VeiculoRepositorio;
import com.autobots.automanager.repositorios.VendaRepositorio;

@ExtendWith(MockitoExtension.class)
class VendaServicoTest {

    private static final long EMPRESA = 1L;
    private static final long CLIENTE = 3L;
    private static final long OUTRO_CLIENTE = 4L;
    private static final long FUNCIONARIO = 6L;
    private static final long FUNCIONARIO_DE_OUTRA = 7L;
    private static final long FUNCIONARIO_SEM_EMPRESA = 8L;
    private static final long VEICULO = 50L;
    private static final long VEICULO_DE_OUTRO = 51L;
    private static final long MERCADORIA = 10L;
    private static final long MERCADORIA_DE_OUTRA = 11L;
    private static final long SERVICO = 20L;
    private static final long SERVICO_DE_OUTRA = 21L;
    private static final long VENDA = 100L;

    @Mock
    private VendaRepositorio repositorio;
    @Mock
    private EmpresaRepositorio empresaRepositorio;
    @Mock
    private UsuarioRepositorio usuarioRepositorio;
    @Mock
    private VeiculoRepositorio veiculoRepositorio;
    @Mock
    private MercadoriaRepositorio mercadoriaRepositorio;
    @Mock
    private ServicoRepositorio servicoRepositorio;

    @InjectMocks
    private VendaServico servico;

    private Empresa empresa;
    private Usuario cliente;
    private Mercadoria mercadoria;
    private Servico itemServico;
    private Veiculo veiculo;

    private Empresa novaEmpresa(long id) {
        Empresa nova = new Empresa();
        nova.setId(id);
        return nova;
    }

    private Usuario usuario(long id, PerfilUsuario perfil, Empresa vinculo) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.getPerfis().add(perfil);
        usuario.setEmpresa(vinculo);
        lenient().when(usuarioRepositorio.findById(id)).thenReturn(Optional.of(usuario));
        return usuario;
    }

    private Veiculo novoVeiculo(long id, Usuario dono) {
        Veiculo novo = new Veiculo();
        novo.setId(id);
        novo.setProprietario(dono);
        lenient().when(veiculoRepositorio.findById(id)).thenReturn(Optional.of(novo));
        return novo;
    }

    private Mercadoria novaMercadoria(long id) {
        Mercadoria nova = new Mercadoria();
        nova.setId(id);
        lenient().when(mercadoriaRepositorio.findById(id)).thenReturn(Optional.of(nova));
        return nova;
    }

    private Servico novoServico(long id) {
        Servico novo = new Servico();
        novo.setId(id);
        lenient().when(servicoRepositorio.findById(id)).thenReturn(Optional.of(novo));
        return novo;
    }

    private VendaCadastroDto cadastro() {
        VendaCadastroDto dto = new VendaCadastroDto();
        dto.setIdentificacao("V-1");
        dto.setClienteId(CLIENTE);
        dto.setFuncionarioId(FUNCIONARIO);
        dto.setVeiculoId(VEICULO);
        dto.setMercadoriaIds(List.of(MERCADORIA));
        dto.setServicoIds(List.of(SERVICO));
        return dto;
    }

    private Venda vendaExistente() {
        Venda venda = new Venda();
        venda.setId(VENDA);
        venda.setIdentificacao("V-1");
        venda.setCliente(cliente);
        venda.setFuncionario(usuarioRepositorio.findById(FUNCIONARIO).orElseThrow());
        venda.setVeiculo(veiculo);
        venda.getMercadorias().add(mercadoria);
        venda.getServicos().add(itemServico);
        lenient().when(repositorio.findById(VENDA)).thenReturn(Optional.of(venda));
        lenient().when(empresaRepositorio.findByVendasId(VENDA)).thenReturn(Optional.of(empresa));
        lenient().when(repositorio.save(venda)).thenReturn(venda);
        return venda;
    }

    private void criarRecusado(VendaCadastroDto dto, String mensagem) {
        IllegalArgumentException excecao = assertThrows(
                IllegalArgumentException.class, () -> servico.criar(EMPRESA, dto));
        assertEquals(mensagem, excecao.getMessage());
        verify(repositorio, never()).save(any(Venda.class));
    }

    @BeforeEach
    void prepararCenario() {
        empresa = novaEmpresa(EMPRESA);
        Empresa outraEmpresa = novaEmpresa(2L);
        lenient().when(empresaRepositorio.findById(EMPRESA)).thenReturn(Optional.of(empresa));

        cliente = usuario(CLIENTE, PerfilUsuario.CLIENTE, null);
        Usuario outroCliente = usuario(OUTRO_CLIENTE, PerfilUsuario.CLIENTE, null);
        usuario(FUNCIONARIO, PerfilUsuario.FUNCIONARIO, empresa);
        usuario(FUNCIONARIO_DE_OUTRA, PerfilUsuario.FUNCIONARIO, outraEmpresa);
        usuario(FUNCIONARIO_SEM_EMPRESA, PerfilUsuario.FUNCIONARIO, null);

        veiculo = novoVeiculo(VEICULO, cliente);
        novoVeiculo(VEICULO_DE_OUTRO, outroCliente);

        mercadoria = novaMercadoria(MERCADORIA);
        novaMercadoria(MERCADORIA_DE_OUTRA);
        empresa.getMercadorias().add(mercadoria);

        itemServico = novoServico(SERVICO);
        novoServico(SERVICO_DE_OUTRA);
        empresa.getServicos().add(itemServico);
    }

    @Test
    @DisplayName("listarPorEmpresa: devolve as vendas da empresa")
    void listarPorEmpresa() {
        Venda venda = new Venda();
        empresa.getVendas().add(venda);

        assertSame(venda, servico.listarPorEmpresa(EMPRESA).get(0));
    }

    @Test
    @DisplayName("listarPorEmpresa: lanca RecursoNaoEncontrado quando a empresa nao existe")
    void listarPorEmpresaInexistente() {
        when(empresaRepositorio.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> servico.listarPorEmpresa(999L));
    }

    @Test
    @DisplayName("listarPorUsuario: busca as vendas em que o usuario e cliente ou funcionario")
    void listarPorUsuario() {
        List<Venda> vendas = List.of(new Venda());
        when(repositorio.findByClienteIdOrFuncionarioIdOrderById(FUNCIONARIO, FUNCIONARIO)).thenReturn(vendas);

        assertSame(vendas, servico.listarPorUsuario(FUNCIONARIO));
    }

    @Test
    @DisplayName("listarPorUsuario: lanca RecursoNaoEncontrado quando o usuario nao existe")
    void listarPorUsuarioInexistente() {
        when(usuarioRepositorio.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> servico.listarPorUsuario(999L));

        verify(repositorio, never()).findByClienteIdOrFuncionarioIdOrderById(999L, 999L);
    }

    @Test
    @DisplayName("listarPorVeiculo: devolve as vendas do veiculo")
    void listarPorVeiculo() {
        Venda venda = new Venda();
        veiculo.getVendas().add(venda);

        assertSame(venda, servico.listarPorVeiculo(VEICULO).get(0));
    }

    @Test
    @DisplayName("criar: monta a venda completa, define a data e vincula a empresa")
    void criarCompleta() {
        when(repositorio.save(any(Venda.class))).thenAnswer(chamada -> {
            Venda argumento = chamada.getArgument(0);
            argumento.setId(VENDA);
            return argumento;
        });

        Venda criada = servico.criar(EMPRESA, cadastro());

        assertEquals(VENDA, criada.getId());
        assertEquals("V-1", criada.getIdentificacao());
        assertNotNull(criada.getCadastro());
        assertSame(cliente, criada.getCliente());
        assertEquals(FUNCIONARIO, criada.getFuncionario().getId());
        assertSame(veiculo, criada.getVeiculo());
        assertSame(mercadoria, criada.getMercadorias().get(0));
        assertSame(itemServico, criada.getServicos().get(0));
        assertSame(criada, empresa.getVendas().get(0));
        verify(empresaRepositorio).save(empresa);
    }

    @Test
    @DisplayName("criar: o veiculo e opcional, e basta um tipo de item")
    void criarSemVeiculoSoComServico() {
        when(repositorio.save(any(Venda.class))).thenAnswer(chamada -> chamada.getArgument(0));
        VendaCadastroDto dto = cadastro();
        dto.setVeiculoId(null);
        dto.setMercadoriaIds(List.of());

        Venda criada = servico.criar(EMPRESA, dto);

        assertNull(criada.getVeiculo());
        assertTrue(criada.getMercadorias().isEmpty());
        assertEquals(1, criada.getServicos().size());
    }

    @Test
    @DisplayName("criar: recusa cliente sem o perfil CLIENTE")
    void criarClienteSemPerfil() {
        VendaCadastroDto dto = cadastro();
        dto.setClienteId(FUNCIONARIO);

        criarRecusado(dto, "O usuario de id 6 nao possui o perfil CLIENTE");
    }

    @Test
    @DisplayName("criar: recusa funcionario sem o perfil FUNCIONARIO")
    void criarFuncionarioSemPerfil() {
        VendaCadastroDto dto = cadastro();
        dto.setFuncionarioId(CLIENTE);

        criarRecusado(dto, "O usuario de id 3 nao possui o perfil FUNCIONARIO");
    }

    @Test
    @DisplayName("criar: recusa funcionario vinculado a outra empresa")
    void criarFuncionarioDeOutraEmpresa() {
        VendaCadastroDto dto = cadastro();
        dto.setFuncionarioId(FUNCIONARIO_DE_OUTRA);

        criarRecusado(dto, "O funcionario de id 7 nao esta vinculado a empresa de id 1");
    }

    @Test
    @DisplayName("criar: recusa funcionario sem empresa")
    void criarFuncionarioSemEmpresa() {
        VendaCadastroDto dto = cadastro();
        dto.setFuncionarioId(FUNCIONARIO_SEM_EMPRESA);

        criarRecusado(dto, "O funcionario de id 8 nao esta vinculado a empresa de id 1");
    }

    @Test
    @DisplayName("criar: recusa veiculo de outro cliente")
    void criarVeiculoDeOutro() {
        VendaCadastroDto dto = cadastro();
        dto.setVeiculoId(VEICULO_DE_OUTRO);

        criarRecusado(dto, "O veiculo de id 51 nao pertence ao cliente de id 3");
    }

    @Test
    @DisplayName("criar: recusa mercadoria de outra empresa")
    void criarMercadoriaDeOutra() {
        VendaCadastroDto dto = cadastro();
        dto.setMercadoriaIds(List.of(MERCADORIA_DE_OUTRA));

        criarRecusado(dto, "A mercadoria de id 11 nao pertence a empresa de id 1");
    }

    @Test
    @DisplayName("criar: recusa servico de outra empresa")
    void criarServicoDeOutra() {
        VendaCadastroDto dto = cadastro();
        dto.setServicoIds(List.of(SERVICO_DE_OUTRA));

        criarRecusado(dto, "O servico de id 21 nao pertence a empresa de id 1");
    }

    @Test
    @DisplayName("criar: recusa venda sem nenhum item")
    void criarSemItens() {
        VendaCadastroDto dto = cadastro();
        dto.setMercadoriaIds(List.of());
        dto.setServicoIds(List.of());

        criarRecusado(dto, "A venda deve ter ao menos uma mercadoria ou um servico");
    }

    @Test
    @DisplayName("criar: lanca RecursoNaoEncontrado para empresa, cliente, veiculo e itens inexistentes")
    void criarReferenciasInexistentes() {
        when(empresaRepositorio.findById(999L)).thenReturn(Optional.empty());
        assertThrows(RecursoNaoEncontradoException.class, () -> servico.criar(999L, cadastro()));

        VendaCadastroDto semCliente = cadastro();
        semCliente.setClienteId(999L);
        when(usuarioRepositorio.findById(999L)).thenReturn(Optional.empty());
        assertThrows(RecursoNaoEncontradoException.class, () -> servico.criar(EMPRESA, semCliente));

        VendaCadastroDto semVeiculo = cadastro();
        semVeiculo.setVeiculoId(999L);
        when(veiculoRepositorio.findById(999L)).thenReturn(Optional.empty());
        assertThrows(RecursoNaoEncontradoException.class, () -> servico.criar(EMPRESA, semVeiculo));

        VendaCadastroDto semMercadoria = cadastro();
        semMercadoria.setMercadoriaIds(List.of(999L));
        when(mercadoriaRepositorio.findById(999L)).thenReturn(Optional.empty());
        assertThrows(RecursoNaoEncontradoException.class, () -> servico.criar(EMPRESA, semMercadoria));

        VendaCadastroDto semServico = cadastro();
        semServico.setServicoIds(List.of(999L));
        when(servicoRepositorio.findById(999L)).thenReturn(Optional.empty());
        assertThrows(RecursoNaoEncontradoException.class, () -> servico.criar(EMPRESA, semServico));

        verify(repositorio, never()).save(any(Venda.class));
    }

    @Test
    @DisplayName("obterPorId: lanca RecursoNaoEncontrado quando o id nao existe")
    void obterPorIdInexistente() {
        when(repositorio.findById(999L)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException excecao = assertThrows(
                RecursoNaoEncontradoException.class, () -> servico.obterPorId(999L));

        assertEquals("Venda de id 999 nao foi encontrado", excecao.getMessage());
    }

    @Test
    @DisplayName("atualizar: altera somente o que foi enviado")
    void atualizarParcial() {
        Venda venda = vendaExistente();
        VendaAtualizacaoDto dto = new VendaAtualizacaoDto();
        dto.setIdentificacao("V-2");

        Venda atualizada = servico.atualizar(VENDA, dto);

        assertEquals("V-2", atualizada.getIdentificacao());
        assertSame(cliente, venda.getCliente());
        assertSame(veiculo, venda.getVeiculo());
        assertEquals(1, venda.getMercadorias().size());
        assertEquals(1, venda.getServicos().size());
    }

    @Test
    @DisplayName("atualizar: identificacao em branco nao altera a venda")
    void atualizarIdentificacaoEmBranco() {
        Venda venda = vendaExistente();
        VendaAtualizacaoDto dto = new VendaAtualizacaoDto();
        dto.setIdentificacao("   ");

        servico.atualizar(VENDA, dto);

        assertEquals("V-1", venda.getIdentificacao());
    }

    @Test
    @DisplayName("atualizar: lista vazia remove os itens quando sobra outro tipo de item")
    void atualizarRemoveMercadorias() {
        Venda venda = vendaExistente();
        VendaAtualizacaoDto dto = new VendaAtualizacaoDto();
        dto.setMercadoriaIds(List.of());

        servico.atualizar(VENDA, dto);

        assertTrue(venda.getMercadorias().isEmpty());
        assertEquals(1, venda.getServicos().size());
    }

    @Test
    @DisplayName("atualizar: recusa deixar a venda sem nenhum item")
    void atualizarSemItens() {
        vendaExistente();
        VendaAtualizacaoDto dto = new VendaAtualizacaoDto();
        dto.setMercadoriaIds(List.of());
        dto.setServicoIds(List.of());

        assertThrows(IllegalArgumentException.class, () -> servico.atualizar(VENDA, dto));

        verify(repositorio, never()).save(any(Venda.class));
    }

    @Test
    @DisplayName("atualizar: recusa trocar o cliente quando o veiculo continua sendo do anterior")
    void atualizarClienteDiferenteDoDonoDoVeiculo() {
        vendaExistente();
        VendaAtualizacaoDto dto = new VendaAtualizacaoDto();
        dto.setClienteId(OUTRO_CLIENTE);

        IllegalArgumentException excecao = assertThrows(
                IllegalArgumentException.class, () -> servico.atualizar(VENDA, dto));

        assertEquals("O veiculo de id 50 nao pertence ao cliente de id 4", excecao.getMessage());
    }

    @Test
    @DisplayName("atualizar: troca cliente e veiculo juntos")
    void atualizarClienteEVeiculo() {
        Venda venda = vendaExistente();
        VendaAtualizacaoDto dto = new VendaAtualizacaoDto();
        dto.setClienteId(OUTRO_CLIENTE);
        dto.setVeiculoId(VEICULO_DE_OUTRO);

        servico.atualizar(VENDA, dto);

        assertEquals(OUTRO_CLIENTE, venda.getCliente().getId());
        assertEquals(VEICULO_DE_OUTRO, venda.getVeiculo().getId());
    }

    @Test
    @DisplayName("atualizar: confere o funcionario e os itens novos na empresa da venda")
    void atualizarConfereEmpresa() {
        vendaExistente();

        VendaAtualizacaoDto funcionario = new VendaAtualizacaoDto();
        funcionario.setFuncionarioId(FUNCIONARIO_DE_OUTRA);
        assertThrows(IllegalArgumentException.class, () -> servico.atualizar(VENDA, funcionario));

        VendaAtualizacaoDto mercadorias = new VendaAtualizacaoDto();
        mercadorias.setMercadoriaIds(List.of(MERCADORIA_DE_OUTRA));
        assertThrows(IllegalArgumentException.class, () -> servico.atualizar(VENDA, mercadorias));

        VendaAtualizacaoDto servicos = new VendaAtualizacaoDto();
        servicos.setServicoIds(List.of(SERVICO_DE_OUTRA));
        assertThrows(IllegalArgumentException.class, () -> servico.atualizar(VENDA, servicos));

        verify(repositorio, never()).save(any(Venda.class));
    }

    @Test
    @DisplayName("atualizar: lanca RecursoNaoEncontrado quando a venda nao pertence a nenhuma empresa")
    void atualizarSemEmpresa() {
        vendaExistente();
        when(empresaRepositorio.findByVendasId(VENDA)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException excecao = assertThrows(RecursoNaoEncontradoException.class,
                () -> servico.atualizar(VENDA, new VendaAtualizacaoDto()));

        assertEquals("A venda de id 100 nao pertence a nenhuma empresa", excecao.getMessage());
    }

    @Test
    @DisplayName("excluir: remove a venda encontrada")
    void excluirExistente() {
        Venda venda = vendaExistente();

        servico.excluir(VENDA);

        verify(repositorio).delete(venda);
    }

    @Test
    @DisplayName("excluir: lanca RecursoNaoEncontrado quando o id nao existe")
    void excluirInexistente() {
        when(repositorio.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> servico.excluir(999L));

        verify(repositorio, never()).delete(any(Venda.class));
    }
}
