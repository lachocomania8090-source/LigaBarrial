package com.example.application.repository;

import com.example.application.model.Jugador;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface JugadorRepository extends MongoRepository<Jugador, String> {
    // Spring infiere la consulta automáticamente por el nombre del método (findBy + Atributo)
    List<Jugador> findByEquipoId(String equipoId);
}