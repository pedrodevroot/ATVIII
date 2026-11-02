package com.autobots.automanager.conversores;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.autobots.automanager.dtos.ServicoAtualizacaoDto;
import com.autobots.automanager.dtos.ServicoCadastroDto;
import com.autobots.automanager.dtos.ServicoDto;
import com.autobots.automanager.entidades.Servico;

class ServicoConversorTest {

    private final ServicoConversor conversor = new ServicoConversor();

    @Test
    @DisplayName("cadastro: copia os campos, e a entidade nasce sem id")
    void cadastro() {
        ServicoCadastroDto dto = new ServicoCadastroDto();
        dto.setNome("Alinhamento");
        dto.setDescricao("Alinhamento das rodas");
        dto.setValor(new BigDecimal("50.00"));

        Servico servico = conversor.paraEntidade(dto);

        assertNull(servico.getId());
        assertEquals("Alinhamento", servico.getNome());
        assertEquals("Alinhamento das rodas", servico.getDescricao());
        assertEquals(new BigDecimal("50.00"), servico.getValor());
    }

    @Test
    @DisplayName("atualizacao: copia somente o que foi enviado")
    void atualizacao() {
        ServicoAtualizacaoDto dto = new ServicoAtualizacaoDto();
        dto.setValor(new BigDecimal("75.50"));

        Servico servico = conversor.paraEntidade(dto);

        assertNull(servico.getId());
        assertNull(servico.getNome());
        assertNull(servico.getDescricao());
        assertEquals(new BigDecimal("75.50"), servico.getValor());
    }

    @Test
    @DisplayName("resposta: expoe todos os campos, tambem em lista")
    void resposta() {
        Servico servico = new Servico();
        servico.setId(9L);
        servico.setNome("Alinhamento");
        servico.setDescricao("Alinhamento das rodas");
        servico.setValor(new BigDecimal("50.00"));

        List<ServicoDto> dtos = conversor.paraResposta(List.of(servico));

        assertEquals(1, dtos.size());
        assertEquals(9L, dtos.get(0).getId());
        assertEquals("Alinhamento", dtos.get(0).getNome());
        assertEquals("Alinhamento das rodas", dtos.get(0).getDescricao());
        assertEquals(new BigDecimal("50.00"), dtos.get(0).getValor());
    }
}
