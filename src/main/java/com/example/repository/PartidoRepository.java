package com.example.application.repository;

import com.example.application.model.Partido;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PartidoRepository extends MongoRepository<Partido, String> {
}