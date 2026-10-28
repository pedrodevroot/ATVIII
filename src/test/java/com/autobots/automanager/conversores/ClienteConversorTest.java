package com.autobots.automanager.conversores;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.autobots.automanager.dtos.ClienteAtualizacaoDto;
import com.autobots.automanager.dtos.ClienteCadastroDto;
import com.autobots.automanager.dtos.ClienteRespostaDto;
import com.autobots.automanager.dtos.DocumentoCadastroDto;
import com.autobots.automanager.dtos.DocumentoDto;
import com.autobots.automanager.dtos.EnderecoCadastroDto;
import com.autobots.automanager.dtos.EnderecoDto;
import com.autobots.automanager.dtos.TelefoneCadastroDto;
import com.autobots.automanager.dtos.TelefoneDto;
import com.autobots.automanager.entidades.Cliente;
import com.autobots.automanager.entidades.Documento;
import com.autobots.automanager.entidades.Endereco;
import com.autobots.automanager.entidades.Telefone;

class ClienteConversorTest {

    private final ClienteConversor conversor = new ClienteConversor();

    private Date data(int ano, int mes, int dia) {
        Calendar calendario = Calendar.getInstance();
        calendario.set(ano, mes, dia, 0, 0, 0);
        calendario.set(Calendar.MILLISECOND, 0);
        return calendario.getTime();
    }

    private ClienteCadastroDto cadastroCompleto() {
        ClienteCadastroDto dto = new ClienteCadastroDto();
        dto.setNome("Joana Ferreira");
        dto.setNomeSocial("Joana");
        dto.setDataNascimento(data(1995, Calendar.AUGUST, 20));

        EnderecoCadastroDto endereco = new EnderecoCadastroDto();
        endereco.setCidade("Santos");
        endereco.setRua("Avenida Ana Costa");
        endereco.setNumero("250");
        dto.getEnderecos().add(endereco);

        DocumentoCadastroDto documento = new DocumentoCadastroDto();
        documento.setTipo("RG");
        documento.setNumero("998877");
        dto.getDocumentos().add(documento);

        TelefoneCadastroDto telefone = new TelefoneCadastroDto();
        telefone.setDdd("13");
        telefone.setNumero("981234567");
        dto.getTelefones().add(telefone);

        return dto;
    }

    @Test
    @DisplayName("cadastro: copia os dados simples")
    void cadastroCopiaDadosSimples() {
        Cliente cliente = conversor.paraEntidade(cadastroCompleto());

        assertEquals("Joana Ferreira", cliente.getNome());
        assertEquals("Joana", cliente.getNomeSocial());
        assertEquals(data(1995, Calendar.AUGUST, 20), cliente.getDataNascimento());
    }

    @Test
    @DisplayName("cadastro: a entidade nasce sem id, pois o DTO nao possui esse campo")
    void cadastroNasceSemId() {
        Cliente cliente = conversor.paraEntidade(cadastroCompleto());

        assertNull(cliente.getId());
    }

    @Test
    @DisplayName("cadastro: nao define dataCadastro, que e responsabilidade do servico")
    void cadastroNaoDefineDataCadastro() {
        Cliente cliente = conversor.paraEntidade(cadastroCompleto());

        assertNull(cliente.getDataCadastro());
    }

    @Test
    @DisplayName("cadastro: os filhos tambem nascem sem id")
    void cadastroFilhosNascemSemId() {
        Cliente cliente = conversor.paraEntidade(cadastroCompleto());

        assertNull(cliente.getEnderecos().get(0).getId());
        assertNull(cliente.getDocumentos().get(0).getId());
        assertNull(cliente.getTelefones().get(0).getId());
    }

    @Test
    @DisplayName("cadastro: converte endereco, documentos e telefones")
    void cadastroConverteFilhos() {
        Cliente cliente = conversor.paraEntidade(cadastroCompleto());

        assertEquals("Santos", cliente.getEnderecos().get(0).getCidade());
        assertEquals(1, cliente.getDocumentos().size());
        assertEquals("RG", cliente.getDocumentos().get(0).getTipo());
        assertEquals(1, cliente.getTelefones().size());
        assertEquals("13", cliente.getTelefones().get(0).getDdd());
    }

    @Test
    @DisplayName("cadastro: aceita endereco ausente sem estourar")
    void cadastroSemEndereco() {
        ClienteCadastroDto dto = new ClienteCadastroDto();
        dto.setNome("Sem Endereco");

        Cliente cliente = conversor.paraEntidade(dto);

        assertTrue(cliente.getEnderecos().isEmpty());
        assertTrue(cliente.getDocumentos().isEmpty());
        assertTrue(cliente.getTelefones().isEmpty());
    }

    @Test
    @DisplayName("atualizacao: copia o id do cliente")
    void atualizacaoCopiaIdDoCliente() {
        ClienteAtualizacaoDto dto = new ClienteAtualizacaoDto();
        dto.setId(42L);
        dto.setNome("Nome Novo");

        Cliente cliente = conversor.paraEntidade(dto);

        assertEquals(42L, cliente.getId());
        assertEquals("Nome Novo", cliente.getNome());
    }

    @Test
    @DisplayName("atualizacao: PRESERVA o id dos filhos, pois os Atualizadores casam por id")
    void atualizacaoPreservaIdDosFilhos() {
        ClienteAtualizacaoDto dto = new ClienteAtualizacaoDto();
        dto.setId(1L);

        DocumentoDto documento = new DocumentoDto();
        documento.setId(200L);
        documento.setTipo("CNH");
        dto.getDocumentos().add(documento);

        TelefoneDto telefone = new TelefoneDto();
        telefone.setId(300L);
        telefone.setDdd("11");
        dto.getTelefones().add(telefone);

        Cliente cliente = conversor.paraEntidade(dto);

        assertEquals(200L, cliente.getDocumentos().get(0).getId());
        assertEquals(300L, cliente.getTelefones().get(0).getId());
    }

    @Test
    @DisplayName("atualizacao: nao define dataCadastro, pois o instante do cadastro e imutavel")
    void atualizacaoNaoDefineDataCadastro() {
        ClienteAtualizacaoDto dto = new ClienteAtualizacaoDto();
        dto.setId(1L);
        dto.setNome("Nome Novo");

        Cliente cliente = conversor.paraEntidade(dto);

        assertNull(cliente.getDataCadastro());
    }

    @Test
    @DisplayName("atualizacao: converte o endereco enviado")
    void atualizacaoConverteEndereco() {
        ClienteAtualizacaoDto dto = new ClienteAtualizacaoDto();
        dto.setId(1L);
        EnderecoDto endereco = new EnderecoDto();
        endereco.setCodigoPostal("11015200");
        dto.getEnderecos().add(endereco);

        Cliente cliente = conversor.paraEntidade(dto);

        assertEquals(1, cliente.getEnderecos().size());
        assertEquals("11015200", cliente.getEnderecos().get(0).getCodigoPostal());
    }

    @Test
    @DisplayName("resposta: expoe id e dataCadastro")
    void respostaExpoeIdEDataCadastro() {
        Cliente cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNome("Pedro de Alcantara");
        cliente.setNomeSocial("Dom Pedro");
        cliente.setDataNascimento(data(2002, Calendar.MAY, 15));
        cliente.setDataCadastro(data(2020, Calendar.JANUARY, 1));

        ClienteRespostaDto dto = conversor.paraResposta(cliente);

        assertEquals(1L, dto.getId());
        assertEquals("Pedro de Alcantara", dto.getNome());
        assertEquals("Dom Pedro", dto.getNomeSocial());
        assertEquals(data(2002, Calendar.MAY, 15), dto.getDataNascimento());
        assertEquals(data(2020, Calendar.JANUARY, 1), dto.getDataCadastro());
    }

    @Test
    @DisplayName("resposta: converte endereco, documentos e telefones com seus ids")
    void respostaConverteFilhos() {
        Cliente cliente = new Cliente();
        cliente.setId(1L);

        Endereco endereco = new Endereco();
        endereco.setId(3L);
        endereco.setCidade("Rio de Janeiro");
        cliente.getEnderecos().add(endereco);

        Documento documento = new Documento();
        documento.setId(5L);
        documento.setTipo("RG");
        cliente.getDocumentos().add(documento);

        Telefone telefone = new Telefone();
        telefone.setId(7L);
        telefone.setDdd("21");
        cliente.getTelefones().add(telefone);

        ClienteRespostaDto dto = conversor.paraResposta(cliente);

        assertEquals(3L, dto.getEnderecos().get(0).getId());
        assertEquals("Rio de Janeiro", dto.getEnderecos().get(0).getCidade());
        assertEquals(5L, dto.getDocumentos().get(0).getId());
        assertEquals(7L, dto.getTelefones().get(0).getId());
    }

    @Test
    @DisplayName("resposta: aceita cliente sem endereco sem estourar")
    void respostaSemEndereco() {
        Cliente cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNome("Sem Endereco");

        ClienteRespostaDto dto = conversor.paraResposta(cliente);

        assertTrue(dto.getEnderecos().isEmpty());
        assertTrue(dto.getDocumentos().isEmpty());
        assertTrue(dto.getTelefones().isEmpty());
    }

    @Test
    @DisplayName("resposta em lista: converte todos os clientes")
    void respostaEmLista() {
        Cliente primeiro = new Cliente();
        primeiro.setId(1L);
        primeiro.setNome("Primeiro");
        Cliente segundo = new Cliente();
        segundo.setId(2L);
        segundo.setNome("Segundo");

        List<ClienteRespostaDto> dtos = conversor.paraResposta(List.of(primeiro, segundo));

        assertEquals(2, dtos.size());
        assertEquals("Primeiro", dtos.get(0).getNome());
        assertEquals("Segundo", dtos.get(1).getNome());
    }
}
