package com.autobots.automanager.modelo;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.List;

import org.springframework.stereotype.Component;

import com.autobots.automanager.controles.ClienteControle;
import com.autobots.automanager.controles.DocumentoControle;
import com.autobots.automanager.controles.EnderecoControle;
import com.autobots.automanager.controles.TelefoneControle;
import com.autobots.automanager.dtos.ClienteRespostaDto;

@Component
public class AdicionadorLinkCliente implements AdicionadorLink<ClienteRespostaDto> {

	@Override
	public void adicionarLink(List<ClienteRespostaDto> lista) {
		for (ClienteRespostaDto cliente : lista) {
			long id = cliente.getId();
			cliente.add(linkTo(methodOn(ClienteControle.class).obterCliente(id)).withSelfRel());
		}
	}

	@Override
	public void adicionarLink(ClienteRespostaDto objeto) {
		long id = objeto.getId();
		objeto.add(linkTo(methodOn(ClienteControle.class).obterCliente(id)).withSelfRel());
		objeto.add(linkTo(methodOn(ClienteControle.class).obterClientes()).withRel("clientes"));
		objeto.add(linkTo(methodOn(TelefoneControle.class).obterTelefones(id)).withRel("telefones"));
		objeto.add(linkTo(methodOn(DocumentoControle.class).obterDocumentos(id)).withRel("documentos"));
		objeto.add(linkTo(methodOn(EnderecoControle.class).obterEnderecos(id)).withRel("enderecos"));
	}
}
