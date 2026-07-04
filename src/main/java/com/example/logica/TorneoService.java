package com.example.logica;

import com.example.modelos.EstadisticaEquipo;
import com.example.modelos.Inscripcion;
import com.example.modelos.Torneo;
import com.example.repository.EstadisticaEquipoRepository;
import com.example.repository.InscripcionRepository;
import com.example.repository.TorneoRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

@Service
public class TorneoService {

    @Autowired
    private TorneoRepository torneoRepo;
    @Autowired
    private InscripcionRepository inscripcionRepo;
    @Autowired
    private EstadisticaEquipoRepository estadisticaRepo;

    public Torneo crearTorneo(Torneo torneo) {
        torneo.setEstado("ACTIVO");
        return torneoRepo.save(torneo);
    }

    public List<Torneo> obtenerTodos() {
        return torneoRepo.findAll();
    }

    public Inscripcion inscribirEquipo(String torneoId, String equipoId, double valor) {
        // 1. Guardar la inscripción
        Inscripcion inscripcion = new Inscripcion();
        inscripcion.setTorneoId(torneoId);
        inscripcion.setEquipoId(equipoId);
        inscripcion.setValorInscripcion(valor);
        inscripcion.setEstadoPago("PENDIENTE");
        inscripcionRepo.save(inscripcion);

        // 2. Inicializar estadísticas en cero para este torneo
        EstadisticaEquipo stats = new EstadisticaEquipo();
        stats.setTorneoId(torneoId);
        stats.setEquipoId(equipoId);
        estadisticaRepo.save(stats);

        return inscripcion;
    }
}
