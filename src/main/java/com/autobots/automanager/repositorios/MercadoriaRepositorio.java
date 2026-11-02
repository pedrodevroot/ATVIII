package com.autobots.automanager.repositorios;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.autobots.automanager.entidades.Mercadoria;

public interface MercadoriaRepositorio extends JpaRepository<Mercadoria, Long> {

    @Modifying
    @Query("update Mercadoria m set m.quantidade = m.quantidade - :quantidade "
            + "where m.id = :id and m.quantidade >= :quantidade")
    int baixarEstoque(@Param("id") Long id, @Param("quantidade") long quantidade);

    @Modifying
    @Query("update Mercadoria m set m.quantidade = m.quantidade + :quantidade where m.id = :id")
    int devolverEstoque(@Param("id") Long id, @Param("quantidade") long quantidade);
}
