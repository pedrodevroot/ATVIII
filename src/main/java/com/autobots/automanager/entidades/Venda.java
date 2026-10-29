package com.autobots.automanager.entidades;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OrderBy;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Venda {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(nullable = false)
	private Date cadastro;
	@Column(nullable = false, unique = true)
	private String identificacao;
	@ManyToOne(optional = false)
	@JoinColumn(name = "cliente_id")
	private Usuario cliente;
	@ManyToOne(optional = false)
	@JoinColumn(name = "funcionario_id")
	private Usuario funcionario;
	@ManyToOne
	@JoinColumn(name = "veiculo_id")
	private Veiculo veiculo;
	@ManyToMany
	@JoinTable(name = "venda_mercadoria",
			joinColumns = @JoinColumn(name = "venda_id"),
			inverseJoinColumns = @JoinColumn(name = "mercadoria_id"))
	@OrderBy("id")
	private List<Mercadoria> mercadorias = new ArrayList<>();
	@ManyToMany
	@JoinTable(name = "venda_servico",
			joinColumns = @JoinColumn(name = "venda_id"),
			inverseJoinColumns = @JoinColumn(name = "servico_id"))
	@OrderBy("id")
	private List<Servico> servicos = new ArrayList<>();
}
