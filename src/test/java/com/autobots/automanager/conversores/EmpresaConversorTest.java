package com.autobots.automanager.conversores;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.autobots.automanager.dtos.EmpresaAtualizacaoDto;
import com.autobots.automanager.dtos.EmpresaCadastroDto;
import com.autobots.automanager.dtos.EmpresaRespostaDto;
import com.autobots.automanager.dtos.EnderecoAtualizacaoDto;
import com.autobots.automanager.dtos.EnderecoCadastroDto;
import com.autobots.automanager.dtos.TelefoneCadastroDto;
import com.autobots.automanager.dtos.TelefoneDto;
import com.autobots.automanager.entidades.Empresa;
import com.autobots.automanager.entidades.Endereco;
import com.autobots.automanager.entidades.Telefone;

class EmpresaConversorTest {

    private final EmpresaConversor conversor = new EmpresaConversor();

    @Test
    @DisplayName("cadastro: copia os dados e os filhos, sem id e sem data de cadastro")
    void cadastroCompleto() {
        EmpresaCadastroDto dto = new EmpresaCadastroDto();
        dto.setRazaoSocial("Car Service Ltda");
        dto.setNomeFantasia("Oficina");
        EnderecoCadastroDto endereco = new EnderecoCadastroDto();
        endereco.setCidade("Sao Paulo");
        dto.setEndereco(endereco);
        TelefoneCadastroDto telefone = new TelefoneCadastroDto();
        telefone.setDdd("11");
        dto.getTelefones().add(telefone);

        Empresa empresa = conversor.paraEntidade(dto);

        assertNull(empresa.getId());
        assertNull(empresa.getCadastro());
        assertEquals("Car Service Ltda", empresa.getRazaoSocial());
        assertEquals("Oficina", empresa.getNomeFantasia());
        assertEquals("Sao Paulo", empresa.getEndereco().getCidade());
        assertEquals("11", empresa.getTelefones().get(0).getDdd());
    }

    @Test
    @DisplayName("cadastro: aceita empresa sem endereco e sem telefones")
    void cadastroSemFilhos() {
        EmpresaCadastroDto dto = new EmpresaCadastroDto();
        dto.setRazaoSocial("Car Service Ltda");

        Empresa empresa = conversor.paraEntidade(dto);

        assertNull(empresa.getEndereco());
        assertTrue(empresa.getTelefones().isEmpty());
    }

    @Test
    @DisplayName("atualizacao: copia o id da empresa e PRESERVA o id dos telefones")
    void atualizacaoPreservaIds() {
        EmpresaAtualizacaoDto dto = new EmpresaAtualizacaoDto();
        dto.setId(42L);
        dto.setRazaoSocial("Novo Nome");
        EnderecoAtualizacaoDto endereco = new EnderecoAtualizacaoDto();
        endereco.setCidade("Santos");
        dto.setEndereco(endereco);
        TelefoneDto telefone = new TelefoneDto();
        telefone.setId(300L);
        dto.getTelefones().add(telefone);

        Empresa empresa = conversor.paraEntidade(dto);

        assertEquals(42L, empresa.getId());
        assertEquals("Novo Nome", empresa.getRazaoSocial());
        assertEquals("Santos", empresa.getEndereco().getCidade());
        assertEquals(300L, empresa.getTelefones().get(0).getId());
    }

    @Test
    @DisplayName("resposta: expoe os dados, o endereco e os telefones com seus ids")
    void respostaCompleta() {
        Date cadastro = new Date(0L);
        Empresa empresa = new Empresa();
        empresa.setId(1L);
        empresa.setRazaoSocial("Car Service Ltda");
        empresa.setCadastro(cadastro);
        Endereco endereco = new Endereco();
        endereco.setId(3L);
        empresa.setEndereco(endereco);
        Telefone telefone = new Telefone();
        telefone.setId(7L);
        empresa.getTelefones().add(telefone);

        EmpresaRespostaDto dto = conversor.paraResposta(empresa);

        assertEquals(1L, dto.getId());
        assertEquals("Car Service Ltda", dto.getRazaoSocial());
        assertEquals(cadastro, dto.getCadastro());
        assertEquals(3L, dto.getEndereco().getId());
        assertEquals(7L, dto.getTelefones().get(0).getId());
    }

    @Test
    @DisplayName("resposta: aceita empresa sem endereco")
    void respostaSemEndereco() {
        Empresa empresa = new Empresa();
        empresa.setId(1L);

        assertNull(conversor.paraResposta(empresa).getEndereco());
    }

    @Test
    @DisplayName("resposta em lista: converte todas as empresas")
    void respostaEmLista() {
        Empresa primeira = new Empresa();
        primeira.setId(1L);
        Empresa segunda = new Empresa();
        segunda.setId(2L);

        List<EmpresaRespostaDto> dtos = conversor.paraResposta(List.of(primeira, segunda));

        assertEquals(2, dtos.size());
        assertEquals(2L, dtos.get(1).getId());
    }
}
