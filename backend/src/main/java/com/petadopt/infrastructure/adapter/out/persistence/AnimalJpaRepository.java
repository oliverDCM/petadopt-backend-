package com.petadopt.infrastructure.adapter.out.persistence;

import com.petadopt.domain.model.EspecieAnimal;
import com.petadopt.domain.model.EstadoAnimal;
import com.petadopt.domain.model.TipoPublicacion;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AnimalJpaRepository extends JpaRepository<AnimalEntity, Long> {

    /**
     * Catálogo paginado. Los parámetros nulos no filtran. La ciudad se compara sin tildes y sin distinguir
     * mayúsculas: {@code patronCiudad} llega ya normalizado y con los comodines de LIKE (por ejemplo "%bogota%").
     */
    @Query("""
            select a from AnimalEntity a
            where a.estado = :estado
              and (:especie is null or a.especie = :especie)
              and (:tipo is null or a.tipo = :tipo)
              and (:patronCiudad is null
                   or lower(function('translate', a.ciudad, 'ÁÉÍÓÚÜÑáéíóúüñ', 'AEIOUUNaeiouun'))
                      like :patronCiudad escape '!')
            """)
    Page<AnimalEntity> buscarCatalogo(@Param("estado") EstadoAnimal estado,
                                      @Param("especie") EspecieAnimal especie,
                                      @Param("tipo") TipoPublicacion tipo,
                                      @Param("patronCiudad") String patronCiudad,
                                      Pageable pageable);

    List<AnimalEntity> findByVendedorIdOrderByFechaPublicacionDescIdDesc(Long vendedorId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from AnimalEntity a where a.id = :id")
    Optional<AnimalEntity> buscarBloqueadoPorId(@Param("id") Long id);
}
