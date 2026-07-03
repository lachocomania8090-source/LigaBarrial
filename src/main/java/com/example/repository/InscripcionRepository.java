package com.example.repository;

import com.example.modelos.Inscripcion;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InscripcionRepository extends MongoRepository<Inscripcion, String> {
}