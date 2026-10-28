package com.autobots.automanager.modelo;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.List;

import org.springframework.stereotype.Component;

import com.autobots.automanager.controles.TelefoneControle;
import com.autobots.automanager.dtos.TelefoneDto;

@Component
public class AdicionadorLinkTelefone implements AdicionadorLink<TelefoneDto> {

	@Override
	public void adicionarLink(List<TelefoneDto> lista) {
		for (TelefoneDto telefone : lista) {
			adicionarLink(telefone);
		}
	}

	@Override
	public void adicionarLink(TelefoneDto objeto) {
		long id = objeto.getId();
		objeto.add(linkTo(methodOn(TelefoneControle.class).obterTelefone(id)).withSelfRel());
	}
}
