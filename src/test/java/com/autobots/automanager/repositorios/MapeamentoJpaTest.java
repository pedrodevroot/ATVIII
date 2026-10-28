package com.autobots.automanager.repositorios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;
import java.util.Calendar;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import com.autobots.automanager.entidades.Cliente;
import com.autobots.automanager.entidades.Documento;
import com.autobots.automanager.entidades.Endereco;
import com.autobots.automanager.entidades.Telefone;

@DataJpaTest
class MapeamentoJpaTest {

    @Autowired
    private ClienteRepositorio repositorio;
    @Autowired
    private TestEntityManager gerenciador;

    private Cliente clienteCompleto(String nome, String numeroDocumento) {
        Cliente cliente = new Cliente();
        cliente.setNome(nome);
        cliente.setNomeSocial("Apelido");
        Calendar calendario = Calendar.getInstance();
        calendario.set(1995, Calendar.AUGUST, 20);
        cliente.setDataNascimento(calendario.getTime());
        cliente.setDataCadastro(Calendar.getInstance().getTime());

        Endereco endereco = new Endereco();
        endereco.setEstado("Sao Paulo");
        endereco.setCidade("Santos");
        endereco.setBairro("Gonzaga");
        endereco.setRua("Avenida Ana Costa");
        endereco.setNumero("250");
        endereco.setCodigoPostal("11060002");
        cliente.getEnderecos().add(endereco);

        Documento documento = new Documento();
        documento.setTipo("RG");
        documento.setNumero(numeroDocumento);
        cliente.getDocumentos().add(documento);

        Telefone telefone = new Telefone();
        telefone.setDdd("13");
        telefone.setNumero("981234567");
        cliente.getTelefones().add(telefone);

        return cliente;
    }

    private List<?> tabelas() {
        return gerenciador.getEntityManager().createNativeQuery(
                "select table_name from information_schema.tables "
                        + "where table_schema = 'PUBLIC' order by table_name")
                .getResultList();
    }

    @Test
    @DisplayName("P13: o schema tem exatamente 4 tabelas, sem tabelas de juncao")
    void schemaComQuatroTabelas() {
        List<?> nomes = tabelas();

        assertEquals(4, nomes.size(), "esperado 4 tabelas, encontrado: " + nomes);
        assertTrue(nomes.contains("CLIENTE"));
        assertTrue(nomes.contains("DOCUMENTO"));
        assertTrue(nomes.contains("ENDERECO"));
        assertTrue(nomes.contains("TELEFONE"));
    }

    @Test
    @DisplayName("cascade: salvar o cliente persiste endereco, documentos e telefones")
    void cascadeAoSalvar() {
        Cliente salvo = repositorio.saveAndFlush(clienteCompleto("Joana", "111111"));
        gerenciador.clear();

        Cliente lido = repositorio.findById(salvo.getId()).orElseThrow();

        assertNotNull(lido.getEnderecos().get(0).getId());
        assertEquals("Santos", lido.getEnderecos().get(0).getCidade());
        assertEquals(1, lido.getDocumentos().size());
        assertNotNull(lido.getDocumentos().get(0).getId());
        assertEquals(1, lido.getTelefones().size());
        assertNotNull(lido.getTelefones().get(0).getId());
    }

    @Test
    @DisplayName("P13: a FK cliente_id fica na tabela telefone e vem preenchida")
    void fkNaTabelaFilha() {
        Cliente salvo = repositorio.saveAndFlush(clienteCompleto("Joana", "222222"));
        Long idTelefone = salvo.getTelefones().get(0).getId();

        Object fk = gerenciador.getEntityManager()
                .createNativeQuery("select cliente_id from telefone where id = :id")
                .setParameter("id", idTelefone)
                .getSingleResult();

        assertEquals(salvo.getId().longValue(), ((Number) fk).longValue());
    }

    @Test
    @DisplayName("orphanRemoval: remover o telefone da colecao apaga a linha")
    void orphanRemovalDoTelefone() {
        Cliente salvo = repositorio.saveAndFlush(clienteCompleto("Joana", "333333"));

        salvo.getTelefones().clear();
        repositorio.saveAndFlush(salvo);
        gerenciador.clear();

        Object total = gerenciador.getEntityManager()
                .createNativeQuery("select count(*) from telefone")
                .getSingleResult();

        assertEquals(0L, ((Number) total).longValue());
    }

    @Test
    @DisplayName("orphanRemoval: remover o endereco da colecao apaga a linha")
    void orphanRemovalDoEndereco() {
        Cliente salvo = repositorio.saveAndFlush(clienteCompleto("Joana", "444444"));

        salvo.getEnderecos().clear();
        repositorio.saveAndFlush(salvo);
        gerenciador.clear();

        Object total = gerenciador.getEntityManager()
                .createNativeQuery("select count(*) from endereco")
                .getSingleResult();

        assertEquals(0L, ((Number) total).longValue());
    }

    @Test
    @DisplayName("cascade: excluir o cliente apaga endereco, documentos e telefones")
    void cascadeAoExcluir() {
        Cliente salvo = repositorio.saveAndFlush(clienteCompleto("Joana", "555555"));

        repositorio.delete(salvo);
        repositorio.flush();
        gerenciador.clear();

        for (String tabela : List.of("cliente", "endereco", "documento", "telefone")) {
            Object total = gerenciador.getEntityManager()
                    .createNativeQuery("select count(*) from " + tabela)
                    .getSingleResult();
            assertEquals(0L, ((Number) total).longValue(), "tabela " + tabela + " nao ficou vazia");
        }
    }

    @Test
    @DisplayName("unique: dois documentos com o mesmo numero violam a restricao do banco")
    void numeroDeDocumentoEUnico() {
        repositorio.saveAndFlush(clienteCompleto("Primeiro", "666666"));

        assertThrows(DataIntegrityViolationException.class,
                () -> repositorio.saveAndFlush(clienteCompleto("Segundo", "666666")));
    }

    @Test
    @DisplayName("findById: encontra o cliente e devolve vazio quando o id nao existe")
    void findByIdEncontraOuDevolveVazio() {
        Cliente salvo = repositorio.saveAndFlush(clienteCompleto("Joana", "777777"));

        assertTrue(repositorio.findById(salvo.getId()).isPresent());
        assertTrue(repositorio.findById(999999L).isEmpty());
    }

    @Test
    @DisplayName("IDENTITY: o id e gerado pelo banco a cada insercao")
    void idGeradoPeloBanco() {
        Cliente primeiro = repositorio.saveAndFlush(clienteCompleto("Primeiro", "888888"));
        Cliente segundo = repositorio.saveAndFlush(clienteCompleto("Segundo", "999999"));

        assertNotNull(primeiro.getId());
        assertNotNull(segundo.getId());
        assertTrue(segundo.getId() > primeiro.getId());
    }

    @Test
    @DisplayName("nullable: endereco sem cidade, rua ou numero viola a restricao do banco")
    void camposObrigatoriosDoEndereco() {
        Cliente cliente = clienteCompleto("Joana", "101010");
        cliente.getEnderecos().get(0).setCidade(null);

        assertThrows(Exception.class, () -> repositorio.saveAndFlush(cliente));
    }

    @Test
    @DisplayName("count: reflete a quantidade de clientes inseridos")
    void countReflete() {
        assertEquals(0, repositorio.count());

        repositorio.saveAndFlush(clienteCompleto("Primeiro", "121212"));
        repositorio.saveAndFlush(clienteCompleto("Segundo", "131313"));

        assertEquals(2, repositorio.count());
        assertEquals(BigInteger.valueOf(2).longValue(), repositorio.count());
    }

    @Test
    @DisplayName("P18: as colecoes filhas voltam do banco ordenadas por id")
    void colecoesOrdenadasPorId() {
        Cliente cliente = clienteCompleto("Joana", "141414");

        Telefone segundo = new Telefone();
        segundo.setDdd("11");
        segundo.setNumero("999999999");
        cliente.getTelefones().add(segundo);

        Endereco segundoEndereco = new Endereco();
        segundoEndereco.setCidade("Rio de Janeiro");
        segundoEndereco.setRua("Avenida Atlantica");
        segundoEndereco.setNumero("1702");
        cliente.getEnderecos().add(segundoEndereco);

        Cliente salvo = repositorio.saveAndFlush(cliente);
        gerenciador.clear();

        Cliente lido = repositorio.findById(salvo.getId()).orElseThrow();

        assertEquals(2, lido.getTelefones().size());
        assertTrue(lido.getTelefones().get(0).getId() < lido.getTelefones().get(1).getId());
        assertEquals(2, lido.getEnderecos().size());
        assertTrue(lido.getEnderecos().get(0).getId() < lido.getEnderecos().get(1).getId());
    }
}
