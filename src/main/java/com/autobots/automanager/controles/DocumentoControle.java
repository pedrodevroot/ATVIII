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

import com.autobots.automanager.conversores.DocumentoConversor;
import com.autobots.automanager.dtos.DocumentoAtualizacaoDto;
import com.autobots.automanager.dtos.DocumentoCadastroDto;
import com.autobots.automanager.dtos.DocumentoDto;
import com.autobots.automanager.entidades.Documento;
import com.autobots.automanager.modelo.AdicionadorLinkDocumento;
import com.autobots.automanager.servicos.DocumentoServico;

@RestController
public class DocumentoControle {

    @Autowired
    private DocumentoServico servico;
    @Autowired
    private DocumentoConversor conversor;
    @Autowired
    private AdicionadorLinkDocumento adicionadorLink;

    @GetMapping("/usuario/{usuarioId}/documentos")
    public ResponseEntity<List<DocumentoDto>> obterDocumentos(@PathVariable long usuarioId) {
        List<DocumentoDto> resposta = conversor.paraResposta(servico.listarPorUsuario(usuarioId));
        adicionadorLink.adicionarLink(resposta);
        return ResponseEntity.ok(resposta);
    }

    @PostMapping("/usuario/{usuarioId}/documentos")
    public ResponseEntity<DocumentoDto> cadastrarDocumento(@PathVariable long usuarioId,
            @Valid @RequestBody DocumentoCadastroDto dto) {
        Documento criado = servico.criar(usuarioId, conversor.paraEntidade(dto));
        DocumentoDto resposta = conversor.paraResposta(criado);
        adicionadorLink.adicionarLink(resposta);
        return ResponseEntity.created(URI.create("/documento/" + criado.getId()))
                .body(resposta);
    }

    @GetMapping("/documento/{id}")
    public ResponseEntity<DocumentoDto> obterDocumento(@PathVariable long id) {
        DocumentoDto resposta = conversor.paraResposta(servico.obterPorId(id));
        adicionadorLink.adicionarLink(resposta);
        return ResponseEntity.ok(resposta);
    }

    @PutMapping("/documento/{id}")
    public ResponseEntity<DocumentoDto> atualizarDocumento(@PathVariable long id,
            @Valid @RequestBody DocumentoAtualizacaoDto dto) {
        Documento atualizado = servico.atualizar(id, conversor.paraEntidade(dto));
        DocumentoDto resposta = conversor.paraResposta(atualizado);
        adicionadorLink.adicionarLink(resposta);
        return ResponseEntity.ok(resposta);
    }

    @DeleteMapping("/documento/{id}")
    public ResponseEntity<Void> excluirDocumento(@PathVariable long id) {
        servico.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
