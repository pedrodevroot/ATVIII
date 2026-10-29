package com.autobots.automanager.controles;

import java.net.URI;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.autobots.automanager.conversores.EnderecoConversor;
import com.autobots.automanager.dtos.EnderecoAtualizacaoDto;
import com.autobots.automanager.dtos.EnderecoCadastroDto;
import com.autobots.automanager.dtos.EnderecoDto;
import com.autobots.automanager.entidades.Endereco;
import com.autobots.automanager.modelo.AdicionadorLinkEndereco;
import com.autobots.automanager.servicos.EnderecoServico;

@RestController
public class EnderecoControle {

    @Autowired
    private EnderecoServico servico;
    @Autowired
    private EnderecoConversor conversor;
    @Autowired
    private AdicionadorLinkEndereco adicionadorLink;

    @GetMapping("/usuario/{usuarioId}/endereco")
    public ResponseEntity<EnderecoDto> obterEnderecoDoUsuario(@PathVariable long usuarioId) {
        EnderecoDto resposta = conversor.paraResposta(servico.obterPorUsuario(usuarioId));
        adicionadorLink.adicionarLink(resposta);
        return ResponseEntity.ok(resposta);
    }

    @PostMapping("/usuario/{usuarioId}/endereco")
    public ResponseEntity<EnderecoDto> cadastrarEndereco(@PathVariable long usuarioId,
            @Valid @RequestBody EnderecoCadastroDto dto) {
        Endereco criado = servico.criar(usuarioId, conversor.paraEntidade(dto));
        EnderecoDto resposta = conversor.paraResposta(criado);
        adicionadorLink.adicionarLink(resposta);
        return ResponseEntity.created(URI.create("/endereco/" + criado.getId()))
                .body(resposta);
    }

    @GetMapping("/endereco/{id}")
    public ResponseEntity<EnderecoDto> obterEndereco(@PathVariable long id) {
        EnderecoDto resposta = conversor.paraResposta(servico.obterPorId(id));
        adicionadorLink.adicionarLink(resposta);
        return ResponseEntity.ok(resposta);
    }

    @PutMapping("/endereco/{id}")
    public ResponseEntity<EnderecoDto> atualizarEndereco(@PathVariable long id,
            @Valid @RequestBody EnderecoAtualizacaoDto dto) {
        Endereco atualizado = servico.atualizar(id, conversor.paraEntidade(dto));
        EnderecoDto resposta = conversor.paraResposta(atualizado);
        adicionadorLink.adicionarLink(resposta);
        return ResponseEntity.ok(resposta);
    }

    @DeleteMapping("/endereco/{id}")
    public ResponseEntity<Void> excluirEndereco(@PathVariable long id) {
        servico.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
