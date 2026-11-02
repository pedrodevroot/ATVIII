package com.autobots.automanager.modelo;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.List;

import org.springframework.stereotype.Component;

import com.autobots.automanager.controles.UsuarioControle;
import com.autobots.automanager.controles.VeiculoControle;
import com.autobots.automanager.controles.VendaControle;
import com.autobots.automanager.dtos.VeiculoDto;

@Component
public class AdicionadorLinkVeiculo implements AdicionadorLink<VeiculoDto> {

	@Override
	public void adicionarLink(List<VeiculoDto> lista) {
		for (VeiculoDto veiculo : lista) {
			long id = veiculo.getId();
			veiculo.add(linkTo(methodOn(VeiculoControle.class).obterVeiculo(id)).withSelfRel());
		}
	}

	@Override
	public void adicionarLink(VeiculoDto objeto) {
		long id = objeto.getId();
		objeto.add(linkTo(methodOn(VeiculoControle.class).obterVeiculo(id)).withSelfRel());
		objeto.add(linkTo(methodOn(UsuarioControle.class).obterUsuario(objeto.getProprietarioId())).withRel("proprietario"));
		objeto.add(linkTo(methodOn(VendaControle.class).obterVendasDoVeiculo(id)).withRel("vendas"));
	}
}
