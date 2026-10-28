package com.autobots.automanager.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Calendar;
import java.util.Date;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.autobots.automanager.entidades.Cliente;
import com.autobots.automanager.entidades.Documento;
import com.autobots.automanager.entidades.Endereco;
import com.autobots.automanager.entidades.Telefone;

class ClienteAtualizadorTest {

    private final ClienteAtualizador atualizador = new ClienteAtualizador();

    private Date data(int ano, int mes, int dia) {
        Calendar calendario = Calendar.getInstance();
        calendario.set(ano, mes, dia, 0, 0, 0);
        calendario.set(Calendar.MILLISECOND, 0);
        return calendario.getTime();
    }

    private Cliente clienteCompleto() {
        Cliente cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNome("Pedro de Alcantara");
        cliente.setNomeSocial("Dom Pedro");
        cliente.setDataNascimento(data(2002, Calendar.MAY, 15));
        cliente.setDataCadastro(data(2020, Calendar.JANUARY, 1));

        Endereco endereco = new Endereco();
        endereco.setCidade("Rio de Janeiro");
        endereco.setRua("Avenida Atlantica");
        endereco.setNumero("1702");
        endereco.setId(300L);
        cliente.getEnderecos().add(endereco);

        Telefone telefone = new Telefone();
        telefone.setId(200L);
        telefone.setDdd("21");
        telefone.setNumero("981234576");
        cliente.getTelefones().add(telefone);

        Documento documento = new Documento();
        documento.setId(200L);
        documento.setTipo("RG");
        documento.setNumero("1500");
        cliente.getDocumentos().add(documento);

        return cliente;
    }

    @Test
    @DisplayName("atualiza os dados simples enviados")
    void atualizaDadosSimples() {
        Cliente cliente = clienteCompleto();
        Cliente atualizacao = new Cliente();
        atualizacao.setNome("Pedro de Alcantara e Bourbon");
        atualizacao.setNomeSocial("Dom Pedro I");

        atualizador.atualizar(cliente, atualizacao);

        assertEquals("Pedro de Alcantara e Bourbon", cliente.getNome());
        assertEquals("Dom Pedro I", cliente.getNomeSocial());
    }

    @Test
    @DisplayName("nao sobrescreve dados ausentes na atualizacao")
    void naoSobrescreveDadosAusentes() {
        Cliente cliente = clienteCompleto();
        Cliente atualizacao = new Cliente();
        atualizacao.setNomeSocial("Dom Pedro I");

        atualizador.atualizar(cliente, atualizacao);

        assertEquals("Pedro de Alcantara", cliente.getNome());
        assertEquals("Dom Pedro I", cliente.getNomeSocial());
        assertEquals(data(2020, Calendar.JANUARY, 1), cliente.getDataCadastro());
    }

    @Test
    @DisplayName("delega a atualizacao do endereco casando por id")
    void delegaEndereco() {
        Cliente cliente = clienteCompleto();
        Cliente atualizacao = new Cliente();
        Endereco endereco = new Endereco();
        endereco.setId(300L);
        endereco.setCidade("Santos");
        atualizacao.getEnderecos().add(endereco);

        atualizador.atualizar(cliente, atualizacao);

        assertEquals("Santos", cliente.getEnderecos().get(0).getCidade());
        assertEquals("Avenida Atlantica", cliente.getEnderecos().get(0).getRua());
    }

    @Test
    @DisplayName("ignora endereco enviado sem id, como faz com telefone e documento")
    void ignoraEnderecoSemId() {
        Cliente cliente = clienteCompleto();
        Cliente atualizacao = new Cliente();
        Endereco endereco = new Endereco();
        endereco.setCidade("Santos");
        atualizacao.getEnderecos().add(endereco);

        atualizador.atualizar(cliente, atualizacao);

        assertEquals(1, cliente.getEnderecos().size());
        assertEquals("Rio de Janeiro", cliente.getEnderecos().get(0).getCidade());
    }

    @Test
    @DisplayName("nao altera endereco de id diferente")
    void naoAlteraEnderecoDeIdDiferente() {
        Cliente cliente = clienteCompleto();
        Cliente atualizacao = new Cliente();
        Endereco endereco = new Endereco();
        endereco.setId(301L);
        endereco.setCidade("Santos");
        atualizacao.getEnderecos().add(endereco);

        atualizador.atualizar(cliente, atualizacao);

        assertEquals("Rio de Janeiro", cliente.getEnderecos().get(0).getCidade());
    }

    @Test
    @DisplayName("REGRESSAO P1: delega telefone e documento casando ids fora do cache de Long")
    void delegaTelefoneEDocumentoComIdGrande() {
        Cliente cliente = clienteCompleto();
        Cliente atualizacao = new Cliente();

        Telefone telefone = new Telefone();
        telefone.setId(200L);
        telefone.setDdd("11");
        atualizacao.getTelefones().add(telefone);

        Documento documento = new Documento();
        documento.setId(200L);
        documento.setTipo("CNH");
        atualizacao.getDocumentos().add(documento);

        atualizador.atualizar(cliente, atualizacao);

        assertEquals("11", cliente.getTelefones().get(0).getDdd());
        assertEquals("981234576", cliente.getTelefones().get(0).getNumero());
        assertEquals("CNH", cliente.getDocumentos().get(0).getTipo());
        assertEquals("1500", cliente.getDocumentos().get(0).getNumero());
    }
}
