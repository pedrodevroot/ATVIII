package com.autobots.automanager.conversores;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.autobots.automanager.dtos.DocumentoAtualizacaoDto;
import com.autobots.automanager.dtos.DocumentoCadastroDto;
import com.autobots.automanager.dtos.DocumentoDto;
import com.autobots.automanager.entidades.Documento;

@Component
public class DocumentoConversor {

    public Documento paraEntidade(DocumentoCadastroDto dto) {
        Documento documento = new Documento();
        documento.setTipo(dto.getTipo());
        documento.setNumero(dto.getNumero());
        return documento;
    }

    public Documento paraEntidade(DocumentoAtualizacaoDto dto) {
        Documento documento = new Documento();
        documento.setTipo(dto.getTipo());
        documento.setNumero(dto.getNumero());
        return documento;
    }

    public DocumentoDto paraResposta(Documento documento) {
        DocumentoDto dto = new DocumentoDto();
        dto.setId(documento.getId());
        dto.setTipo(documento.getTipo());
        dto.setNumero(documento.getNumero());
        return dto;
    }

    public List<DocumentoDto> paraResposta(List<Documento> documentos) {
        List<DocumentoDto> dtos = new ArrayList<>();
        for (Documento documento : documentos) {
            dtos.add(paraResposta(documento));
        }
        return dtos;
    }
}
