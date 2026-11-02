package com.autobots.automanager.conversores;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.autobots.automanager.dtos.VeiculoAtualizacaoDto;
import com.autobots.automanager.dtos.VeiculoCadastroDto;
import com.autobots.automanager.dtos.VeiculoDto;
import com.autobots.automanager.entidades.Veiculo;

@Component
public class VeiculoConversor {

    public Veiculo paraEntidade(VeiculoCadastroDto dto) {
        Veiculo veiculo = new Veiculo();
        veiculo.setTipo(dto.getTipo());
        veiculo.setModelo(dto.getModelo());
        veiculo.setPlaca(dto.getPlaca());
        return veiculo;
    }

    public Veiculo paraEntidade(VeiculoAtualizacaoDto dto) {
        Veiculo veiculo = new Veiculo();
        veiculo.setTipo(dto.getTipo());
        veiculo.setModelo(dto.getModelo());
        veiculo.setPlaca(dto.getPlaca());
        return veiculo;
    }

    public VeiculoDto paraResposta(Veiculo veiculo) {
        VeiculoDto dto = new VeiculoDto();
        dto.setId(veiculo.getId());
        dto.setTipo(veiculo.getTipo());
        dto.setModelo(veiculo.getModelo());
        dto.setPlaca(veiculo.getPlaca());
        dto.setProprietarioId(veiculo.getProprietario().getId());
        return dto;
    }

    public List<VeiculoDto> paraResposta(List<Veiculo> veiculos) {
        List<VeiculoDto> dtos = new ArrayList<>();
        for (Veiculo veiculo : veiculos) {
            dtos.add(paraResposta(veiculo));
        }
        return dtos;
    }
}
