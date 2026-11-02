package com.autobots.automanager.modelo;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.List;

import org.springframework.stereotype.Component;

import com.autobots.automanager.controles.MercadoriaControle;
import com.autobots.automanager.dtos.MercadoriaDto;

@Component
public class AdicionadorLinkMercadoria implements AdicionadorLink<MercadoriaDto> {

	@Override
	public void adicionarLink(List<MercadoriaDto> lista) {
		for (MercadoriaDto mercadoria : lista) {
			adicionarLink(mercadoria);
		}
	}

	@Override
	public void adicionarLink(MercadoriaDto objeto) {
		long id = objeto.getId();
		objeto.add(linkTo(methodOn(MercadoriaControle.class).obterMercadoria(id)).withSelfRel());
	}
}
