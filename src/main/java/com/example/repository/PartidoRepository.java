package com.example.repository;

import com.example.modelos.Partido;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List; // <-- No olvides este import para las listas

@Repository
public interface PartidoRepository extends MongoRepository<Partido, String> {

    // Spring Data genera automáticamente la consulta: { "torneoId": torneoId }
    List<Partido> findByTorneoId(String torneoId);

}