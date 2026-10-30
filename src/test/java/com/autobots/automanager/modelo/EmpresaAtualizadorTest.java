package com.autobots.automanager.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.autobots.automanager.entidades.Empresa;
import com.autobots.automanager.entidades.Endereco;
import com.autobots.automanager.entidades.Telefone;

class EmpresaAtualizadorTest {

    private final EmpresaAtualizador atualizador = new EmpresaAtualizador();

    private Empresa empresaCompleta() {
        Empresa empresa = new Empresa();
        empresa.setId(1L);
        empresa.setRazaoSocial("Car Service Ltda");
        empresa.setNomeFantasia("Oficina");

        Endereco endereco = new Endereco();
        endereco.setId(300L);
        endereco.setCidade("Sao Paulo");
        endereco.setRua("Avenida Sao Joao");
        empresa.setEndereco(endereco);

        Telefone telefone = new Telefone();
        telefone.setId(200L);
        telefone.setDdd("11");
        telefone.setNumero("986454527");
        empresa.getTelefones().add(telefone);

        return empresa;
    }

    @Test
    @DisplayName("atualiza so os dados enviados")
    void atualizaDadosEnviados() {
        Empresa empresa = empresaCompleta();
        Empresa atualizacao = new Empresa();
        atualizacao.setNomeFantasia("Oficina Nova");

        atualizador.atualizar(empresa, atualizacao);

        assertEquals("Car Service Ltda", empresa.getRazaoSocial());
        assertEquals("Oficina Nova", empresa.getNomeFantasia());
    }

    @Test
    @DisplayName("atualiza so os campos enviados do endereco")
    void atualizaEndereco() {
        Empresa empresa = empresaCompleta();
        Empresa atualizacao = new Empresa();
        Endereco endereco = new Endereco();
        endereco.setCidade("Santos");
        atualizacao.setEndereco(endereco);

        atualizador.atualizar(empresa, atualizacao);

        assertEquals("Santos", empresa.getEndereco().getCidade());
        assertEquals("Avenida Sao Joao", empresa.getEndereco().getRua());
    }

    @Test
    @DisplayName("ignora o endereco enviado quando a empresa ainda nao tem endereco")
    void ignoraEnderecoDeEmpresaSemEndereco() {
        Empresa empresa = empresaCompleta();
        empresa.setEndereco(null);
        Empresa atualizacao = new Empresa();
        atualizacao.setEndereco(new Endereco());

        atualizador.atualizar(empresa, atualizacao);

        assertNull(empresa.getEndereco());
    }

    @Test
    @DisplayName("REGRESSAO P1: casa o telefone com id fora do cache de Long")
    void telefoneComIdGrande() {
        Empresa empresa = empresaCompleta();
        Empresa atualizacao = new Empresa();
        Telefone telefone = new Telefone();
        telefone.setId(200L);
        telefone.setDdd("13");
        atualizacao.getTelefones().add(telefone);

        atualizador.atualizar(empresa, atualizacao);

        assertEquals("13", empresa.getTelefones().get(0).getDdd());
        assertEquals("986454527", empresa.getTelefones().get(0).getNumero());
    }
}
