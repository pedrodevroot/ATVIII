package com.autobots.automanager.modelo;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.List;

import org.springframework.stereotype.Component;

import com.autobots.automanager.controles.EmailControle;
import com.autobots.automanager.dtos.EmailDto;

@Component
public class AdicionadorLinkEmail implements AdicionadorLink<EmailDto> {

	@Override
	public void adicionarLink(List<EmailDto> lista) {
		for (EmailDto email : lista) {
			adicionarLink(email);
		}
	}

	@Override
	public void adicionarLink(EmailDto objeto) {
		long id = objeto.getId();
		objeto.add(linkTo(methodOn(EmailControle.class).obterEmail(id)).withSelfRel());
	}
}
