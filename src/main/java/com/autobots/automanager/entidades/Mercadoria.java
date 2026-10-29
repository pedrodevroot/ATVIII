package com.autobots.automanager.entidades;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Data;

@Data
@Entity
public class Mercadoria {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column
	private Date validade;
	@Column(nullable = false)
	private Date fabricacao;
	@Column(nullable = false)
	private Date cadastro;
	@Column(nullable = false)
	private String nome;
	@Column(nullable = false)
	private long quantidade;
	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal valor;
	@Column
	private String descricao;
}
