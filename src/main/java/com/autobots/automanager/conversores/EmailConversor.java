package com.autobots.automanager.conversores;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.autobots.automanager.dtos.EmailAtualizacaoDto;
import com.autobots.automanager.dtos.EmailCadastroDto;
import com.autobots.automanager.dtos.EmailDto;
import com.autobots.automanager.entidades.Email;

@Component
public class EmailConversor {

    public Email paraEntidade(EmailCadastroDto dto) {
        Email email = new Email();
        email.setEndereco(dto.getEndereco());
        return email;
    }

    public Email paraEntidade(EmailAtualizacaoDto dto) {
        Email email = new Email();
        email.setEndereco(dto.getEndereco());
        return email;
    }

    public Email paraEntidade(EmailDto dto) {
        Email email = new Email();
        email.setId(dto.getId());
        email.setEndereco(dto.getEndereco());
        return email;
    }

    public EmailDto paraResposta(Email email) {
        EmailDto dto = new EmailDto();
        dto.setId(email.getId());
        dto.setEndereco(email.getEndereco());
        return dto;
    }

    public List<EmailDto> paraResposta(List<Email> emails) {
        List<EmailDto> dtos = new ArrayList<>();
        for (Email email : emails) {
            dtos.add(paraResposta(email));
        }
        return dtos;
    }
}
