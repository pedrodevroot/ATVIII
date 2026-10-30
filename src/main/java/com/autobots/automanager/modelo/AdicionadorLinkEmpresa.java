package com.autobots.automanager.modelo;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.List;

import org.springframework.stereotype.Component;

import com.autobots.automanager.controles.EmpresaControle;
import com.autobots.automanager.controles.EnderecoControle;
import com.autobots.automanager.controles.TelefoneControle;
import com.autobots.automanager.dtos.EmpresaRespostaDto;

@Component
public class AdicionadorLinkEmpresa implements AdicionadorLink<EmpresaRespostaDto> {

	@Override
	public void adicionarLink(List<EmpresaRespostaDto> lista) {
		for (EmpresaRespostaDto empresa : lista) {
			long id = empresa.getId();
			empresa.add(linkTo(methodOn(EmpresaControle.class).obterEmpresa(id)).withSelfRel());
		}
	}

	@Override
	public void adicionarLink(EmpresaRespostaDto objeto) {
		long id = objeto.getId();
		objeto.add(linkTo(methodOn(EmpresaControle.class).obterEmpresa(id)).withSelfRel());
		objeto.add(linkTo(methodOn(EmpresaControle.class).obterEmpresas()).withRel("empresas"));
		objeto.add(linkTo(methodOn(EmpresaControle.class).obterUsuarios(id)).withRel("usuarios"));
		objeto.add(linkTo(methodOn(TelefoneControle.class).obterTelefonesDaEmpresa(id)).withRel("telefones"));
		if (objeto.getEndereco() != null) {
			objeto.add(linkTo(methodOn(EnderecoControle.class).obterEnderecoDaEmpresa(id)).withRel("endereco"));
		}
	}
}
