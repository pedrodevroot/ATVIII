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

import com.autobots.automanager.conversores.ServicoConversor;
import com.autobots.automanager.dtos.ServicoAtualizacaoDto;
import com.autobots.automanager.dtos.ServicoCadastroDto;
import com.autobots.automanager.dtos.ServicoDto;
import com.autobots.automanager.entidades.Servico;
import com.autobots.automanager.modelo.AdicionadorLinkServico;
import com.autobots.automanager.servicos.ServicoServico;

@RestController
public class ServicoControle {

    @Autowired
    private ServicoServico servico;
    @Autowired
    private ServicoConversor conversor;
    @Autowired
    private AdicionadorLinkServico adicionadorLink;

    @GetMapping("/empresa/{empresaId}/servicos")
    public ResponseEntity<List<ServicoDto>> obterServicos(@PathVariable long empresaId) {
        List<ServicoDto> resposta = conversor.paraResposta(servico.listarPorEmpresa(empresaId));
        adicionadorLink.adicionarLink(resposta);
        return ResponseEntity.ok(resposta);
    }

    @PostMapping("/empresa/{empresaId}/servicos")
    public ResponseEntity<ServicoDto> cadastrarServico(@PathVariable long empresaId,
            @Valid @RequestBody ServicoCadastroDto dto) {
        Servico criado = servico.criar(empresaId, conversor.paraEntidade(dto));
        ServicoDto resposta = conversor.paraResposta(criado);
        adicionadorLink.adicionarLink(resposta);
        return ResponseEntity.created(URI.create("/servico/" + criado.getId()))
                .body(resposta);
    }

    @GetMapping("/servico/{id}")
    public ResponseEntity<ServicoDto> obterServico(@PathVariable long id) {
        ServicoDto resposta = conversor.paraResposta(servico.obterPorId(id));
        adicionadorLink.adicionarLink(resposta);
        return ResponseEntity.ok(resposta);
    }

    @PutMapping("/servico/{id}")
    public ResponseEntity<ServicoDto> atualizarServico(@PathVariable long id,
            @Valid @RequestBody ServicoAtualizacaoDto dto) {
        Servico atualizado = servico.atualizar(id, conversor.paraEntidade(dto));
        ServicoDto resposta = conversor.paraResposta(atualizado);
        adicionadorLink.adicionarLink(resposta);
        return ResponseEntity.ok(resposta);
    }

    @DeleteMapping("/servico/{id}")
    public ResponseEntity<Void> excluirServico(@PathVariable long id) {
        servico.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
