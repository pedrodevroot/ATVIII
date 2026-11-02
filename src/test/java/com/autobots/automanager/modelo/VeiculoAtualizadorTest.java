package com.autobots.automanager.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.autobots.automanager.entidades.Veiculo;
import com.autobots.automanager.enumeracoes.TipoVeiculo;

class VeiculoAtualizadorTest {

    private final VeiculoAtualizador atualizador = new VeiculoAtualizador();

    private Veiculo existente() {
        Veiculo veiculo = new Veiculo();
        veiculo.setTipo(TipoVeiculo.SUV);
        veiculo.setModelo("Corolla Cross");
        veiculo.setPlaca("ABC1D23");
        return veiculo;
    }

    @Test
    @DisplayName("altera todos os campos informados")
    void alteraCampos() {
        Veiculo veiculo = existente();
        Veiculo atualizacao = new Veiculo();
        atualizacao.setTipo(TipoVeiculo.HATCH);
        atualizacao.setModelo("Yaris");
        atualizacao.setPlaca("XYZ1234");

        atualizador.atualizar(veiculo, atualizacao);

        assertEquals(TipoVeiculo.HATCH, veiculo.getTipo());
        assertEquals("Yaris", veiculo.getModelo());
        assertEquals("XYZ1234", veiculo.getPlaca());
    }

    @Test
    @DisplayName("campos nulos ou em branco nao alteram o veiculo")
    void ignoraNuloEBranco() {
        Veiculo veiculo = existente();
        Veiculo atualizacao = new Veiculo();
        atualizacao.setModelo("   ");

        atualizador.atualizar(veiculo, atualizacao);

        assertEquals(TipoVeiculo.SUV, veiculo.getTipo());
        assertEquals("Corolla Cross", veiculo.getModelo());
        assertEquals("ABC1D23", veiculo.getPlaca());
    }
}
