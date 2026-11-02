package com.autobots.automanager.modelo;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.List;

import org.springframework.stereotype.Component;

import com.autobots.automanager.controles.EmpresaControle;
import com.autobots.automanager.controles.EnderecoControle;
import com.autobots.automanager.controles.MercadoriaControle;
import com.autobots.automanager.controles.ServicoControle;
import com.autobots.automanager.controles.TelefoneControle;
import com.autobots.automanager.controles.VendaControle;
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
		objeto.add(linkTo(methodOn(MercadoriaControle.class).obterMercadorias(id)).withRel("mercadorias"));
		objeto.add(linkTo(methodOn(ServicoControle.class).obterServicos(id)).withRel("servicos"));
		objeto.add(linkTo(methodOn(VendaControle.class).obterVendas(id)).withRel("vendas"));
		if (objeto.getEndereco() != null) {
			objeto.add(linkTo(methodOn(EnderecoControle.class).obterEnderecoDaEmpresa(id)).withRel("endereco"));
		}
	}
}
