package com.autobots.automanager.modelo;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.List;

import org.springframework.stereotype.Component;

import com.autobots.automanager.controles.DocumentoControle;
import com.autobots.automanager.dtos.DocumentoDto;

@Component
public class AdicionadorLinkDocumento implements AdicionadorLink<DocumentoDto> {

	@Override
	public void adicionarLink(List<DocumentoDto> lista) {
		for (DocumentoDto documento : lista) {
			adicionarLink(documento);
		}
	}

	@Override
	public void adicionarLink(DocumentoDto objeto) {
		long id = objeto.getId();
		objeto.add(linkTo(methodOn(DocumentoControle.class).obterDocumento(id)).withSelfRel());
	}
}
