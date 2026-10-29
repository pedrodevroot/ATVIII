package com.autobots.automanager.modelo;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.List;

import org.springframework.stereotype.Component;

import com.autobots.automanager.controles.CredencialControle;
import com.autobots.automanager.dtos.CredencialDto;

@Component
public class AdicionadorLinkCredencial implements AdicionadorLink<CredencialDto> {

	@Override
	public void adicionarLink(List<CredencialDto> lista) {
		for (CredencialDto credencial : lista) {
			adicionarLink(credencial);
		}
	}

	@Override
	public void adicionarLink(CredencialDto objeto) {
		long id = objeto.getId();
		objeto.add(linkTo(methodOn(CredencialControle.class).obterCredencial(id)).withSelfRel());
	}
}
