package com.autobots.automanager;

import java.util.Calendar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.autobots.automanager.entidades.Cliente;
import com.autobots.automanager.entidades.Documento;
import com.autobots.automanager.entidades.Endereco;
import com.autobots.automanager.entidades.Telefone;
import com.autobots.automanager.repositorios.ClienteRepositorio;

@Component
public class CargaInicial implements ApplicationRunner {

    @Autowired
    private ClienteRepositorio repositorio;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (repositorio.count() > 0) {
            return;
        }

        Calendar calendario = Calendar.getInstance();
        calendario.set(2002, Calendar.MAY, 15);

        Cliente cliente = new Cliente();
        cliente.setNome("Pedro Alcantara de Braganca e Bourbon");
        cliente.setDataCadastro(Calendar.getInstance().getTime());
        cliente.setDataNascimento(calendario.getTime());
        cliente.setNomeSocial("Dom Pedro");

        Telefone telefone = new Telefone();
        telefone.setDdd("21");
        telefone.setNumero("981234576");
        cliente.getTelefones().add(telefone);

        Endereco endereco = new Endereco();
        endereco.setEstado("Rio de Janeiro");
        endereco.setCidade("Rio de Janeiro");
        endereco.setBairro("Copacabana");
        endereco.setRua("Avenida Atlantica");
        endereco.setNumero("1702");
        endereco.setCodigoPostal("22021001");
        endereco.setInformacoesAdicionais("Hotel Copacabana Palace");
        cliente.getEnderecos().add(endereco);

        Documento rg = new Documento();
        rg.setTipo("RG");
        rg.setNumero("1500");

        Documento cpf = new Documento();
        cpf.setTipo("CPF");
        cpf.setNumero("00000000001");

        cliente.getDocumentos().add(rg);
        cliente.getDocumentos().add(cpf);

        repositorio.save(cliente);
    }
}
