package com.autobots.automanager.conversores;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.autobots.automanager.dtos.ClienteAtualizacaoDto;
import com.autobots.automanager.dtos.ClienteCadastroDto;
import com.autobots.automanager.dtos.ClienteRespostaDto;
import com.autobots.automanager.dtos.DocumentoCadastroDto;
import com.autobots.automanager.dtos.DocumentoDto;
import com.autobots.automanager.dtos.EnderecoCadastroDto;
import com.autobots.automanager.dtos.TelefoneCadastroDto;
import com.autobots.automanager.dtos.EnderecoDto;
import com.autobots.automanager.dtos.TelefoneDto;
import com.autobots.automanager.entidades.Cliente;
import com.autobots.automanager.entidades.Documento;
import com.autobots.automanager.entidades.Endereco;
import com.autobots.automanager.entidades.Telefone;

@Component
public class ClienteConversor {

    public Cliente paraEntidade(ClienteCadastroDto dto) {
        Cliente cliente = new Cliente();
        cliente.setNome(dto.getNome());
        cliente.setNomeSocial(dto.getNomeSocial());
        cliente.setDataNascimento(dto.getDataNascimento());

        for (EnderecoCadastroDto endereco : dto.getEnderecos()) {
            cliente.getEnderecos().add(paraEndereco(endereco));
        }
        for (DocumentoCadastroDto documento : dto.getDocumentos()) {
            cliente.getDocumentos().add(paraDocumento(documento));
        }
        for (TelefoneCadastroDto telefone : dto.getTelefones()) {
            cliente.getTelefones().add(paraTelefone(telefone));
        }
        return cliente;
    }

    public Cliente paraEntidade(ClienteAtualizacaoDto dto) {
        Cliente cliente = new Cliente();
        cliente.setId(dto.getId());
        cliente.setNome(dto.getNome());
        cliente.setNomeSocial(dto.getNomeSocial());
        cliente.setDataNascimento(dto.getDataNascimento());

        for (EnderecoDto endereco : dto.getEnderecos()) {
            Endereco entidade = paraEndereco(endereco);
            entidade.setId(endereco.getId());
            cliente.getEnderecos().add(entidade);
        }
        for (DocumentoDto documento : dto.getDocumentos()) {
            Documento entidade = paraDocumento(documento);
            entidade.setId(documento.getId());
            cliente.getDocumentos().add(entidade);
        }
        for (TelefoneDto telefone : dto.getTelefones()) {
            Telefone entidade = paraTelefone(telefone);
            entidade.setId(telefone.getId());
            cliente.getTelefones().add(entidade);
        }
        return cliente;
    }

    public ClienteRespostaDto paraResposta(Cliente cliente) {
        ClienteRespostaDto dto = new ClienteRespostaDto();
        dto.setId(cliente.getId());
        dto.setNome(cliente.getNome());
        dto.setNomeSocial(cliente.getNomeSocial());
        dto.setDataNascimento(cliente.getDataNascimento());
        dto.setDataCadastro(cliente.getDataCadastro());

        for (Endereco endereco : cliente.getEnderecos()) {
            dto.getEnderecos().add(paraEnderecoDto(endereco));
        }
        for (Documento documento : cliente.getDocumentos()) {
            dto.getDocumentos().add(paraDocumentoDto(documento));
        }
        for (Telefone telefone : cliente.getTelefones()) {
            dto.getTelefones().add(paraTelefoneDto(telefone));
        }
        return dto;
    }

    public List<ClienteRespostaDto> paraResposta(List<Cliente> clientes) {
        List<ClienteRespostaDto> dtos = new ArrayList<>();
        for (Cliente cliente : clientes) {
            dtos.add(paraResposta(cliente));
        }
        return dtos;
    }

    private Documento paraDocumento(DocumentoDto dto) {
        Documento documento = new Documento();
        documento.setTipo(dto.getTipo());
        documento.setNumero(dto.getNumero());
        return documento;
    }

    private Telefone paraTelefone(TelefoneDto dto) {
        Telefone telefone = new Telefone();
        telefone.setDdd(dto.getDdd());
        telefone.setNumero(dto.getNumero());
        return telefone;
    }

    private Endereco paraEndereco(EnderecoDto dto) {
        Endereco endereco = new Endereco();
        endereco.setEstado(dto.getEstado());
        endereco.setCidade(dto.getCidade());
        endereco.setBairro(dto.getBairro());
        endereco.setRua(dto.getRua());
        endereco.setNumero(dto.getNumero());
        endereco.setCodigoPostal(dto.getCodigoPostal());
        endereco.setInformacoesAdicionais(dto.getInformacoesAdicionais());
        return endereco;
    }

    private DocumentoDto paraDocumentoDto(Documento documento) {
        DocumentoDto dto = new DocumentoDto();
        dto.setId(documento.getId());
        dto.setTipo(documento.getTipo());
        dto.setNumero(documento.getNumero());
        return dto;
    }

    private TelefoneDto paraTelefoneDto(Telefone telefone) {
        TelefoneDto dto = new TelefoneDto();
        dto.setId(telefone.getId());
        dto.setDdd(telefone.getDdd());
        dto.setNumero(telefone.getNumero());
        return dto;
    }

    private EnderecoDto paraEnderecoDto(Endereco endereco) {
        EnderecoDto dto = new EnderecoDto();
        dto.setId(endereco.getId());
        dto.setEstado(endereco.getEstado());
        dto.setCidade(endereco.getCidade());
        dto.setBairro(endereco.getBairro());
        dto.setRua(endereco.getRua());
        dto.setNumero(endereco.getNumero());
        dto.setCodigoPostal(endereco.getCodigoPostal());
        dto.setInformacoesAdicionais(endereco.getInformacoesAdicionais());
        return dto;
    }

    private Documento paraDocumento(DocumentoCadastroDto dto) {
        Documento documento = new Documento();
        documento.setTipo(dto.getTipo());
        documento.setNumero(dto.getNumero());
        return documento;
    }

    private Telefone paraTelefone(TelefoneCadastroDto dto) {
        Telefone telefone = new Telefone();
        telefone.setDdd(dto.getDdd());
        telefone.setNumero(dto.getNumero());
        return telefone;
    }

    private Endereco paraEndereco(EnderecoCadastroDto dto) {
        Endereco endereco = new Endereco();
        endereco.setEstado(dto.getEstado());
        endereco.setCidade(dto.getCidade());
        endereco.setBairro(dto.getBairro());
        endereco.setRua(dto.getRua());
        endereco.setNumero(dto.getNumero());
        endereco.setCodigoPostal(dto.getCodigoPostal());
        endereco.setInformacoesAdicionais(dto.getInformacoesAdicionais());
        return endereco;
    }
}
