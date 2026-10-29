package com.autobots.automanager.entidades;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Empresa {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(nullable = false)
	private String razaoSocial;
	@Column
	private String nomeFantasia;
	@Column(nullable = false)
	private Date cadastro;
	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
	private Endereco endereco;
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "empresa_id")
	@OrderBy("id")
	private List<Telefone> telefones = new ArrayList<>();
	@OneToMany
	@JoinColumn(name = "empresa_id")
	@OrderBy("id")
	private List<Usuario> usuarios = new ArrayList<>();
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "empresa_id")
	@OrderBy("id")
	private List<Mercadoria> mercadorias = new ArrayList<>();
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "empresa_id")
	@OrderBy("id")
	private List<Servico> servicos = new ArrayList<>();
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "empresa_id")
	@OrderBy("id")
	private List<Venda> vendas = new ArrayList<>();
}
