package com.autobots.automanager.conversores;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.autobots.automanager.dtos.CredencialUsuarioSenhaCadastroDto;
import com.autobots.automanager.dtos.DocumentoCadastroDto;
import com.autobots.automanager.dtos.DocumentoDto;
import com.autobots.automanager.dtos.EmailCadastroDto;
import com.autobots.automanager.dtos.EmailDto;
import com.autobots.automanager.dtos.EnderecoAtualizacaoDto;
import com.autobots.automanager.dtos.EnderecoCadastroDto;
import com.autobots.automanager.dtos.TelefoneCadastroDto;
import com.autobots.automanager.dtos.TelefoneDto;
import com.autobots.automanager.dtos.UsuarioAtualizacaoDto;
import com.autobots.automanager.dtos.UsuarioCadastroDto;
import com.autobots.automanager.dtos.UsuarioRespostaDto;
import com.autobots.automanager.entidades.CredencialUsuarioSenha;
import com.autobots.automanager.entidades.Documento;
import com.autobots.automanager.entidades.Email;
import com.autobots.automanager.entidades.Empresa;
import com.autobots.automanager.entidades.Endereco;
import com.autobots.automanager.entidades.Telefone;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.enumeracoes.PerfilUsuario;
import com.autobots.automanager.enumeracoes.TipoDocumento;

class UsuarioConversorTest {

    private final UsuarioConversor conversor = new UsuarioConversor();

    private Date data(int ano, int mes, int dia) {
        Calendar calendario = Calendar.getInstance();
        calendario.set(ano, mes, dia, 0, 0, 0);
        calendario.set(Calendar.MILLISECOND, 0);
        return calendario.getTime();
    }

    private UsuarioCadastroDto cadastroCompleto() {
        UsuarioCadastroDto dto = new UsuarioCadastroDto();
        dto.setNome("Joana Ferreira");
        dto.setNomeSocial("Joana");
        dto.setDataNascimento(data(1995, Calendar.AUGUST, 20));
        dto.getPerfis().add(PerfilUsuario.CLIENTE);

        EnderecoCadastroDto endereco = new EnderecoCadastroDto();
        endereco.setCidade("Santos");
        endereco.setRua("Avenida Ana Costa");
        endereco.setNumero("250");
        dto.setEndereco(endereco);

        DocumentoCadastroDto documento = new DocumentoCadastroDto();
        documento.setTipo(TipoDocumento.RG);
        documento.setNumero("998877");
        documento.setDataEmissao(data(2010, Calendar.MARCH, 1));
        dto.getDocumentos().add(documento);

        TelefoneCadastroDto telefone = new TelefoneCadastroDto();
        telefone.setDdd("13");
        telefone.setNumero("981234567");
        dto.getTelefones().add(telefone);

        EmailCadastroDto email = new EmailCadastroDto();
        email.setEndereco("joana@email.com");
        dto.getEmails().add(email);

        CredencialUsuarioSenhaCadastroDto credencial = new CredencialUsuarioSenhaCadastroDto();
        credencial.setNomeUsuario("joana");
        credencial.setSenha("segredo1");
        dto.getCredenciais().add(credencial);

        return dto;
    }

    @Test
    @DisplayName("cadastro: copia os dados simples e os perfis")
    void cadastroCopiaDadosSimples() {
        Usuario usuario = conversor.paraEntidade(cadastroCompleto());

        assertEquals("Joana Ferreira", usuario.getNome());
        assertEquals("Joana", usuario.getNomeSocial());
        assertEquals(data(1995, Calendar.AUGUST, 20), usuario.getDataNascimento());
        assertEquals(Set.of(PerfilUsuario.CLIENTE), usuario.getPerfis());
    }

    @Test
    @DisplayName("cadastro: a entidade nasce sem id, pois o DTO nao possui esse campo")
    void cadastroNasceSemId() {
        Usuario usuario = conversor.paraEntidade(cadastroCompleto());

        assertNull(usuario.getId());
    }

    @Test
    @DisplayName("cadastro: nao define dataCadastro, que e responsabilidade do servico")
    void cadastroNaoDefineDataCadastro() {
        Usuario usuario = conversor.paraEntidade(cadastroCompleto());

        assertNull(usuario.getDataCadastro());
    }

    @Test
    @DisplayName("cadastro: os filhos tambem nascem sem id")
    void cadastroFilhosNascemSemId() {
        Usuario usuario = conversor.paraEntidade(cadastroCompleto());

        assertNull(usuario.getEndereco().getId());
        assertNull(usuario.getDocumentos().get(0).getId());
        assertNull(usuario.getTelefones().get(0).getId());
        assertNull(usuario.getEmails().get(0).getId());
        assertNull(usuario.getCredenciais().get(0).getId());
    }

    @Test
    @DisplayName("cadastro: converte endereco, documentos, telefones, e-mails e credenciais")
    void cadastroConverteFilhos() {
        Usuario usuario = conversor.paraEntidade(cadastroCompleto());

        assertEquals("Santos", usuario.getEndereco().getCidade());
        assertEquals(TipoDocumento.RG, usuario.getDocumentos().get(0).getTipo());
        assertEquals(data(2010, Calendar.MARCH, 1), usuario.getDocumentos().get(0).getDataEmissao());
        assertEquals("13", usuario.getTelefones().get(0).getDdd());
        assertEquals("joana@email.com", usuario.getEmails().get(0).getEndereco());
        CredencialUsuarioSenha credencial = assertInstanceOf(CredencialUsuarioSenha.class,
                usuario.getCredenciais().get(0));
        assertEquals("joana", credencial.getNomeUsuario());
    }

    @Test
    @DisplayName("cadastro: aceita usuario sem endereco e sem filhos sem estourar")
    void cadastroSemFilhos() {
        UsuarioCadastroDto dto = new UsuarioCadastroDto();
        dto.setNome("Sem Filhos");
        dto.getPerfis().add(PerfilUsuario.FORNECEDOR);

        Usuario usuario = conversor.paraEntidade(dto);

        assertNull(usuario.getEndereco());
        assertTrue(usuario.getDocumentos().isEmpty());
        assertTrue(usuario.getTelefones().isEmpty());
        assertTrue(usuario.getEmails().isEmpty());
        assertTrue(usuario.getCredenciais().isEmpty());
    }

    @Test
    @DisplayName("atualizacao: copia o id do usuario")
    void atualizacaoCopiaIdDoUsuario() {
        UsuarioAtualizacaoDto dto = new UsuarioAtualizacaoDto();
        dto.setId(42L);
        dto.setNome("Nome Novo");

        Usuario usuario = conversor.paraEntidade(dto);

        assertEquals(42L, usuario.getId());
        assertEquals("Nome Novo", usuario.getNome());
    }

    @Test
    @DisplayName("atualizacao: perfis ausentes viram conjunto vazio, que o atualizador ignora")
    void atualizacaoSemPerfis() {
        UsuarioAtualizacaoDto dto = new UsuarioAtualizacaoDto();
        dto.setId(1L);

        Usuario usuario = conversor.paraEntidade(dto);

        assertTrue(usuario.getPerfis().isEmpty());
    }

    @Test
    @DisplayName("atualizacao: PRESERVA o id dos filhos, pois os Atualizadores casam por id")
    void atualizacaoPreservaIdDosFilhos() {
        UsuarioAtualizacaoDto dto = new UsuarioAtualizacaoDto();
        dto.setId(1L);

        DocumentoDto documento = new DocumentoDto();
        documento.setId(200L);
        documento.setTipo(TipoDocumento.CNH);
        dto.getDocumentos().add(documento);

        TelefoneDto telefone = new TelefoneDto();
        telefone.setId(300L);
        telefone.setDdd("11");
        dto.getTelefones().add(telefone);

        EmailDto email = new EmailDto();
        email.setId(400L);
        email.setEndereco("novo@email.com");
        dto.getEmails().add(email);

        Usuario usuario = conversor.paraEntidade(dto);

        assertEquals(200L, usuario.getDocumentos().get(0).getId());
        assertEquals(300L, usuario.getTelefones().get(0).getId());
        assertEquals(400L, usuario.getEmails().get(0).getId());
    }

    @Test
    @DisplayName("atualizacao: nao define dataCadastro, pois o instante do cadastro e imutavel")
    void atualizacaoNaoDefineDataCadastro() {
        UsuarioAtualizacaoDto dto = new UsuarioAtualizacaoDto();
        dto.setId(1L);
        dto.setNome("Nome Novo");

        Usuario usuario = conversor.paraEntidade(dto);

        assertNull(usuario.getDataCadastro());
    }

    @Test
    @DisplayName("atualizacao: converte o endereco enviado")
    void atualizacaoConverteEndereco() {
        UsuarioAtualizacaoDto dto = new UsuarioAtualizacaoDto();
        dto.setId(1L);
        EnderecoAtualizacaoDto endereco = new EnderecoAtualizacaoDto();
        endereco.setCodigoPostal("11015200");
        dto.setEndereco(endereco);

        Usuario usuario = conversor.paraEntidade(dto);

        assertEquals("11015200", usuario.getEndereco().getCodigoPostal());
    }

    @Test
    @DisplayName("resposta: expoe id, perfis e dataCadastro")
    void respostaExpoeIdEDataCadastro() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("Pedro de Alcantara");
        usuario.setNomeSocial("Dom Pedro");
        usuario.setDataNascimento(data(2002, Calendar.MAY, 15));
        usuario.setDataCadastro(data(2020, Calendar.JANUARY, 1));
        usuario.getPerfis().add(PerfilUsuario.FUNCIONARIO);

        UsuarioRespostaDto dto = conversor.paraResposta(usuario);

        assertEquals(1L, dto.getId());
        assertEquals("Pedro de Alcantara", dto.getNome());
        assertEquals("Dom Pedro", dto.getNomeSocial());
        assertEquals(data(2002, Calendar.MAY, 15), dto.getDataNascimento());
        assertEquals(data(2020, Calendar.JANUARY, 1), dto.getDataCadastro());
        assertEquals(Set.of(PerfilUsuario.FUNCIONARIO), dto.getPerfis());
    }

    @Test
    @DisplayName("resposta: converte os filhos com seus ids")
    void respostaConverteFilhos() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);

        Endereco endereco = new Endereco();
        endereco.setId(3L);
        endereco.setCidade("Rio de Janeiro");
        usuario.setEndereco(endereco);

        Documento documento = new Documento();
        documento.setId(5L);
        documento.setTipo(TipoDocumento.RG);
        usuario.getDocumentos().add(documento);

        Telefone telefone = new Telefone();
        telefone.setId(7L);
        telefone.setDdd("21");
        usuario.getTelefones().add(telefone);

        Email email = new Email();
        email.setId(9L);
        email.setEndereco("pedro@email.com");
        usuario.getEmails().add(email);

        CredencialUsuarioSenha credencial = new CredencialUsuarioSenha();
        credencial.setId(11L);
        credencial.setNomeUsuario("pedro");
        usuario.getCredenciais().add(credencial);

        UsuarioRespostaDto dto = conversor.paraResposta(usuario);

        assertEquals(3L, dto.getEndereco().getId());
        assertEquals("Rio de Janeiro", dto.getEndereco().getCidade());
        assertEquals(5L, dto.getDocumentos().get(0).getId());
        assertEquals(7L, dto.getTelefones().get(0).getId());
        assertEquals(9L, dto.getEmails().get(0).getId());
        assertEquals(11L, dto.getCredenciais().get(0).getId());
        assertEquals("USUARIO_SENHA", dto.getCredenciais().get(0).getTipo());
    }

    @Test
    @DisplayName("resposta: aceita usuario sem endereco sem estourar")
    void respostaSemEndereco() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("Sem Endereco");

        UsuarioRespostaDto dto = conversor.paraResposta(usuario);

        assertNull(dto.getEndereco());
        assertTrue(dto.getDocumentos().isEmpty());
        assertTrue(dto.getTelefones().isEmpty());
        assertTrue(dto.getEmails().isEmpty());
        assertTrue(dto.getCredenciais().isEmpty());
    }

    @Test
    @DisplayName("resposta: expoe o id da empresa quando ha vinculo, e nada quando nao ha")
    void respostaComESemEmpresa() {
        Empresa empresa = new Empresa();
        empresa.setId(7L);
        Usuario comEmpresa = new Usuario();
        comEmpresa.setId(1L);
        comEmpresa.setEmpresa(empresa);
        Usuario semEmpresa = new Usuario();
        semEmpresa.setId(2L);

        assertEquals(7L, conversor.paraResposta(comEmpresa).getEmpresaId());
        assertNull(conversor.paraResposta(semEmpresa).getEmpresaId());
    }

    @Test
    @DisplayName("resposta em lista: converte todos os usuarios")
    void respostaEmLista() {
        Usuario primeiro = new Usuario();
        primeiro.setId(1L);
        primeiro.setNome("Primeiro");
        Usuario segundo = new Usuario();
        segundo.setId(2L);
        segundo.setNome("Segundo");

        List<UsuarioRespostaDto> dtos = conversor.paraResposta(List.of(primeiro, segundo));

        assertEquals(2, dtos.size());
        assertEquals("Primeiro", dtos.get(0).getNome());
        assertEquals("Segundo", dtos.get(1).getNome());
    }
}
