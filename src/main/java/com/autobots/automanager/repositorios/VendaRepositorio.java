package com.autobots.automanager.repositorios;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.autobots.automanager.entidades.Venda;

public interface VendaRepositorio extends JpaRepository<Venda, Long> {
    boolean existsByClienteIdOrFuncionarioId(Long clienteId, Long funcionarioId);

    boolean existsByVeiculoId(Long veiculoId);

    boolean existsByMercadoriasId(Long mercadoriaId);

    boolean existsByServicosId(Long servicoId);

    List<Venda> findByClienteIdOrFuncionarioIdOrderById(Long clienteId, Long funcionarioId);
}
