package com.autobots.automanager.conversores;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.autobots.automanager.dtos.CredencialCadastroDto;
import com.autobots.automanager.dtos.DocumentoCadastroDto;
import com.autobots.automanager.dtos.DocumentoDto;
import com.autobots.automanager.dtos.EmailCadastroDto;
import com.autobots.automanager.dtos.EmailDto;
import com.autobots.automanager.dtos.TelefoneCadastroDto;
import com.autobots.automanager.dtos.TelefoneDto;
import com.autobots.automanager.dtos.UsuarioAtualizacaoDto;
import com.autobots.automanager.dtos.UsuarioCadastroDto;
import com.autobots.automanager.dtos.UsuarioRespostaDto;
import com.autobots.automanager.entidades.Usuario;

@Component
public class UsuarioConversor {
    private EnderecoConversor enderecoConversor = new EnderecoConversor();
    private DocumentoConversor documentoConversor = new DocumentoConversor();
    private TelefoneConversor telefoneConversor = new TelefoneConversor();
    private EmailConversor emailConversor = new EmailConversor();
    private CredencialConversor credencialConversor = new CredencialConversor();

    public Usuario paraEntidade(UsuarioCadastroDto dto) {
        Usuario usuario = new Usuario();
        usuario.setNome(dto.getNome());
        usuario.setNomeSocial(dto.getNomeSocial());
        usuario.setDataNascimento(dto.getDataNascimento());
        usuario.getPerfis().addAll(dto.getPerfis());
        if (dto.getEndereco() != null) {
            usuario.setEndereco(enderecoConversor.paraEntidade(dto.getEndereco()));
        }
        for (DocumentoCadastroDto documento : dto.getDocumentos()) {
            usuario.getDocumentos().add(documentoConversor.paraEntidade(documento));
        }
        for (TelefoneCadastroDto telefone : dto.getTelefones()) {
            usuario.getTelefones().add(telefoneConversor.paraEntidade(telefone));
        }
        for (EmailCadastroDto email : dto.getEmails()) {
            usuario.getEmails().add(emailConversor.paraEntidade(email));
        }
        for (CredencialCadastroDto credencial : dto.getCredenciais()) {
            usuario.getCredenciais().add(credencialConversor.paraEntidade(credencial));
        }
        return usuario;
    }

    public Usuario paraEntidade(UsuarioAtualizacaoDto dto) {
        Usuario usuario = new Usuario();
        usuario.setId(dto.getId());
        usuario.setNome(dto.getNome());
        usuario.setNomeSocial(dto.getNomeSocial());
        usuario.setDataNascimento(dto.getDataNascimento());
        if (dto.getPerfis() != null) {
            usuario.getPerfis().addAll(dto.getPerfis());
        }
        if (dto.getEndereco() != null) {
            usuario.setEndereco(enderecoConversor.paraEntidade(dto.getEndereco()));
        }
        for (DocumentoDto documento : dto.getDocumentos()) {
            usuario.getDocumentos().add(documentoConversor.paraEntidade(documento));
        }
        for (TelefoneDto telefone : dto.getTelefones()) {
            usuario.getTelefones().add(telefoneConversor.paraEntidade(telefone));
        }
        for (EmailDto email : dto.getEmails()) {
            usuario.getEmails().add(emailConversor.paraEntidade(email));
        }
        return usuario;
    }

    public UsuarioRespostaDto paraResposta(Usuario usuario) {
        UsuarioRespostaDto dto = new UsuarioRespostaDto();
        dto.setId(usuario.getId());
        dto.setNome(usuario.getNome());
        dto.setNomeSocial(usuario.getNomeSocial());
        dto.setDataNascimento(usuario.getDataNascimento());
        dto.setDataCadastro(usuario.getDataCadastro());
        if (usuario.getEmpresa() != null) {
            dto.setEmpresaId(usuario.getEmpresa().getId());
        }
        dto.getPerfis().addAll(usuario.getPerfis());
        if (usuario.getEndereco() != null) {
            dto.setEndereco(enderecoConversor.paraResposta(usuario.getEndereco()));
        }
        dto.setDocumentos(documentoConversor.paraResposta(usuario.getDocumentos()));
        dto.setTelefones(telefoneConversor.paraResposta(usuario.getTelefones()));
        dto.setEmails(emailConversor.paraResposta(usuario.getEmails()));
        dto.setCredenciais(credencialConversor.paraResposta(usuario.getCredenciais()));
        return dto;
    }

    public List<UsuarioRespostaDto> paraResposta(List<Usuario> usuarios) {
        List<UsuarioRespostaDto> dtos = new ArrayList<>();
        for (Usuario usuario : usuarios) {
            dtos.add(paraResposta(usuario));
        }
        return dtos;
    }
}
