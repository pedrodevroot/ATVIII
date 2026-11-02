package com.autobots.automanager.modelo;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.List;

import org.springframework.stereotype.Component;

import com.autobots.automanager.controles.MercadoriaControle;
import com.autobots.automanager.controles.ServicoControle;
import com.autobots.automanager.controles.UsuarioControle;
import com.autobots.automanager.controles.VeiculoControle;
import com.autobots.automanager.controles.VendaControle;
import com.autobots.automanager.dtos.ItemVendaDto;
import com.autobots.automanager.dtos.ServicoDto;
import com.autobots.automanager.dtos.VendaRespostaDto;

@Component
public class AdicionadorLinkVenda implements AdicionadorLink<VendaRespostaDto> {

	@Override
	public void adicionarLink(List<VendaRespostaDto> lista) {
		for (VendaRespostaDto venda : lista) {
			long id = venda.getId();
			venda.add(linkTo(methodOn(VendaControle.class).obterVenda(id)).withSelfRel());
		}
	}

	@Override
	public void adicionarLink(VendaRespostaDto objeto) {
		long id = objeto.getId();
		objeto.add(linkTo(methodOn(VendaControle.class).obterVenda(id)).withSelfRel());
		objeto.add(linkTo(methodOn(UsuarioControle.class).obterUsuario(objeto.getClienteId())).withRel("cliente"));
		objeto.add(linkTo(methodOn(UsuarioControle.class).obterUsuario(objeto.getFuncionarioId())).withRel("funcionario"));
		if (objeto.getVeiculoId() != null) {
			objeto.add(linkTo(methodOn(VeiculoControle.class).obterVeiculo(objeto.getVeiculoId())).withRel("veiculo"));
		}
		for (ItemVendaDto item : objeto.getItens()) {
			item.add(linkTo(methodOn(MercadoriaControle.class).obterMercadoria(item.getMercadoriaId())).withRel("mercadoria"));
		}
		for (ServicoDto servico : objeto.getServicos()) {
			servico.add(linkTo(methodOn(ServicoControle.class).obterServico(servico.getId())).withSelfRel());
		}
	}
}
