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

import com.autobots.automanager.conversores.MercadoriaConversor;
import com.autobots.automanager.dtos.MercadoriaAtualizacaoDto;
import com.autobots.automanager.dtos.MercadoriaCadastroDto;
import com.autobots.automanager.dtos.MercadoriaDto;
import com.autobots.automanager.entidades.Mercadoria;
import com.autobots.automanager.modelo.AdicionadorLinkMercadoria;
import com.autobots.automanager.servicos.MercadoriaServico;

@RestController
public class MercadoriaControle {

    @Autowired
    private MercadoriaServico servico;
    @Autowired
    private MercadoriaConversor conversor;
    @Autowired
    private AdicionadorLinkMercadoria adicionadorLink;

    @GetMapping("/empresa/{empresaId}/mercadorias")
    public ResponseEntity<List<MercadoriaDto>> obterMercadorias(@PathVariable long empresaId) {
        List<MercadoriaDto> resposta = conversor.paraResposta(servico.listarPorEmpresa(empresaId));
        adicionadorLink.adicionarLink(resposta);
        return ResponseEntity.ok(resposta);
    }

    @PostMapping("/empresa/{empresaId}/mercadorias")
    public ResponseEntity<MercadoriaDto> cadastrarMercadoria(@PathVariable long empresaId,
            @Valid @RequestBody MercadoriaCadastroDto dto) {
        Mercadoria criada = servico.criar(empresaId, conversor.paraEntidade(dto));
        MercadoriaDto resposta = conversor.paraResposta(criada);
        adicionadorLink.adicionarLink(resposta);
        return ResponseEntity.created(URI.create("/mercadoria/" + criada.getId()))
                .body(resposta);
    }

    @GetMapping("/mercadoria/{id}")
    public ResponseEntity<MercadoriaDto> obterMercadoria(@PathVariable long id) {
        MercadoriaDto resposta = conversor.paraResposta(servico.obterPorId(id));
        adicionadorLink.adicionarLink(resposta);
        return ResponseEntity.ok(resposta);
    }

    @PutMapping("/mercadoria/{id}")
    public ResponseEntity<MercadoriaDto> atualizarMercadoria(@PathVariable long id,
            @Valid @RequestBody MercadoriaAtualizacaoDto dto) {
        MercadoriaDto resposta = conversor.paraResposta(servico.atualizar(id, dto));
        adicionadorLink.adicionarLink(resposta);
        return ResponseEntity.ok(resposta);
    }

    @DeleteMapping("/mercadoria/{id}")
    public ResponseEntity<Void> excluirMercadoria(@PathVariable long id) {
        servico.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/usuario/{usuarioId}/mercadorias")
    public ResponseEntity<List<MercadoriaDto>> obterMercadoriasDoFornecedor(@PathVariable long usuarioId) {
        List<MercadoriaDto> resposta = conversor.paraResposta(servico.listarPorFornecedor(usuarioId));
        adicionadorLink.adicionarLink(resposta);
        return ResponseEntity.ok(resposta);
    }

    @PutMapping("/usuario/{usuarioId}/mercadorias/{mercadoriaId}")
    public ResponseEntity<Void> vincularFornecedor(@PathVariable long usuarioId,
            @PathVariable long mercadoriaId) {
        servico.vincularFornecedor(usuarioId, mercadoriaId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/usuario/{usuarioId}/mercadorias/{mercadoriaId}")
    public ResponseEntity<Void> desvincularFornecedor(@PathVariable long usuarioId,
            @PathVariable long mercadoriaId) {
        servico.desvincularFornecedor(usuarioId, mercadoriaId);
        return ResponseEntity.noContent().build();
    }
}
