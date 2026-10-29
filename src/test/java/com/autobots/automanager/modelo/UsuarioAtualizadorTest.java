package com.autobots.automanager.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Calendar;
import java.util.Date;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.autobots.automanager.entidades.Documento;
import com.autobots.automanager.entidades.Email;
import com.autobots.automanager.entidades.Endereco;
import com.autobots.automanager.entidades.Telefone;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.enumeracoes.PerfilUsuario;
import com.autobots.automanager.enumeracoes.TipoDocumento;

class UsuarioAtualizadorTest {

    private final UsuarioAtualizador atualizador = new UsuarioAtualizador();

    private Date data(int ano, int mes, int dia) {
        Calendar calendario = Calendar.getInstance();
        calendario.set(ano, mes, dia, 0, 0, 0);
        calendario.set(Calendar.MILLISECOND, 0);
        return calendario.getTime();
    }

    private Usuario usuarioCompleto() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("Pedro de Alcantara");
        usuario.setNomeSocial("Dom Pedro");
        usuario.setDataNascimento(data(2002, Calendar.MAY, 15));
        usuario.setDataCadastro(data(2020, Calendar.JANUARY, 1));
        usuario.getPerfis().add(PerfilUsuario.CLIENTE);

        Endereco endereco = new Endereco();
        endereco.setId(300L);
        endereco.setCidade("Rio de Janeiro");
        endereco.setRua("Avenida Atlantica");
        endereco.setNumero("1702");
        usuario.setEndereco(endereco);

        Telefone telefone = new Telefone();
        telefone.setId(200L);
        telefone.setDdd("21");
        telefone.setNumero("981234576");
        usuario.getTelefones().add(telefone);

        Documento documento = new Documento();
        documento.setId(200L);
        documento.setTipo(TipoDocumento.RG);
        documento.setNumero("1500");
        usuario.getDocumentos().add(documento);

        Email email = new Email();
        email.setId(200L);
        email.setEndereco("pedro@email.com");
        usuario.getEmails().add(email);

        return usuario;
    }

    @Test
    @DisplayName("atualiza os dados simples enviados")
    void atualizaDadosSimples() {
        Usuario usuario = usuarioCompleto();
        Usuario atualizacao = new Usuario();
        atualizacao.setNome("Pedro de Alcantara e Bourbon");
        atualizacao.setNomeSocial("Dom Pedro I");

        atualizador.atualizar(usuario, atualizacao);

        assertEquals("Pedro de Alcantara e Bourbon", usuario.getNome());
        assertEquals("Dom Pedro I", usuario.getNomeSocial());
    }

    @Test
    @DisplayName("nao sobrescreve dados ausentes na atualizacao")
    void naoSobrescreveDadosAusentes() {
        Usuario usuario = usuarioCompleto();
        Usuario atualizacao = new Usuario();
        atualizacao.setNomeSocial("Dom Pedro I");

        atualizador.atualizar(usuario, atualizacao);

        assertEquals("Pedro de Alcantara", usuario.getNome());
        assertEquals("Dom Pedro I", usuario.getNomeSocial());
        assertEquals(data(2020, Calendar.JANUARY, 1), usuario.getDataCadastro());
    }

    @Test
    @DisplayName("substitui os perfis quando eles sao enviados")
    void substituiPerfis() {
        Usuario usuario = usuarioCompleto();
        Usuario atualizacao = new Usuario();
        atualizacao.getPerfis().add(PerfilUsuario.FUNCIONARIO);
        atualizacao.getPerfis().add(PerfilUsuario.FORNECEDOR);

        atualizador.atualizar(usuario, atualizacao);

        assertEquals(Set.of(PerfilUsuario.FUNCIONARIO, PerfilUsuario.FORNECEDOR), usuario.getPerfis());
    }

    @Test
    @DisplayName("mantem os perfis quando eles nao sao enviados")
    void mantemPerfis() {
        Usuario usuario = usuarioCompleto();

        atualizador.atualizar(usuario, new Usuario());

        assertEquals(Set.of(PerfilUsuario.CLIENTE), usuario.getPerfis());
    }

    @Test
    @DisplayName("atualiza so os campos enviados do endereco")
    void atualizaEndereco() {
        Usuario usuario = usuarioCompleto();
        Usuario atualizacao = new Usuario();
        Endereco endereco = new Endereco();
        endereco.setCidade("Santos");
        atualizacao.setEndereco(endereco);

        atualizador.atualizar(usuario, atualizacao);

        assertEquals("Santos", usuario.getEndereco().getCidade());
        assertEquals("Avenida Atlantica", usuario.getEndereco().getRua());
    }

    @Test
    @DisplayName("ignora o endereco enviado quando o usuario ainda nao tem endereco")
    void ignoraEnderecoDeUsuarioSemEndereco() {
        Usuario usuario = usuarioCompleto();
        usuario.setEndereco(null);
        Usuario atualizacao = new Usuario();
        Endereco endereco = new Endereco();
        endereco.setCidade("Santos");
        atualizacao.setEndereco(endereco);

        atualizador.atualizar(usuario, atualizacao);

        assertNull(usuario.getEndereco());
    }

    @Test
    @DisplayName("REGRESSAO P1: delega telefone, documento e e-mail casando ids fora do cache de Long")
    void delegaFilhosComIdGrande() {
        Usuario usuario = usuarioCompleto();
        Usuario atualizacao = new Usuario();

        Telefone telefone = new Telefone();
        telefone.setId(200L);
        telefone.setDdd("11");
        atualizacao.getTelefones().add(telefone);

        Documento documento = new Documento();
        documento.setId(200L);
        documento.setTipo(TipoDocumento.CNH);
        atualizacao.getDocumentos().add(documento);

        Email email = new Email();
        email.setId(200L);
        email.setEndereco("dompedro@email.com");
        atualizacao.getEmails().add(email);

        atualizador.atualizar(usuario, atualizacao);

        assertEquals("11", usuario.getTelefones().get(0).getDdd());
        assertEquals("981234576", usuario.getTelefones().get(0).getNumero());
        assertEquals(TipoDocumento.CNH, usuario.getDocumentos().get(0).getTipo());
        assertEquals("1500", usuario.getDocumentos().get(0).getNumero());
        assertEquals("dompedro@email.com", usuario.getEmails().get(0).getEndereco());
    }
}
