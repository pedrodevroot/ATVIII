package com.autobots.automanager.repositorios;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.autobots.automanager.entidades.Venda;

public interface VendaRepositorio extends JpaRepository<Venda, Long> {
    boolean existsByClienteIdOrFuncionarioId(Long clienteId, Long funcionarioId);

    boolean existsByVeiculoId(Long veiculoId);

    boolean existsByItensMercadoriaId(Long mercadoriaId);

    boolean existsByItensServicoServicoId(Long servicoId);

    boolean existsByClienteId(Long clienteId);

    boolean existsByFuncionarioId(Long funcionarioId);

    List<Venda> findByClienteIdOrFuncionarioIdOrderById(Long clienteId, Long funcionarioId);
}
