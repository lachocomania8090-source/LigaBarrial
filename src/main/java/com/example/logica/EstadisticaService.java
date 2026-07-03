package com.example.logica;

import com.example.modelos.EstadisticaEquipo;
import com.example.repository.EstadisticaEquipoRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.Comparator;
import java.util.stream.Collectors;

@Service
public class EstadisticaService {

    @Autowired
    private EstadisticaEquipoRepository estadisticaRepo;

    public List<EstadisticaEquipo> obtenerTablaPosiciones(String torneoId) {
        List<EstadisticaEquipo> estadisticas = estadisticaRepo.findByTorneoId(torneoId);

        // Ordenar por puntos (descendente) y luego por diferencia de goles (descendente)
        return estadisticas.stream()
                .sorted(Comparator.comparingInt(EstadisticaEquipo::getPuntos).reversed()
                        .thenComparing(e -> ((EstadisticaEquipo) e).getDiferenciaGoles()).reversed())
                .collect(Collectors.toList());
    }
}