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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.autobots.automanager.conversores.UsuarioConversor;
import com.autobots.automanager.dtos.UsuarioAtualizacaoDto;
import com.autobots.automanager.dtos.UsuarioCadastroDto;
import com.autobots.automanager.dtos.UsuarioRespostaDto;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.modelo.AdicionadorLinkUsuario;
import com.autobots.automanager.servicos.UsuarioServico;

@RestController
@RequestMapping("/usuario")
public class UsuarioControle {

    @Autowired
    private UsuarioServico servico;
    @Autowired
    private UsuarioConversor conversor;
    @Autowired
    private AdicionadorLinkUsuario adicionadorLink;

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioRespostaDto> obterUsuario(@PathVariable long id) {
        UsuarioRespostaDto resposta = conversor.paraResposta(servico.obterPorId(id));
        adicionadorLink.adicionarLink(resposta);
        return ResponseEntity.ok(resposta);
    }

    @GetMapping("/usuarios")
    public ResponseEntity<List<UsuarioRespostaDto>> obterUsuarios() {
        List<UsuarioRespostaDto> resposta = conversor.paraResposta(servico.listar());
        adicionadorLink.adicionarLink(resposta);
        return ResponseEntity.ok(resposta);
    }

    @PostMapping("/cadastro")
    public ResponseEntity<UsuarioRespostaDto> cadastrarUsuario(
            @Valid @RequestBody UsuarioCadastroDto dto) {
        Usuario cadastrado = servico.cadastrar(conversor.paraEntidade(dto));
        UsuarioRespostaDto resposta = conversor.paraResposta(cadastrado);
        adicionadorLink.adicionarLink(resposta);
        return ResponseEntity.created(URI.create("/usuario/" + cadastrado.getId()))
                .body(resposta);
    }

    @PutMapping("/atualizar")
    public ResponseEntity<UsuarioRespostaDto> atualizarUsuario(
            @Valid @RequestBody UsuarioAtualizacaoDto dto) {
        Usuario atualizado = servico.atualizar(conversor.paraEntidade(dto));
        UsuarioRespostaDto resposta = conversor.paraResposta(atualizado);
        adicionadorLink.adicionarLink(resposta);
        return ResponseEntity.ok(resposta);
    }

    @DeleteMapping("/excluir/{id}")
    public ResponseEntity<Void> excluirUsuario(@PathVariable long id) {
        servico.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
