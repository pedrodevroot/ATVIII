package com.autobots.automanager.entidades;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;

import com.autobots.automanager.enumeracoes.TipoVeiculo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Veiculo {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private TipoVeiculo tipo;
	@Column(nullable = false)
	private String modelo;
	@Column(nullable = false, unique = true)
	private String placa;
	@ManyToOne(optional = false)
	@JoinColumn(name = "proprietario_id")
	private Usuario proprietario;
	@OneToMany(mappedBy = "veiculo")
	@OrderBy("id")
	private List<Venda> vendas = new ArrayList<>();
}
