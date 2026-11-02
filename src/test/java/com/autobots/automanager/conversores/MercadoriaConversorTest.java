package com.autobots.automanager.conversores;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.autobots.automanager.dtos.MercadoriaCadastroDto;
import com.autobots.automanager.dtos.MercadoriaDto;
import com.autobots.automanager.entidades.Mercadoria;

class MercadoriaConversorTest {

    private final MercadoriaConversor conversor = new MercadoriaConversor();

    @Test
    @DisplayName("cadastro: copia os campos, e a entidade nasce sem id nem data de cadastro")
    void cadastro() {
        MercadoriaCadastroDto dto = new MercadoriaCadastroDto();
        dto.setNome("Pneu");
        dto.setDescricao("Aro 15");
        dto.setFabricacao(new Date(1000L));
        dto.setValidade(new Date(2000L));
        dto.setQuantidade(10L);
        dto.setValor(new BigDecimal("300.00"));

        Mercadoria mercadoria = conversor.paraEntidade(dto);

        assertNull(mercadoria.getId());
        assertNull(mercadoria.getCadastro());
        assertEquals("Pneu", mercadoria.getNome());
        assertEquals("Aro 15", mercadoria.getDescricao());
        assertEquals(new Date(1000L), mercadoria.getFabricacao());
        assertEquals(new Date(2000L), mercadoria.getValidade());
        assertEquals(10L, mercadoria.getQuantidade());
        assertEquals(new BigDecimal("300.00"), mercadoria.getValor());
    }

    @Test
    @DisplayName("resposta: expoe todos os campos, tambem em lista")
    void resposta() {
        Mercadoria mercadoria = new Mercadoria();
        mercadoria.setId(9L);
        mercadoria.setNome("Pneu");
        mercadoria.setDescricao("Aro 15");
        mercadoria.setFabricacao(new Date(1000L));
        mercadoria.setValidade(new Date(2000L));
        mercadoria.setCadastro(new Date(3000L));
        mercadoria.setQuantidade(10);
        mercadoria.setValor(new BigDecimal("300.00"));

        List<MercadoriaDto> dtos = conversor.paraResposta(List.of(mercadoria));

        assertEquals(1, dtos.size());
        MercadoriaDto dto = dtos.get(0);
        assertEquals(9L, dto.getId());
        assertEquals("Pneu", dto.getNome());
        assertEquals("Aro 15", dto.getDescricao());
        assertEquals(new Date(1000L), dto.getFabricacao());
        assertEquals(new Date(2000L), dto.getValidade());
        assertEquals(new Date(3000L), dto.getCadastro());
        assertEquals(10L, dto.getQuantidade());
        assertEquals(new BigDecimal("300.00"), dto.getValor());
    }
}
