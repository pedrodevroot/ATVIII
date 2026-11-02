package com.autobots.automanager.conversores;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.autobots.automanager.dtos.VeiculoAtualizacaoDto;
import com.autobots.automanager.dtos.VeiculoCadastroDto;
import com.autobots.automanager.dtos.VeiculoDto;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.entidades.Veiculo;
import com.autobots.automanager.enumeracoes.TipoVeiculo;

class VeiculoConversorTest {

    private final VeiculoConversor conversor = new VeiculoConversor();

    @Test
    @DisplayName("cadastro: copia os campos, e a entidade nasce sem id nem proprietario")
    void cadastro() {
        VeiculoCadastroDto dto = new VeiculoCadastroDto();
        dto.setTipo(TipoVeiculo.SUV);
        dto.setModelo("Corolla Cross");
        dto.setPlaca("ABC1D23");

        Veiculo veiculo = conversor.paraEntidade(dto);

        assertNull(veiculo.getId());
        assertNull(veiculo.getProprietario());
        assertEquals(TipoVeiculo.SUV, veiculo.getTipo());
        assertEquals("Corolla Cross", veiculo.getModelo());
        assertEquals("ABC1D23", veiculo.getPlaca());
    }

    @Test
    @DisplayName("atualizacao: copia somente o que foi enviado")
    void atualizacao() {
        VeiculoAtualizacaoDto dto = new VeiculoAtualizacaoDto();
        dto.setModelo("Yaris");

        Veiculo veiculo = conversor.paraEntidade(dto);

        assertNull(veiculo.getId());
        assertNull(veiculo.getTipo());
        assertNull(veiculo.getPlaca());
        assertEquals("Yaris", veiculo.getModelo());
    }

    @Test
    @DisplayName("resposta: expoe os campos e o id do proprietario, tambem em lista")
    void resposta() {
        Usuario dono = new Usuario();
        dono.setId(3L);
        Veiculo veiculo = new Veiculo();
        veiculo.setId(9L);
        veiculo.setTipo(TipoVeiculo.PICKUP);
        veiculo.setModelo("Hilux");
        veiculo.setPlaca("XYZ1234");
        veiculo.setProprietario(dono);

        List<VeiculoDto> dtos = conversor.paraResposta(List.of(veiculo));

        assertEquals(1, dtos.size());
        assertEquals(9L, dtos.get(0).getId());
        assertEquals(TipoVeiculo.PICKUP, dtos.get(0).getTipo());
        assertEquals("Hilux", dtos.get(0).getModelo());
        assertEquals("XYZ1234", dtos.get(0).getPlaca());
        assertEquals(3L, dtos.get(0).getProprietarioId());
    }
}
