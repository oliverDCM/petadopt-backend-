package com.petadopt.infrastructure.adapter.out.persistence;

import com.petadopt.domain.model.EstadoAnimal;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AnimalJpaRepository extends JpaRepository<AnimalEntity, Long> {

    List<AnimalEntity> findByEstado(EstadoAnimal estado);

    List<AnimalEntity> findByVendedorId(Long vendedorId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from AnimalEntity a where a.id = :id")
    Optional<AnimalEntity> buscarBloqueadoPorId(@Param("id") Long id);
}
