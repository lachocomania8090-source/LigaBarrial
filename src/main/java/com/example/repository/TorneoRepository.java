package com.example.repository;

import com.example.modelos.Torneo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TorneoRepository extends MongoRepository<Torneo, String> {
}