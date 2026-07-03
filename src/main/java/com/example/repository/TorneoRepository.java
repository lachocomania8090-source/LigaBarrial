package com.example.application.repository;

import com.example.application.model.Torneo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TorneoRepository extends MongoRepository<Torneo, String> {
}