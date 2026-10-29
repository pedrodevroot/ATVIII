package com.autobots.automanager.modelo;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.List;

import org.springframework.stereotype.Component;

import com.autobots.automanager.controles.CredencialControle;
import com.autobots.automanager.controles.DocumentoControle;
import com.autobots.automanager.controles.EmailControle;
import com.autobots.automanager.controles.EnderecoControle;
import com.autobots.automanager.controles.TelefoneControle;
import com.autobots.automanager.controles.UsuarioControle;
import com.autobots.automanager.dtos.UsuarioRespostaDto;

@Component
public class AdicionadorLinkUsuario implements AdicionadorLink<UsuarioRespostaDto> {

	@Override
	public void adicionarLink(List<UsuarioRespostaDto> lista) {
		for (UsuarioRespostaDto usuario : lista) {
			long id = usuario.getId();
			usuario.add(linkTo(methodOn(UsuarioControle.class).obterUsuario(id)).withSelfRel());
		}
	}

	@Override
	public void adicionarLink(UsuarioRespostaDto objeto) {
		long id = objeto.getId();
		objeto.add(linkTo(methodOn(UsuarioControle.class).obterUsuario(id)).withSelfRel());
		objeto.add(linkTo(methodOn(UsuarioControle.class).obterUsuarios()).withRel("usuarios"));
		objeto.add(linkTo(methodOn(TelefoneControle.class).obterTelefones(id)).withRel("telefones"));
		objeto.add(linkTo(methodOn(DocumentoControle.class).obterDocumentos(id)).withRel("documentos"));
		objeto.add(linkTo(methodOn(EmailControle.class).obterEmails(id)).withRel("emails"));
		objeto.add(linkTo(methodOn(CredencialControle.class).obterCredenciais(id)).withRel("credenciais"));
		if (objeto.getEndereco() != null) {
			objeto.add(linkTo(methodOn(EnderecoControle.class).obterEnderecoDoUsuario(id)).withRel("endereco"));
		}
	}
}
