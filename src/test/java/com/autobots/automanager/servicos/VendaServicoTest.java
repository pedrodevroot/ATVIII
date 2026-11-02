package com.autobots.automanager.servicos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

    private ItemVendaCadastroDto itemDto(long mercadoriaId, long quantidade) {
        ItemVendaCadastroDto dto = new ItemVendaCadastroDto();
        dto.setMercadoriaId(mercadoriaId);
        dto.setQuantidade(quantidade);
        return dto;
    }

    private VendaCadastroDto cadastro() {
        VendaCadastroDto dto = new VendaCadastroDto();
        dto.setIdentificacao("V-1");
        dto.setClienteId(CLIENTE);
        dto.setFuncionarioId(FUNCIONARIO);
        dto.setVeiculoId(VEICULO);
        dto.setItens(List.of(itemDto(MERCADORIA, 4L)));
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
        ItemVenda item = new ItemVenda();
        item.setVenda(venda);
        item.setMercadoria(mercadoria);
        item.setQuantidade(2);
        item.setValorUnitario(new BigDecimal("100.00"));
        venda.getItens().add(item);
        ItemServico contratado = new ItemServico();
        contratado.setVenda(venda);
        contratado.setServico(itemServico);
        contratado.setValorUnitario(new BigDecimal("40.00"));
        venda.getItensServico().add(contratado);
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
        lenient().when(mercadoriaRepositorio.baixarEstoque(anyLong(), anyLong())).thenReturn(1);
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
        mercadoria.setValor(new BigDecimal("300.00"));
        novaMercadoria(MERCADORIA_DE_OUTRA);
        empresa.getMercadorias().add(mercadoria);

        itemServico = novoServico(SERVICO);
        itemServico.setValor(new BigDecimal("50.00"));
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
        assertSame(mercadoria, criada.getItens().get(0).getMercadoria());
        assertSame(criada, criada.getItens().get(0).getVenda());
        assertEquals(4L, criada.getItens().get(0).getQuantidade());
        verify(mercadoriaRepositorio).baixarEstoque(MERCADORIA, 4L);
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
        dto.setItens(List.of());

        Venda criada = servico.criar(EMPRESA, dto);

        assertNull(criada.getVeiculo());
        assertTrue(criada.getItens().isEmpty());
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
        dto.setItens(List.of(itemDto(MERCADORIA_DE_OUTRA, 1L)));

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
        dto.setItens(List.of());
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
        semMercadoria.setItens(List.of(itemDto(999L, 1L)));
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
        assertEquals(1, venda.getItens().size());
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
        dto.setItens(List.of());

        servico.atualizar(VENDA, dto);

        assertTrue(venda.getItens().isEmpty());
        assertEquals(1, venda.getServicos().size());
    }

    @Test
    @DisplayName("atualizar: recusa deixar a venda sem nenhum item")
    void atualizarSemItens() {
        vendaExistente();
        VendaAtualizacaoDto dto = new VendaAtualizacaoDto();
        dto.setItens(List.of());
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
        mercadorias.setItens(List.of(itemDto(MERCADORIA_DE_OUTRA, 1L)));
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

        verify(mercadoriaRepositorio).devolverEstoque(MERCADORIA, 2L);
        verify(repositorio).delete(venda);
    }

    @Test
    @DisplayName("excluir: lanca RecursoNaoEncontrado quando o id nao existe")
    void excluirInexistente() {
        when(repositorio.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> servico.excluir(999L));

        verify(repositorio, never()).delete(any(Venda.class));
    }

    @Test
    @DisplayName("criar: lanca Conflito quando o estoque nao atende a quantidade, sem salvar a venda")
    void criarEstoqueInsuficiente() {
        when(mercadoriaRepositorio.baixarEstoque(MERCADORIA, 4L)).thenReturn(0);

        ConflitoException excecao = assertThrows(
                ConflitoException.class, () -> servico.criar(EMPRESA, cadastro()));

        assertEquals("Estoque insuficiente para a mercadoria de id 10. Quantidade solicitada: 4",
                excecao.getMessage());
        verify(repositorio, never()).save(any(Venda.class));
    }

    @Test
    @DisplayName("criar: recusa a mesma mercadoria informada duas vezes, sem baixar o estoque da repetida")
    void criarMercadoriaRepetida() {
        VendaCadastroDto dto = cadastro();
        dto.setItens(List.of(itemDto(MERCADORIA, 1L), itemDto(MERCADORIA, 2L)));

        criarRecusado(dto, "A mercadoria de id 10 foi informada mais de uma vez");

        verify(mercadoriaRepositorio, never()).baixarEstoque(MERCADORIA, 2L);
    }

    @Test
    @DisplayName("criar: nao baixa o estoque de mercadoria de outra empresa")
    void criarNaoBaixaEstoqueDeOutraEmpresa() {
        VendaCadastroDto dto = cadastro();
        dto.setItens(List.of(itemDto(MERCADORIA_DE_OUTRA, 1L)));

        assertThrows(IllegalArgumentException.class, () -> servico.criar(EMPRESA, dto));

        verify(mercadoriaRepositorio, never()).baixarEstoque(MERCADORIA_DE_OUTRA, 1L);
    }

    @Test
    @DisplayName("atualizar: devolve o estoque dos itens antigos antes de baixar o dos novos")
    void atualizarTrocaItens() {
        Venda venda = vendaExistente();
        VendaAtualizacaoDto dto = new VendaAtualizacaoDto();
        dto.setItens(List.of(itemDto(MERCADORIA, 5L)));

        servico.atualizar(VENDA, dto);

        InOrder ordem = inOrder(mercadoriaRepositorio);
        ordem.verify(mercadoriaRepositorio).devolverEstoque(MERCADORIA, 2L);
        ordem.verify(mercadoriaRepositorio).baixarEstoque(MERCADORIA, 5L);
        assertEquals(1, venda.getItens().size());
        assertEquals(5L, venda.getItens().get(0).getQuantidade());
    }

    @Test
    @DisplayName("atualizar: sem a lista de itens, o estoque nao e tocado")
    void atualizarSemItensNaoMexeNoEstoque() {
        Venda venda = vendaExistente();
        VendaAtualizacaoDto dto = new VendaAtualizacaoDto();
        dto.setIdentificacao("V-2");

        servico.atualizar(VENDA, dto);

        verify(mercadoriaRepositorio, never()).devolverEstoque(anyLong(), anyLong());
        verify(mercadoriaRepositorio, never()).baixarEstoque(anyLong(), anyLong());
        assertEquals(2L, venda.getItens().get(0).getQuantidade());
    }

    @Test
    @DisplayName("atualizar: lanca Conflito quando o estoque nao atende os itens novos")
    void atualizarEstoqueInsuficiente() {
        vendaExistente();
        when(mercadoriaRepositorio.baixarEstoque(MERCADORIA, 50L)).thenReturn(0);
        VendaAtualizacaoDto dto = new VendaAtualizacaoDto();
        dto.setItens(List.of(itemDto(MERCADORIA, 50L)));

        assertThrows(ConflitoException.class, () -> servico.atualizar(VENDA, dto));

        verify(repositorio, never()).save(any(Venda.class));
    }

    @Test
    @DisplayName("excluir: venda sem mercadorias nao mexe no estoque")
    void excluirSemMercadorias() {
        Venda venda = vendaExistente();
        venda.getItens().clear();

        servico.excluir(VENDA);

        verify(mercadoriaRepositorio, never()).devolverEstoque(anyLong(), anyLong());
        verify(repositorio).delete(venda);
    }

    @Test
    @DisplayName("criar: baixa o estoque sempre na ordem do id da mercadoria, para evitar deadlock")
    void criarBaixaEstoqueEmOrdem() {
        Mercadoria segunda = novaMercadoria(12L);
        empresa.getMercadorias().add(segunda);
        when(repositorio.save(any(Venda.class))).thenAnswer(chamada -> chamada.getArgument(0));
        VendaCadastroDto dto = cadastro();
        dto.setItens(List.of(itemDto(12L, 1L), itemDto(MERCADORIA, 2L)));

        servico.criar(EMPRESA, dto);

        InOrder ordem = inOrder(mercadoriaRepositorio);
        ordem.verify(mercadoriaRepositorio).baixarEstoque(MERCADORIA, 2L);
        ordem.verify(mercadoriaRepositorio).baixarEstoque(12L, 1L);
    }

    @Test
    @DisplayName("excluir: devolve o estoque sempre na ordem do id da mercadoria, para evitar deadlock")
    void excluirDevolveEstoqueEmOrdem() {
        Venda venda = vendaExistente();
        Mercadoria menor = novaMercadoria(5L);
        ItemVenda item = new ItemVenda();
        item.setVenda(venda);
        item.setMercadoria(menor);
        item.setQuantidade(3);
        venda.getItens().add(item);

        servico.excluir(VENDA);

        InOrder ordem = inOrder(mercadoriaRepositorio);
        ordem.verify(mercadoriaRepositorio).devolverEstoque(5L, 3L);
        ordem.verify(mercadoriaRepositorio).devolverEstoque(MERCADORIA, 2L);
    }

    @Test
    @DisplayName("criar: recusa cliente vinculado a outra empresa")
    void criarClienteDeOutraEmpresa() {
        usuario(60L, PerfilUsuario.CLIENTE, novaEmpresa(2L));
        VendaCadastroDto dto = cadastro();
        dto.setClienteId(60L);
        dto.setVeiculoId(null);

        criarRecusado(dto, "O cliente de id 60 esta vinculado a empresa de id 2");
    }

    @Test
    @DisplayName("criar: aceita cliente vinculado a mesma empresa, e tambem o sem empresa")
    void criarClienteDaMesmaEmpresa() {
        when(repositorio.save(any(Venda.class))).thenAnswer(chamada -> chamada.getArgument(0));
        Usuario daEmpresa = usuario(61L, PerfilUsuario.CLIENTE, empresa);
        VendaCadastroDto dto = cadastro();
        dto.setClienteId(61L);
        dto.setVeiculoId(null);

        assertSame(daEmpresa, servico.criar(EMPRESA, dto).getCliente());
        assertSame(cliente, servico.criar(EMPRESA, cadastro()).getCliente());
    }

    @Test
    @DisplayName("atualizar: recusa trocar para cliente vinculado a outra empresa")
    void atualizarClienteDeOutraEmpresa() {
        vendaExistente();
        usuario(60L, PerfilUsuario.CLIENTE, novaEmpresa(2L));
        VendaAtualizacaoDto dto = new VendaAtualizacaoDto();
        dto.setClienteId(60L);

        assertThrows(IllegalArgumentException.class, () -> servico.atualizar(VENDA, dto));

        verify(repositorio, never()).save(any(Venda.class));
    }

    @Test
    @DisplayName("criar: o item guarda o valor atual da mercadoria")
    void criarGuardaValorDaMercadoria() {
        when(repositorio.save(any(Venda.class))).thenAnswer(chamada -> chamada.getArgument(0));

        Venda criada = servico.criar(EMPRESA, cadastro());

        assertEquals(new BigDecimal("300.00"), criada.getItens().get(0).getValorUnitario());
    }

    @Test
    @DisplayName("atualizar: mercadoria que continua na venda mantem o valor original, e a nova usa o atual")
    void atualizarPreservaValorOriginal() {
        Venda venda = vendaExistente();
        Mercadoria nova = novaMercadoria(12L);
        nova.setValor(new BigDecimal("50.00"));
        empresa.getMercadorias().add(nova);
        VendaAtualizacaoDto dto = new VendaAtualizacaoDto();
        dto.setItens(List.of(itemDto(MERCADORIA, 5L), itemDto(12L, 1L)));

        servico.atualizar(VENDA, dto);

        assertEquals(new BigDecimal("100.00"), venda.getItens().get(0).getValorUnitario());
        assertEquals(new BigDecimal("50.00"), venda.getItens().get(1).getValorUnitario());
    }

    @Test
    @DisplayName("criar: o servico da venda guarda o valor atual")
    void criarGuardaValorDoServico() {
        when(repositorio.save(any(Venda.class))).thenAnswer(chamada -> chamada.getArgument(0));

        Venda criada = servico.criar(EMPRESA, cadastro());

        assertEquals(new BigDecimal("50.00"), criada.getItensServico().get(0).getValorUnitario());
        assertSame(criada, criada.getItensServico().get(0).getVenda());
    }

    @Test
    @DisplayName("atualizar: servico que continua na venda mantem o valor original, e o novo usa o atual")
    void atualizarPreservaValorDoServico() {
        Venda venda = vendaExistente();
        Servico novo = novoServico(22L);
        novo.setValor(new BigDecimal("70.00"));
        empresa.getServicos().add(novo);
        VendaAtualizacaoDto dto = new VendaAtualizacaoDto();
        dto.setServicoIds(List.of(SERVICO, 22L));

        servico.atualizar(VENDA, dto);

        assertEquals(new BigDecimal("40.00"), venda.getItensServico().get(0).getValorUnitario());
        assertEquals(new BigDecimal("70.00"), venda.getItensServico().get(1).getValorUnitario());
    }

    @Test
    @DisplayName("atualizar: removerVeiculo tira o veiculo da venda")
    void atualizarRemoveVeiculo() {
        Venda venda = vendaExistente();
        VendaAtualizacaoDto dto = new VendaAtualizacaoDto();
        dto.setRemoverVeiculo(true);

        servico.atualizar(VENDA, dto);

        assertNull(venda.getVeiculo());
    }

    @Test
    @DisplayName("atualizar: sem removerVeiculo, o veiculo fica; e removerVeiculo false nao remove")
    void atualizarMantemVeiculo() {
        Venda venda = vendaExistente();
        VendaAtualizacaoDto dto = new VendaAtualizacaoDto();
        dto.setRemoverVeiculo(false);

        servico.atualizar(VENDA, dto);

        assertSame(veiculo, venda.getVeiculo());
    }

    @Test
    @DisplayName("atualizar: recusa veiculoId e removerVeiculo juntos")
    void atualizarVeiculoEmDobro() {
        vendaExistente();
        VendaAtualizacaoDto dto = new VendaAtualizacaoDto();
        dto.setVeiculoId(VEICULO);
        dto.setRemoverVeiculo(true);

        IllegalArgumentException excecao = assertThrows(
                IllegalArgumentException.class, () -> servico.atualizar(VENDA, dto));

        assertEquals("Informe veiculoId ou removerVeiculo, nao os dois", excecao.getMessage());
    }
}
