package com.petadopt.infrastructure.adapter.out.persistence;

import com.petadopt.domain.model.EstadoAnimal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnimalJpaRepository extends JpaRepository<AnimalEntity, Long> {

    List<AnimalEntity> findByEstado(EstadoAnimal estado);

    List<AnimalEntity> findByVendedorId(Long vendedorId);
}