package com.autobots.automanager;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
import com.autobots.automanager.repositorios.EmpresaRepositorio;
import com.autobots.automanager.repositorios.UsuarioRepositorio;

@Component
public class CargaInicial implements ApplicationRunner {

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;
    @Autowired
    private EmpresaRepositorio empresaRepositorio;

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        if (usuarioRepositorio.count() > 0) {
            return;
        }
        Date agora = Calendar.getInstance().getTime();

        Usuario funcionario = usuario("Pedro Alcantara de Braganca e Bourbon", "Dom Pedro",
                PerfilUsuario.FUNCIONARIO, agora);
        funcionario.setEndereco(endereco("Sao Paulo", "Sao Paulo", "Jardins",
                "Avenida Sao Gabriel", "00", "01435001"));
        funcionario.getTelefones().add(telefone("11", "985463372"));
        funcionario.getDocumentos().add(documento(TipoDocumento.CPF, "85647381922", data(2010, Calendar.MARCH, 10)));
        funcionario.getEmails().add(email("a@a.com"));
        funcionario.getCredenciais().add(credencial("dompedrofuncionario", agora));

        Usuario fornecedor = usuario("Componentes varejo de partes automotivas ltda",
                "Loja do carro, vendas de componentes automotivos", PerfilUsuario.FORNECEDOR, agora);
        fornecedor.setEndereco(endereco("Rio de Janeiro", "Rio de Janeiro", "Centro",
                "Avenida Republica do Chile", "00", "20031170"));
        fornecedor.getDocumentos().add(documento(TipoDocumento.CNPJ, "00014556000100", data(2005, Calendar.JUNE, 1)));
        fornecedor.getEmails().add(email("f@f.com"));
        fornecedor.getCredenciais().add(credencial("dompedrofornecedor", agora));

        Usuario cliente = usuario("Pedro Alcantara de Braganca e Bourbon", "Dom pedro cliente",
                PerfilUsuario.CLIENTE, agora);
        cliente.setEndereco(endereco("Sao Paulo", "Sao Jose dos Campos", "Centro",
                "Avenida Doutor Nelson D'Avila", "00", "12245070"));
        cliente.getDocumentos().add(documento(TipoDocumento.CPF, "12584698533", data(2012, Calendar.AUGUST, 20)));
        cliente.getEmails().add(email("c@c.com"));
        cliente.getCredenciais().add(credencial("dompedrocliente", agora));

        Veiculo veiculo = new Veiculo();
        veiculo.setTipo(TipoVeiculo.SUV);
        veiculo.setModelo("Corolla Cross");
        veiculo.setPlaca("ABC0000");
        veiculo.setProprietario(cliente);
        cliente.getVeiculos().add(veiculo);

        usuarioRepositorio.save(funcionario);
        usuarioRepositorio.save(fornecedor);
        usuarioRepositorio.save(cliente);

        Empresa empresa = new Empresa();
        empresa.setRazaoSocial("Car service toyota ltda");
        empresa.setNomeFantasia("Car service manutencao veicular");
        empresa.setCadastro(agora);
        empresa.setEndereco(endereco("Sao Paulo", "Sao Paulo", "Centro", "Avenida Sao Joao", "00", "01035000"));
        empresa.getTelefones().add(telefone("11", "986454527"));
        empresa.getUsuarios().add(funcionario);
        empresa.getUsuarios().add(fornecedor);
        empresa.getUsuarios().add(cliente);

        Mercadoria roda = new Mercadoria();
        roda.setNome("Roda de liga leve modelo toyota etios");
        roda.setDescricao("Roda de liga leve original de fabrica da toyota para modelos do tipo hatch");
        roda.setFabricacao(data(2025, Calendar.JANUARY, 15));
        roda.setCadastro(agora);
        roda.setQuantidade(30);
        roda.setValor(new BigDecimal("300.00"));
        empresa.getMercadorias().add(roda);

        Servico trocaRodas = servico("Troca de rodas", "Troca das rodas do carro por novas", "50.00");
        Servico alinhamento = servico("Alinhamento de rodas", "Alinhamento das rodas do carro", "50.00");
        Servico balanceamento = servico("Balanceamento de rodas", "Balanceamento das rodas do carro", "30.00");
        empresa.getServicos().add(trocaRodas);
        empresa.getServicos().add(alinhamento);
        empresa.getServicos().add(balanceamento);

        empresa.getVendas().add(venda("1234698745", agora, cliente, funcionario, veiculo, roda,
                trocaRodas, alinhamento));
        empresa.getVendas().add(venda("1234698749", agora, cliente, funcionario, veiculo, roda,
                balanceamento, alinhamento));
        empresaRepositorio.save(empresa);

        fornecedor.getMercadorias().add(roda);
    }

    private Usuario usuario(String nome, String nomeSocial, PerfilUsuario perfil, Date agora) {
        Usuario usuario = new Usuario();
        usuario.setNome(nome);
        usuario.setNomeSocial(nomeSocial);
        usuario.setDataCadastro(agora);
        usuario.getPerfis().add(perfil);
        return usuario;
    }

    private Endereco endereco(String estado, String cidade, String bairro, String rua,
            String numero, String codigoPostal) {
        Endereco endereco = new Endereco();
        endereco.setEstado(estado);
        endereco.setCidade(cidade);
        endereco.setBairro(bairro);
        endereco.setRua(rua);
        endereco.setNumero(numero);
        endereco.setCodigoPostal(codigoPostal);
        return endereco;
    }

    private Telefone telefone(String ddd, String numero) {
        Telefone telefone = new Telefone();
        telefone.setDdd(ddd);
        telefone.setNumero(numero);
        return telefone;
    }

    private Documento documento(TipoDocumento tipo, String numero, Date dataEmissao) {
        Documento documento = new Documento();
        documento.setTipo(tipo);
        documento.setNumero(numero);
        documento.setDataEmissao(dataEmissao);
        return documento;
    }

    private Email email(String endereco) {
        Email email = new Email();
        email.setEndereco(endereco);
        return email;
    }

    private CredencialUsuarioSenha credencial(String nomeUsuario, Date agora) {
        CredencialUsuarioSenha credencial = new CredencialUsuarioSenha();
        credencial.setNomeUsuario(nomeUsuario);
        credencial.setSenha("123456");
        credencial.setCriacao(agora);
        return credencial;
    }

    private Servico servico(String nome, String descricao, String valor) {
        Servico servico = new Servico();
        servico.setNome(nome);
        servico.setDescricao(descricao);
        servico.setValor(new BigDecimal(valor));
        return servico;
    }

    private Venda venda(String identificacao, Date agora, Usuario cliente, Usuario funcionario,
            Veiculo veiculo, Mercadoria mercadoria, Servico... servicos) {
        Venda venda = new Venda();
        venda.setIdentificacao(identificacao);
        venda.setCadastro(agora);
        venda.setCliente(cliente);
        venda.setFuncionario(funcionario);
        venda.setVeiculo(veiculo);
        venda.getMercadorias().add(mercadoria);
        for (Servico servico : servicos) {
            venda.getServicos().add(servico);
        }
        return venda;
    }

    private Date data(int ano, int mes, int dia) {
        Calendar calendario = Calendar.getInstance();
        calendario.set(ano, mes, dia, 0, 0, 0);
        return calendario.getTime();
    }
}
