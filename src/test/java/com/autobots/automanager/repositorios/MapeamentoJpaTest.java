package com.autobots.automanager.repositorios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.hibernate.Hibernate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import com.autobots.automanager.entidades.CredencialUsuarioSenha;
import com.autobots.automanager.entidades.Documento;
import com.autobots.automanager.entidades.Email;
import com.autobots.automanager.entidades.Empresa;
import com.autobots.automanager.entidades.Endereco;
import com.autobots.automanager.entidades.Mercadoria;
import com.autobots.automanager.entidades.Servico;
import com.autobots.automanager.entidades.Telefone;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.entidades.Veiculo;
import com.autobots.automanager.entidades.Venda;
import com.autobots.automanager.enumeracoes.PerfilUsuario;
import com.autobots.automanager.enumeracoes.TipoDocumento;
import com.autobots.automanager.enumeracoes.TipoVeiculo;

@DataJpaTest
class MapeamentoJpaTest {

    @Autowired
    private UsuarioRepositorio repositorio;
    @Autowired
    private EmpresaRepositorio empresaRepositorio;
    @Autowired
    private VendaRepositorio vendaRepositorio;
    @Autowired
    private VeiculoRepositorio veiculoRepositorio;
    @Autowired
    private TestEntityManager gerenciador;

    private Usuario usuarioCompleto(String nome, String numeroDocumento) {
        Usuario usuario = new Usuario();
        usuario.setNome(nome);
        usuario.setNomeSocial("Apelido");
        Calendar calendario = Calendar.getInstance();
        calendario.set(1995, Calendar.AUGUST, 20);
        usuario.setDataNascimento(calendario.getTime());
        usuario.setDataCadastro(Calendar.getInstance().getTime());
        usuario.getPerfis().add(PerfilUsuario.CLIENTE);

        Endereco endereco = new Endereco();
        endereco.setEstado("Sao Paulo");
        endereco.setCidade("Santos");
        endereco.setBairro("Gonzaga");
        endereco.setRua("Avenida Ana Costa");
        endereco.setNumero("250");
        endereco.setCodigoPostal("11060002");
        usuario.setEndereco(endereco);

        Documento documento = new Documento();
        documento.setTipo(TipoDocumento.RG);
        documento.setNumero(numeroDocumento);
        documento.setDataEmissao(new Date());
        usuario.getDocumentos().add(documento);

        Telefone telefone = new Telefone();
        telefone.setDdd("13");
        telefone.setNumero("981234567");
        usuario.getTelefones().add(telefone);

        Email email = new Email();
        email.setEndereco(nome.replace(" ", "") + "@email.com");
        usuario.getEmails().add(email);

        CredencialUsuarioSenha credencial = new CredencialUsuarioSenha();
        credencial.setNomeUsuario("login-" + numeroDocumento);
        credencial.setSenha("segredo1");
        credencial.setCriacao(new Date());
        usuario.getCredenciais().add(credencial);

        return usuario;
    }

    private Veiculo veiculo(Usuario dono, String placa) {
        Veiculo veiculo = new Veiculo();
        veiculo.setTipo(TipoVeiculo.SUV);
        veiculo.setModelo("Corolla Cross");
        veiculo.setPlaca(placa);
        veiculo.setProprietario(dono);
        dono.getVeiculos().add(veiculo);
        return veiculo;
    }

    private Usuario usuarioSimples(String nome, PerfilUsuario perfil) {
        Usuario usuario = new Usuario();
        usuario.setNome(nome);
        usuario.getPerfis().add(perfil);
        return usuario;
    }

    private long contar(String tabela) {
        Object total = gerenciador.getEntityManager()
                .createNativeQuery("select count(*) from " + tabela)
                .getSingleResult();
        return ((Number) total).longValue();
    }

    private List<?> tabelas() {
        return gerenciador.getEntityManager().createNativeQuery(
                "select table_name from information_schema.tables "
                        + "where table_schema = 'PUBLIC' order by table_name")
                .getResultList();
    }

    @Test
    @DisplayName("o schema tem 16 tabelas: as 13 entidades, os perfis e as 2 juncoes da venda")
    void schemaComDezesseisTabelas() {
        List<?> nomes = tabelas();

        assertEquals(16, nomes.size(), "esperado 16 tabelas, encontrado: " + nomes);
        for (String tabela : List.of("USUARIO", "USUARIO_PERFIS", "EMPRESA", "ENDERECO", "TELEFONE",
                "DOCUMENTO", "EMAIL", "CREDENCIAL", "CREDENCIAL_USUARIO_SENHA", "CREDENCIAL_CODIGO_BARRA",
                "MERCADORIA", "SERVICO", "VEICULO", "VENDA", "VENDA_MERCADORIA", "VENDA_SERVICO")) {
            assertTrue(nomes.contains(tabela), "faltou a tabela " + tabela);
        }
    }

    @Test
    @DisplayName("cascade: salvar o usuario persiste endereco, documentos, telefones, e-mails e credenciais")
    void cascadeAoSalvar() {
        Usuario salvo = repositorio.saveAndFlush(usuarioCompleto("Joana", "111111"));
        gerenciador.clear();

        Usuario lido = repositorio.findById(salvo.getId()).orElseThrow();

        assertNotNull(lido.getEndereco().getId());
        assertEquals("Santos", lido.getEndereco().getCidade());
        assertEquals(1, lido.getDocumentos().size());
        assertEquals(1, lido.getTelefones().size());
        assertEquals(1, lido.getEmails().size());
        assertEquals(1, lido.getCredenciais().size());
        assertEquals(List.of(PerfilUsuario.CLIENTE), List.copyOf(lido.getPerfis()));
    }

    @Test
    @DisplayName("heranca JOINED: a credencial volta do banco como a subclasse certa")
    void credencialVoltaComoSubclasse() {
        Usuario salvo = repositorio.saveAndFlush(usuarioCompleto("Joana", "121212"));
        gerenciador.clear();

        Usuario lido = repositorio.findById(salvo.getId()).orElseThrow();

        CredencialUsuarioSenha credencial = (CredencialUsuarioSenha) lido.getCredenciais().get(0);
        assertEquals("login-121212", credencial.getNomeUsuario());
        assertEquals(1, contar("credencial"));
        assertEquals(1, contar("credencial_usuario_senha"));
        assertEquals(0, contar("credencial_codigo_barra"));
    }

    @Test
    @DisplayName("a FK usuario_id fica na tabela telefone e vem preenchida")
    void fkNaTabelaFilha() {
        Usuario salvo = repositorio.saveAndFlush(usuarioCompleto("Joana", "222222"));
        Long idTelefone = salvo.getTelefones().get(0).getId();

        Object fk = gerenciador.getEntityManager()
                .createNativeQuery("select usuario_id from telefone where id = :id")
                .setParameter("id", idTelefone)
                .getSingleResult();

        assertEquals(salvo.getId().longValue(), ((Number) fk).longValue());
    }

    @Test
    @DisplayName("enum STRING: o banco grava o nome do valor, e nao a posicao")
    void enumGravadoComoTexto() {
        Usuario salvo = repositorio.saveAndFlush(usuarioCompleto("Joana", "232323"));

        Object tipo = gerenciador.getEntityManager()
                .createNativeQuery("select tipo from documento where id = :id")
                .setParameter("id", salvo.getDocumentos().get(0).getId())
                .getSingleResult();
        Object perfil = gerenciador.getEntityManager()
                .createNativeQuery("select perfis from usuario_perfis where usuario_id = :id")
                .setParameter("id", salvo.getId())
                .getSingleResult();

        assertEquals("RG", tipo);
        assertEquals("CLIENTE", perfil);
    }

    @Test
    @DisplayName("orphanRemoval: remover o telefone da colecao apaga a linha")
    void orphanRemovalDoTelefone() {
        Usuario salvo = repositorio.saveAndFlush(usuarioCompleto("Joana", "333333"));

        salvo.getTelefones().clear();
        repositorio.saveAndFlush(salvo);
        gerenciador.clear();

        assertEquals(0, contar("telefone"));
    }

    @Test
    @DisplayName("orphanRemoval: desligar o endereco do usuario apaga a linha")
    void orphanRemovalDoEndereco() {
        Usuario salvo = repositorio.saveAndFlush(usuarioCompleto("Joana", "444444"));

        salvo.setEndereco(null);
        repositorio.saveAndFlush(salvo);
        gerenciador.clear();

        assertEquals(0, contar("endereco"));
    }

    @Test
    @DisplayName("cascade: excluir o usuario apaga endereco, documentos, telefones, e-mails, credenciais e veiculos")
    void cascadeAoExcluir() {
        Usuario usuario = usuarioCompleto("Joana", "555555");
        veiculo(usuario, "ABC1D23");
        Usuario salvo = repositorio.saveAndFlush(usuario);

        repositorio.delete(salvo);
        repositorio.flush();
        gerenciador.clear();

        for (String tabela : List.of("usuario", "usuario_perfis", "endereco", "documento", "telefone",
                "email", "credencial", "credencial_usuario_senha", "veiculo")) {
            assertEquals(0, contar(tabela), "tabela " + tabela + " nao ficou vazia");
        }
    }

    @Test
    @DisplayName("unique: dois documentos com o mesmo numero violam a restricao do banco")
    void numeroDeDocumentoEUnico() {
        repositorio.saveAndFlush(usuarioCompleto("Primeiro", "666666"));

        Usuario segundo = usuarioCompleto("Segundo", "666666");
        segundo.getCredenciais().clear();
        assertThrows(DataIntegrityViolationException.class, () -> repositorio.saveAndFlush(segundo));
    }

    @Test
    @DisplayName("unique: duas credenciais com o mesmo nome de usuario violam a restricao do banco")
    void nomeDeUsuarioEUnico() {
        repositorio.saveAndFlush(usuarioCompleto("Primeiro", "676767"));

        Usuario segundo = usuarioCompleto("Segundo", "686868");
        ((CredencialUsuarioSenha) segundo.getCredenciais().get(0)).setNomeUsuario("login-676767");
        assertThrows(DataIntegrityViolationException.class, () -> repositorio.saveAndFlush(segundo));
    }

    @Test
    @DisplayName("unique: dois veiculos com a mesma placa violam a restricao do banco")
    void placaEUnica() {
        Usuario primeiro = usuarioSimples("Primeiro", PerfilUsuario.CLIENTE);
        veiculo(primeiro, "ABC1D23");
        repositorio.saveAndFlush(primeiro);

        Usuario segundo = usuarioSimples("Segundo", PerfilUsuario.CLIENTE);
        veiculo(segundo, "ABC1D23");
        assertThrows(DataIntegrityViolationException.class, () -> repositorio.saveAndFlush(segundo));
    }

    @Test
    @DisplayName("IDENTITY: o id e gerado pelo banco a cada insercao")
    void idGeradoPeloBanco() {
        Usuario primeiro = repositorio.saveAndFlush(usuarioCompleto("Primeiro", "888888"));
        Usuario segundo = repositorio.saveAndFlush(usuarioCompleto("Segundo", "999999"));

        assertNotNull(primeiro.getId());
        assertTrue(segundo.getId() > primeiro.getId());
    }

    @Test
    @DisplayName("nullable: endereco sem cidade viola a restricao do banco")
    void camposObrigatoriosDoEndereco() {
        Usuario usuario = usuarioCompleto("Joana", "101010");
        usuario.getEndereco().setCidade(null);

        assertThrows(Exception.class, () -> repositorio.saveAndFlush(usuario));
    }

    @Test
    @DisplayName("as colecoes filhas voltam do banco ordenadas por id")
    void colecoesOrdenadasPorId() {
        Usuario usuario = usuarioCompleto("Joana", "141414");
        Telefone segundo = new Telefone();
        segundo.setDdd("11");
        segundo.setNumero("999999999");
        usuario.getTelefones().add(segundo);

        Usuario salvo = repositorio.saveAndFlush(usuario);
        gerenciador.clear();

        Usuario lido = repositorio.findById(salvo.getId()).orElseThrow();

        assertEquals(2, lido.getTelefones().size());
        assertTrue(lido.getTelefones().get(0).getId() < lido.getTelefones().get(1).getId());
    }

    @Test
    @DisplayName("venda: o mesmo servico, a mesma mercadoria e o mesmo carro entram em duas vendas")
    void mesmoItemEmDuasVendas() {
        Usuario cliente = usuarioSimples("Cliente", PerfilUsuario.CLIENTE);
        Veiculo carro = veiculo(cliente, "ABC1D23");
        Usuario funcionario = usuarioSimples("Funcionario", PerfilUsuario.FUNCIONARIO);
        repositorio.save(cliente);
        repositorio.save(funcionario);

        Empresa empresa = empresaCom(cliente, funcionario, carro, "V1", "V2");
        empresaRepositorio.saveAndFlush(empresa);
        cliente.setEmpresa(empresa);
        funcionario.setEmpresa(empresa);
        repositorio.flush();
        gerenciador.clear();

        assertEquals(2, vendaRepositorio.count());
        assertEquals(2, contar("venda_servico"));
        assertEquals(2, contar("venda_mercadoria"));
        assertEquals(2, repositorio.findById(cliente.getId()).orElseThrow().getVendas().size());
        assertEquals(2, veiculoRepositorio.findById(carro.getId()).orElseThrow().getVendas().size());
        assertTrue(vendaRepositorio.existsByClienteIdOrFuncionarioId(funcionario.getId(), funcionario.getId()));
    }

    @Test
    @DisplayName("excluir a empresa apaga as vendas e o catalogo dela, e mantem os usuarios desvinculados")
    void excluirEmpresaMantemUsuarios() {
        Usuario cliente = usuarioSimples("Cliente", PerfilUsuario.CLIENTE);
        Veiculo carro = veiculo(cliente, "ABC1D23");
        Usuario funcionario = usuarioSimples("Funcionario", PerfilUsuario.FUNCIONARIO);
        repositorio.save(cliente);
        repositorio.save(funcionario);
        Empresa empresa = empresaRepositorio.saveAndFlush(empresaCom(cliente, funcionario, carro, "V1"));
        cliente.setEmpresa(empresa);
        funcionario.setEmpresa(empresa);
        repositorio.flush();
        gerenciador.clear();

        Empresa lida = empresaRepositorio.findById(empresa.getId()).orElseThrow();
        assertEquals(2, lida.getUsuarios().size());
        for (Usuario usuario : lida.getUsuarios()) {
            usuario.setEmpresa(null);
        }
        empresaRepositorio.delete(lida);
        empresaRepositorio.flush();
        gerenciador.clear();

        for (String tabela : List.of("empresa", "venda", "venda_servico", "venda_mercadoria",
                "mercadoria", "servico")) {
            assertEquals(0, contar(tabela), "tabela " + tabela + " nao ficou vazia");
        }
        assertEquals(2, repositorio.count());
        assertEquals(1, contar("veiculo"));
        Object vinculo = gerenciador.getEntityManager()
                .createNativeQuery("select empresa_id from usuario where id = :id")
                .setParameter("id", cliente.getId())
                .getSingleResult();
        assertNull(vinculo);
    }

    @Test
    @DisplayName("excluir a empresa sem limpar o vinculo dos usuarios viola a FK")
    void excluirEmpresaSemLimparUsuarios() {
        Usuario cliente = usuarioSimples("Cliente", PerfilUsuario.CLIENTE);
        repositorio.save(cliente);
        Empresa empresa = new Empresa();
        empresa.setRazaoSocial("Loja");
        empresa.setCadastro(new Date());
        empresaRepositorio.save(empresa);
        cliente.setEmpresa(empresa);
        repositorio.flush();
        gerenciador.clear();

        empresaRepositorio.delete(empresaRepositorio.findById(empresa.getId()).orElseThrow());

        assertThrows(DataIntegrityViolationException.class, () -> empresaRepositorio.flush());
    }

    @Test
    @DisplayName("excluir a empresa cuja mercadoria tem fornecedor mantem o fornecedor")
    void excluirEmpresaComFornecedorDeMercadoria() {
        Usuario fornecedor = usuarioSimples("Fornecedor", PerfilUsuario.FORNECEDOR);
        repositorio.save(fornecedor);
        Empresa empresa = new Empresa();
        empresa.setRazaoSocial("Loja");
        empresa.setCadastro(new Date());
        Mercadoria roda = mercadoria();
        empresa.getMercadorias().add(roda);
        empresaRepositorio.save(empresa);
        fornecedor.setEmpresa(empresa);
        fornecedor.getMercadorias().add(roda);
        repositorio.flush();
        gerenciador.clear();

        Empresa lida = empresaRepositorio.findById(empresa.getId()).orElseThrow();
        for (Usuario usuario : lida.getUsuarios()) {
            usuario.setEmpresa(null);
        }
        empresaRepositorio.delete(lida);
        empresaRepositorio.flush();
        gerenciador.clear();

        assertEquals(0, contar("mercadoria"));
        assertEquals(1, repositorio.count());
    }

    @Test
    @DisplayName("o telefone da empresa fica com empresa_id preenchido e usuario_id vazio")
    void telefoneDaEmpresa() {
        Empresa empresa = new Empresa();
        empresa.setRazaoSocial("Loja");
        empresa.setCadastro(new Date());
        Telefone telefone = new Telefone();
        telefone.setDdd("11");
        telefone.setNumero("986454527");
        empresa.getTelefones().add(telefone);
        empresaRepositorio.saveAndFlush(empresa);

        Object[] linha = (Object[]) gerenciador.getEntityManager()
                .createNativeQuery("select empresa_id, usuario_id from telefone where id = :id")
                .setParameter("id", telefone.getId())
                .getSingleResult();

        assertEquals(empresa.getId().longValue(), ((Number) linha[0]).longValue());
        assertNull(linha[1]);
    }

    @Test
    @DisplayName("LAZY: ler o id da empresa do usuario nao carrega a empresa")
    void lerIdDaEmpresaNaoCarregaEmpresa() {
        Usuario cliente = usuarioSimples("Cliente", PerfilUsuario.CLIENTE);
        repositorio.save(cliente);
        Empresa empresa = new Empresa();
        empresa.setRazaoSocial("Loja");
        empresa.setCadastro(new Date());
        empresaRepositorio.save(empresa);
        cliente.setEmpresa(empresa);
        repositorio.flush();
        gerenciador.clear();

        Usuario lido = repositorio.findById(cliente.getId()).orElseThrow();

        assertEquals(empresa.getId(), lido.getEmpresa().getId());
        assertFalse(Hibernate.isInitialized(lido.getEmpresa()));
    }

    @Test
    @DisplayName("excluir o fornecedor mantem a mercadoria no estoque, sem fornecedor")
    void excluirFornecedorMantemMercadoria() {
        Usuario fornecedor = usuarioSimples("Fornecedor", PerfilUsuario.FORNECEDOR);
        repositorio.save(fornecedor);
        Empresa empresa = new Empresa();
        empresa.setRazaoSocial("Loja");
        empresa.setCadastro(new Date());
        Mercadoria roda = mercadoria();
        empresa.getMercadorias().add(roda);
        empresaRepositorio.save(empresa);
        fornecedor.getMercadorias().add(roda);
        repositorio.saveAndFlush(fornecedor);
        gerenciador.clear();

        repositorio.delete(repositorio.findById(fornecedor.getId()).orElseThrow());
        repositorio.flush();
        gerenciador.clear();

        assertEquals(1, contar("mercadoria"));
        Object vinculo = gerenciador.getEntityManager()
                .createNativeQuery("select fornecedor_id from mercadoria").getSingleResult();
        assertNull(vinculo);
    }

    private Mercadoria mercadoria() {
        Mercadoria roda = new Mercadoria();
        roda.setNome("Roda");
        roda.setFabricacao(new Date());
        roda.setCadastro(new Date());
        roda.setQuantidade(10);
        roda.setValor(new BigDecimal("300.00"));
        return roda;
    }

    private Empresa empresaCom(Usuario cliente, Usuario funcionario, Veiculo carro, String... identificacoes) {
        Empresa empresa = new Empresa();
        empresa.setRazaoSocial("Loja");
        empresa.setCadastro(new Date());
        Mercadoria roda = mercadoria();
        Servico alinhamento = new Servico();
        alinhamento.setNome("Alinhamento");
        alinhamento.setValor(new BigDecimal("50.00"));
        empresa.getMercadorias().add(roda);
        empresa.getServicos().add(alinhamento);
        for (String identificacao : identificacoes) {
            Venda venda = new Venda();
            venda.setIdentificacao(identificacao);
            venda.setCadastro(new Date());
            venda.setCliente(cliente);
            venda.setFuncionario(funcionario);
            venda.setVeiculo(carro);
            venda.getMercadorias().add(roda);
            venda.getServicos().add(alinhamento);
            empresa.getVendas().add(venda);
        }
        return empresa;
    }
}
