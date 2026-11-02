package com.autobots.automanager.modelo;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.List;

import org.springframework.stereotype.Component;

import com.autobots.automanager.controles.ServicoControle;
import com.autobots.automanager.dtos.ServicoDto;

@Component
public class AdicionadorLinkServico implements AdicionadorLink<ServicoDto> {

	@Override
	public void adicionarLink(List<ServicoDto> lista) {
		for (ServicoDto servico : lista) {
			adicionarLink(servico);
		}
	}

	@Override
	public void adicionarLink(ServicoDto objeto) {
		long id = objeto.getId();
		objeto.add(linkTo(methodOn(ServicoControle.class).obterServico(id)).withSelfRel());
	}
}
