package com.example.repository; // Ajusta a tu paquete actual

import com.example.modelos.Equipo; // Ajusta a tu paquete de modelos
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EquipoRepository extends MongoRepository<Equipo, String> {
    // Hereda todos los métodos CRUD básicos automáticamente
}
