package com.example.application.repository;

import com.example.application.model.EstadisticaEquipo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EstadisticaEquipoRepository extends MongoRepository<EstadisticaEquipo, String> {
    // Para listar la tabla de un torneo
    List<EstadisticaEquipo> findByTorneoId(String torneoId);

    // Para encontrar la celda de estadística exacta a modificar tras un partido
    EstadisticaEquipo findByTorneoIdAndEquipoId(String torneoId, String equipoId);
}