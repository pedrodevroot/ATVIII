package com.autobots.automanager.controles;

import java.net.URI;
import java.util.List;

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

import com.autobots.automanager.conversores.CredencialConversor;
import com.autobots.automanager.dtos.CredencialAtualizacaoDto;
import com.autobots.automanager.dtos.CredencialCadastroDto;
import com.autobots.automanager.dtos.CredencialDto;
import com.autobots.automanager.entidades.Credencial;
import com.autobots.automanager.modelo.AdicionadorLinkCredencial;
import com.autobots.automanager.servicos.CredencialServico;

@RestController
public class CredencialControle {

    @Autowired
    private CredencialServico servico;
    @Autowired
    private CredencialConversor conversor;
    @Autowired
    private AdicionadorLinkCredencial adicionadorLink;

    @GetMapping("/usuario/{usuarioId}/credenciais")
    public ResponseEntity<List<CredencialDto>> obterCredenciais(@PathVariable long usuarioId) {
        List<CredencialDto> resposta = conversor.paraResposta(servico.listarPorUsuario(usuarioId));
        adicionadorLink.adicionarLink(resposta);
        return ResponseEntity.ok(resposta);
    }

    @PostMapping("/usuario/{usuarioId}/credenciais")
    public ResponseEntity<CredencialDto> cadastrarCredencial(@PathVariable long usuarioId,
            @Valid @RequestBody CredencialCadastroDto dto) {
        Credencial criada = servico.criar(usuarioId, conversor.paraEntidade(dto));
        CredencialDto resposta = conversor.paraResposta(criada);
        adicionadorLink.adicionarLink(resposta);
        return ResponseEntity.created(URI.create("/credencial/" + criada.getId()))
                .body(resposta);
    }

    @GetMapping("/credencial/{id}")
    public ResponseEntity<CredencialDto> obterCredencial(@PathVariable long id) {
        CredencialDto resposta = conversor.paraResposta(servico.obterPorId(id));
        adicionadorLink.adicionarLink(resposta);
        return ResponseEntity.ok(resposta);
    }

    @PutMapping("/credencial/{id}")
    public ResponseEntity<CredencialDto> atualizarCredencial(@PathVariable long id,
            @Valid @RequestBody CredencialAtualizacaoDto dto) {
        CredencialDto resposta = conversor.paraResposta(servico.atualizar(id, dto));
        adicionadorLink.adicionarLink(resposta);
        return ResponseEntity.ok(resposta);
    }

    @DeleteMapping("/credencial/{id}")
    public ResponseEntity<Void> excluirCredencial(@PathVariable long id) {
        servico.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
