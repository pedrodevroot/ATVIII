package com.autobots.automanager.conversores;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.autobots.automanager.dtos.EnderecoCadastroDto;
import com.autobots.automanager.dtos.EnderecoDto;
import com.autobots.automanager.entidades.Endereco;

class EnderecoConversorTest {

    private final EnderecoConversor conversor = new EnderecoConversor();

    @Test
    @DisplayName("cadastro: copia os sete campos e a entidade nasce sem id")
    void cadastroCopiaTodosOsCampos() {
        EnderecoCadastroDto dto = new EnderecoCadastroDto();
        dto.setEstado("Sao Paulo");
        dto.setCidade("Santos");
        dto.setBairro("Gonzaga");
        dto.setRua("Avenida Ana Costa");
        dto.setNumero("250");
        dto.setCodigoPostal("11060002");
        dto.setInformacoesAdicionais("Apartamento 32");

        Endereco endereco = conversor.paraEntidade(dto);

        assertNull(endereco.getId());
        assertEquals("Sao Paulo", endereco.getEstado());
        assertEquals("Santos", endereco.getCidade());
        assertEquals("Gonzaga", endereco.getBairro());
        assertEquals("Avenida Ana Costa", endereco.getRua());
        assertEquals("250", endereco.getNumero());
        assertEquals("11060002", endereco.getCodigoPostal());
        assertEquals("Apartamento 32", endereco.getInformacoesAdicionais());
    }

    @Test
    @DisplayName("resposta: expoe o id e os sete campos")
    void respostaExpoeTodosOsCampos() {
        Endereco endereco = new Endereco();
        endereco.setId(3L);
        endereco.setEstado("Rio de Janeiro");
        endereco.setCidade("Rio de Janeiro");
        endereco.setBairro("Copacabana");
        endereco.setRua("Avenida Atlantica");
        endereco.setNumero("1702");
        endereco.setCodigoPostal("22021001");
        endereco.setInformacoesAdicionais("Hotel");

        EnderecoDto dto = conversor.paraResposta(endereco);

        assertEquals(3L, dto.getId());
        assertEquals("Rio de Janeiro", dto.getEstado());
        assertEquals("Rio de Janeiro", dto.getCidade());
        assertEquals("Copacabana", dto.getBairro());
        assertEquals("Avenida Atlantica", dto.getRua());
        assertEquals("1702", dto.getNumero());
        assertEquals("22021001", dto.getCodigoPostal());
        assertEquals("Hotel", dto.getInformacoesAdicionais());
    }
}
