package com.autobots.automanager.conversores;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.autobots.automanager.dtos.CredencialCodigoBarraCadastroDto;
import com.autobots.automanager.dtos.CredencialDto;
import com.autobots.automanager.dtos.CredencialUsuarioSenhaCadastroDto;
import com.autobots.automanager.entidades.CredencialCodigoBarra;
import com.autobots.automanager.entidades.CredencialUsuarioSenha;

class CredencialConversorTest {

    private final CredencialConversor conversor = new CredencialConversor();

    @Test
    @DisplayName("cadastro de usuario e senha: gera a subclasse certa, sem id")
    void cadastroUsuarioSenha() {
        CredencialUsuarioSenhaCadastroDto dto = new CredencialUsuarioSenhaCadastroDto();
        dto.setNomeUsuario("maria");
        dto.setSenha("segredo1");

        CredencialUsuarioSenha credencial = assertInstanceOf(CredencialUsuarioSenha.class,
                conversor.paraEntidade(dto));

        assertNull(credencial.getId());
        assertEquals("maria", credencial.getNomeUsuario());
        assertEquals("segredo1", credencial.getSenha());
    }

    @Test
    @DisplayName("cadastro de codigo de barras: gera a subclasse certa")
    void cadastroCodigoBarra() {
        CredencialCodigoBarraCadastroDto dto = new CredencialCodigoBarraCadastroDto();
        dto.setCodigo(789123456L);

        CredencialCodigoBarra credencial = assertInstanceOf(CredencialCodigoBarra.class,
                conversor.paraEntidade(dto));

        assertEquals(789123456L, credencial.getCodigo());
    }

    @Test
    @DisplayName("resposta de usuario e senha: informa o tipo e o nome de usuario, nunca a senha")
    void respostaUsuarioSenha() {
        Date criacao = new Date(0L);
        CredencialUsuarioSenha credencial = new CredencialUsuarioSenha();
        credencial.setId(3L);
        credencial.setNomeUsuario("maria");
        credencial.setSenha("segredo1");
        credencial.setCriacao(criacao);
        credencial.setInativo(true);

        CredencialDto dto = conversor.paraResposta(credencial);

        assertEquals(3L, dto.getId());
        assertEquals("USUARIO_SENHA", dto.getTipo());
        assertEquals("maria", dto.getNomeUsuario());
        assertEquals(criacao, dto.getCriacao());
        assertEquals(true, dto.getInativo());
        assertNull(dto.getCodigo());
    }

    @Test
    @DisplayName("resposta de codigo de barras: informa o tipo e o codigo, sem nome de usuario")
    void respostaCodigoBarra() {
        CredencialCodigoBarra credencial = new CredencialCodigoBarra();
        credencial.setId(4L);
        credencial.setCodigo(789123456L);

        List<CredencialDto> dtos = conversor.paraResposta(List.of(credencial));

        assertEquals("CODIGO_BARRA", dtos.get(0).getTipo());
        assertEquals(789123456L, dtos.get(0).getCodigo());
        assertNull(dtos.get(0).getNomeUsuario());
    }
}
