package com.autobots.automanager.entidades;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class ItemVenda {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@ManyToOne(optional = false)
	@JoinColumn(name = "venda_id")
	private Venda venda;
	@ManyToOne(optional = false)
	@JoinColumn(name = "mercadoria_id")
	private Mercadoria mercadoria;
	@Column(nullable = false)
	private long quantidade;
	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal valorUnitario;
}
