package com.autobots.automanager.entidades;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
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
	@OneToMany(mappedBy = "venda", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("id")
	private List<ItemVenda> itens = new ArrayList<>();
	@OneToMany(mappedBy = "venda", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("id")
	private List<ItemServico> itensServico = new ArrayList<>();

	public List<Mercadoria> getMercadorias() {
		return itens.stream().map(ItemVenda::getMercadoria).collect(Collectors.toList());
	}

	public List<Servico> getServicos() {
		return itensServico.stream().map(ItemServico::getServico).collect(Collectors.toList());
	}
}
