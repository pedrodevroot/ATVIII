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

import com.autobots.automanager.conversores.VendaConversor;
import com.autobots.automanager.dtos.VendaAtualizacaoDto;
import com.autobots.automanager.dtos.VendaCadastroDto;
import com.autobots.automanager.dtos.VendaRespostaDto;
import com.autobots.automanager.entidades.Venda;
import com.autobots.automanager.modelo.AdicionadorLinkVenda;
import com.autobots.automanager.servicos.VendaServico;

@RestController
public class VendaControle {

    @Autowired
    private VendaServico servico;
    @Autowired
    private VendaConversor conversor;
    @Autowired
    private AdicionadorLinkVenda adicionadorLink;

    @GetMapping("/empresa/{empresaId}/vendas")
    public ResponseEntity<List<VendaRespostaDto>> obterVendas(@PathVariable long empresaId) {
        List<VendaRespostaDto> resposta = conversor.paraResposta(servico.listarPorEmpresa(empresaId));
        adicionadorLink.adicionarLink(resposta);
        return ResponseEntity.ok(resposta);
    }

    @GetMapping("/usuario/{usuarioId}/vendas")
    public ResponseEntity<List<VendaRespostaDto>> obterVendasDoUsuario(@PathVariable long usuarioId) {
        List<VendaRespostaDto> resposta = conversor.paraResposta(servico.listarPorUsuario(usuarioId));
        adicionadorLink.adicionarLink(resposta);
        return ResponseEntity.ok(resposta);
    }

    @GetMapping("/veiculo/{veiculoId}/vendas")
    public ResponseEntity<List<VendaRespostaDto>> obterVendasDoVeiculo(@PathVariable long veiculoId) {
        List<VendaRespostaDto> resposta = conversor.paraResposta(servico.listarPorVeiculo(veiculoId));
        adicionadorLink.adicionarLink(resposta);
        return ResponseEntity.ok(resposta);
    }

    @PostMapping("/empresa/{empresaId}/vendas")
    public ResponseEntity<VendaRespostaDto> cadastrarVenda(@PathVariable long empresaId,
            @Valid @RequestBody VendaCadastroDto dto) {
        Venda criada = servico.criar(empresaId, dto);
        VendaRespostaDto resposta = conversor.paraResposta(criada);
        adicionadorLink.adicionarLink(resposta);
        return ResponseEntity.created(URI.create("/venda/" + criada.getId()))
                .body(resposta);
    }

    @GetMapping("/venda/{id}")
    public ResponseEntity<VendaRespostaDto> obterVenda(@PathVariable long id) {
        VendaRespostaDto resposta = conversor.paraResposta(servico.obterPorId(id));
        adicionadorLink.adicionarLink(resposta);
        return ResponseEntity.ok(resposta);
    }

    @PutMapping("/venda/{id}")
    public ResponseEntity<VendaRespostaDto> atualizarVenda(@PathVariable long id,
            @Valid @RequestBody VendaAtualizacaoDto dto) {
        VendaRespostaDto resposta = conversor.paraResposta(servico.atualizar(id, dto));
        adicionadorLink.adicionarLink(resposta);
        return ResponseEntity.ok(resposta);
    }

    @DeleteMapping("/venda/{id}")
    public ResponseEntity<Void> excluirVenda(@PathVariable long id) {
        servico.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
