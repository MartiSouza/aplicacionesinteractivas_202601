package com.uade.tpejemplo.repository;

import com.uade.tpejemplo.model.Credito;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface CreditoRepository extends JpaRepository<Credito, Long> {

    List<Credito> findByClienteDni(String dni);

    @Query("select coalesce(sum(c.deudaOriginal), 0) from Credito c")
    BigDecimal sumDeudaOriginal();

    @EntityGraph(attributePaths = "cuotas")
    @Query("select distinct c from Credito c left join fetch c.cuotas")
    List<Credito> findAllWithCuotas();
}
