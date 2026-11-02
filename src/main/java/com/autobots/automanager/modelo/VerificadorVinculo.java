package com.autobots.automanager.modelo;

import java.util.List;
import java.util.function.Function;

import com.autobots.automanager.excecoes.RecursoNaoEncontradoException;

public class VerificadorVinculo {

	public <T> void verificar(List<T> atuais, List<T> atualizacoes, Function<T, Long> id,
			String recurso, String dono) {
		for (T atualizacao : atualizacoes) {
			Long procurado = id.apply(atualizacao);
			boolean encontrado = false;
			for (T atual : atuais) {
				if (id.apply(atual).equals(procurado)) {
					encontrado = true;
				}
			}
			if (!encontrado) {
				throw new RecursoNaoEncontradoException(
						recurso + " de id " + procurado + " nao pertence " + dono);
			}
		}
	}
}
