package com.autobots.automanager.modelo;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.List;

import org.springframework.stereotype.Component;

import com.autobots.automanager.controles.EnderecoControle;
import com.autobots.automanager.dtos.EnderecoDto;

@Component
public class AdicionadorLinkEndereco implements AdicionadorLink<EnderecoDto> {

	@Override
	public void adicionarLink(List<EnderecoDto> lista) {
		for (EnderecoDto endereco : lista) {
			adicionarLink(endereco);
		}
	}

	@Override
	public void adicionarLink(EnderecoDto objeto) {
		long id = objeto.getId();
		objeto.add(linkTo(methodOn(EnderecoControle.class).obterEndereco(id)).withSelfRel());
	}
}
