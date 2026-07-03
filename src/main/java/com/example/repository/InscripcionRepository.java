package com.example.application.repository;

import com.example.application.model.Inscripcion;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InscripcionRepository extends MongoRepository<Inscripcion, String> {
}