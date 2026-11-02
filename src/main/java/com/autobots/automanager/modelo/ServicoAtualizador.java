package com.autobots.automanager.modelo;

import org.springframework.stereotype.Component;

import com.autobots.automanager.entidades.Servico;

@Component
public class ServicoAtualizador {
	private StringVerificadorNulo verificador = new StringVerificadorNulo();

	public void atualizar(Servico servico, Servico atualizacao) {
		if (!verificador.verificar(atualizacao.getNome())) {
			servico.setNome(atualizacao.getNome());
		}
		if (!verificador.verificar(atualizacao.getDescricao())) {
			servico.setDescricao(atualizacao.getDescricao());
		}
		if (!(atualizacao.getValor() == null)) {
			servico.setValor(atualizacao.getValor());
		}
	}
}
