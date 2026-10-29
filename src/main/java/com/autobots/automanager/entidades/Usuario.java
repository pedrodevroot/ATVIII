package com.autobots.automanager.entidades;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;

import com.autobots.automanager.enumeracoes.PerfilUsuario;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Usuario {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(nullable = false)
	private String nome;
	@Column
	private String nomeSocial;
	@Column
	private Date dataNascimento;
	@Column
	private Date dataCadastro;
	@ElementCollection(fetch = FetchType.EAGER)
	@Enumerated(EnumType.STRING)
	private Set<PerfilUsuario> perfis = new HashSet<>();
	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
	private Endereco endereco;
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "usuario_id")
	@OrderBy("id")
	private List<Telefone> telefones = new ArrayList<>();
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "usuario_id")
	@OrderBy("id")
	private List<Documento> documentos = new ArrayList<>();
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "usuario_id")
	@OrderBy("id")
	private List<Email> emails = new ArrayList<>();
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "usuario_id")
	@OrderBy("id")
	private List<Credencial> credenciais = new ArrayList<>();
	@OneToMany
	@JoinColumn(name = "fornecedor_id")
	@OrderBy("id")
	private List<Mercadoria> mercadorias = new ArrayList<>();
	@OneToMany(mappedBy = "cliente")
	@OrderBy("id")
	private List<Venda> vendas = new ArrayList<>();
	@OneToMany(mappedBy = "proprietario", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("id")
	private List<Veiculo> veiculos = new ArrayList<>();
}
