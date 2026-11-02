package com.autobots.automanager.modelo;

import org.springframework.stereotype.Component;

import com.autobots.automanager.dtos.MercadoriaAtualizacaoDto;
import com.autobots.automanager.entidades.Mercadoria;

@Component
public class MercadoriaAtualizador {
	private StringVerificadorNulo verificador = new StringVerificadorNulo();

	public void atualizar(Mercadoria mercadoria, MercadoriaAtualizacaoDto atualizacao) {
		if (!verificador.verificar(atualizacao.getNome())) {
			mercadoria.setNome(atualizacao.getNome());
		}
		if (!verificador.verificar(atualizacao.getDescricao())) {
			mercadoria.setDescricao(atualizacao.getDescricao());
		}
		if (!(atualizacao.getFabricacao() == null)) {
			mercadoria.setFabricacao(atualizacao.getFabricacao());
		}
		if (!(atualizacao.getValidade() == null)) {
			mercadoria.setValidade(atualizacao.getValidade());
		}
		if (!(atualizacao.getQuantidade() == null)) {
			mercadoria.setQuantidade(atualizacao.getQuantidade());
		}
		if (!(atualizacao.getValor() == null)) {
			mercadoria.setValor(atualizacao.getValor());
		}
	}
}
