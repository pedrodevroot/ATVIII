package com.autobots.automanager.conversores;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.autobots.automanager.dtos.EmpresaAtualizacaoDto;
import com.autobots.automanager.dtos.EmpresaCadastroDto;
import com.autobots.automanager.dtos.EmpresaRespostaDto;
import com.autobots.automanager.dtos.TelefoneCadastroDto;
import com.autobots.automanager.dtos.TelefoneDto;
import com.autobots.automanager.entidades.Empresa;

@Component
public class EmpresaConversor {
    private EnderecoConversor enderecoConversor = new EnderecoConversor();
    private TelefoneConversor telefoneConversor = new TelefoneConversor();

    public Empresa paraEntidade(EmpresaCadastroDto dto) {
        Empresa empresa = new Empresa();
        empresa.setRazaoSocial(dto.getRazaoSocial());
        empresa.setNomeFantasia(dto.getNomeFantasia());
        if (dto.getEndereco() != null) {
            empresa.setEndereco(enderecoConversor.paraEntidade(dto.getEndereco()));
        }
        for (TelefoneCadastroDto telefone : dto.getTelefones()) {
            empresa.getTelefones().add(telefoneConversor.paraEntidade(telefone));
        }
        return empresa;
    }

    public Empresa paraEntidade(EmpresaAtualizacaoDto dto) {
        Empresa empresa = new Empresa();
        empresa.setId(dto.getId());
        empresa.setRazaoSocial(dto.getRazaoSocial());
        empresa.setNomeFantasia(dto.getNomeFantasia());
        if (dto.getEndereco() != null) {
            empresa.setEndereco(enderecoConversor.paraEntidade(dto.getEndereco()));
        }
        for (TelefoneDto telefone : dto.getTelefones()) {
            empresa.getTelefones().add(telefoneConversor.paraEntidade(telefone));
        }
        return empresa;
    }

    public EmpresaRespostaDto paraResposta(Empresa empresa) {
        EmpresaRespostaDto dto = new EmpresaRespostaDto();
        dto.setId(empresa.getId());
        dto.setRazaoSocial(empresa.getRazaoSocial());
        dto.setNomeFantasia(empresa.getNomeFantasia());
        dto.setCadastro(empresa.getCadastro());
        if (empresa.getEndereco() != null) {
            dto.setEndereco(enderecoConversor.paraResposta(empresa.getEndereco()));
        }
        dto.setTelefones(telefoneConversor.paraResposta(empresa.getTelefones()));
        return dto;
    }

    public List<EmpresaRespostaDto> paraResposta(List<Empresa> empresas) {
        List<EmpresaRespostaDto> dtos = new ArrayList<>();
        for (Empresa empresa : empresas) {
            dtos.add(paraResposta(empresa));
        }
        return dtos;
    }
}
