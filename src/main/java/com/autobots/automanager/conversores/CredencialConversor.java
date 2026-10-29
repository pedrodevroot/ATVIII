package com.autobots.automanager.conversores;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.autobots.automanager.dtos.CredencialCadastroDto;
import com.autobots.automanager.dtos.CredencialCodigoBarraCadastroDto;
import com.autobots.automanager.dtos.CredencialDto;
import com.autobots.automanager.dtos.CredencialUsuarioSenhaCadastroDto;
import com.autobots.automanager.entidades.Credencial;
import com.autobots.automanager.entidades.CredencialCodigoBarra;
import com.autobots.automanager.entidades.CredencialUsuarioSenha;

@Component
public class CredencialConversor {

    public Credencial paraEntidade(CredencialCadastroDto dto) {
        if (dto instanceof CredencialUsuarioSenhaCadastroDto usuarioSenha) {
            CredencialUsuarioSenha credencial = new CredencialUsuarioSenha();
            credencial.setNomeUsuario(usuarioSenha.getNomeUsuario());
            credencial.setSenha(usuarioSenha.getSenha());
            return credencial;
        }
        CredencialCodigoBarraCadastroDto codigoBarra = (CredencialCodigoBarraCadastroDto) dto;
        CredencialCodigoBarra credencial = new CredencialCodigoBarra();
        credencial.setCodigo(codigoBarra.getCodigo());
        return credencial;
    }

    public CredencialDto paraResposta(Credencial credencial) {
        CredencialDto dto = new CredencialDto();
        dto.setId(credencial.getId());
        dto.setCriacao(credencial.getCriacao());
        dto.setUltimoAcesso(credencial.getUltimoAcesso());
        dto.setInativo(credencial.isInativo());
        if (credencial instanceof CredencialUsuarioSenha usuarioSenha) {
            dto.setTipo(CredencialCadastroDto.USUARIO_SENHA);
            dto.setNomeUsuario(usuarioSenha.getNomeUsuario());
        } else if (credencial instanceof CredencialCodigoBarra codigoBarra) {
            dto.setTipo(CredencialCadastroDto.CODIGO_BARRA);
            dto.setCodigo(codigoBarra.getCodigo());
        }
        return dto;
    }

    public List<CredencialDto> paraResposta(List<Credencial> credenciais) {
        List<CredencialDto> dtos = new ArrayList<>();
        for (Credencial credencial : credenciais) {
            dtos.add(paraResposta(credencial));
        }
        return dtos;
    }
}
