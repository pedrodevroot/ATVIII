package com.autobots.automanager.modelo;

import org.springframework.stereotype.Component;

import com.autobots.automanager.dtos.CredencialAtualizacaoDto;
import com.autobots.automanager.entidades.Credencial;
import com.autobots.automanager.entidades.CredencialCodigoBarra;
import com.autobots.automanager.entidades.CredencialUsuarioSenha;

@Component
public class CredencialAtualizador {
	private StringVerificadorNulo verificador = new StringVerificadorNulo();

	public void atualizar(Credencial credencial, CredencialAtualizacaoDto atualizacao) {
		if (atualizacao.getInativo() != null) {
			credencial.setInativo(atualizacao.getInativo());
		}
		if (credencial instanceof CredencialUsuarioSenha usuarioSenha) {
			if (!verificador.verificar(atualizacao.getNomeUsuario())) {
				usuarioSenha.setNomeUsuario(atualizacao.getNomeUsuario());
			}
			if (!verificador.verificar(atualizacao.getSenha())) {
				usuarioSenha.setSenha(atualizacao.getSenha());
			}
		}
		if (credencial instanceof CredencialCodigoBarra codigoBarra && atualizacao.getCodigo() != null) {
			codigoBarra.setCodigo(atualizacao.getCodigo());
		}
	}
}
