package com.example.logica;

import com.example.modelos.EstadisticaEquipo;
import com.example.modelos.Inscripcion;
import com.example.modelos.Torneo;
import com.example.repository.EstadisticaEquipoRepository;
import com.example.repository.InscripcionRepository;
import com.example.repository.TorneoRepository;
import com.example.repository.EquipoRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Comparator;
import java.util.List;

@Service
public class TorneoService {

    @Autowired
    private TorneoRepository torneoRepo;
    @Autowired
    private InscripcionRepository inscripcionRepo;
    @Autowired
    private EstadisticaEquipoRepository estadisticaRepo;
    @Autowired
    private EquipoService equipoService;
    @Autowired
    private EquipoRepository equipoRepository;

    public Torneo crearTorneo(Torneo torneo) {
        torneo.setEstado("ACTIVO");
        Torneo torneoGuardado = torneoRepo.save(torneo);

        // Crear registros de estadísticas iniciales para todos los equipos
        List<com.example.modelos.Equipo> equipos = equipoRepository.findAll();
        for (com.example.modelos.Equipo equipo : equipos) {
            EstadisticaEquipo estadistica = new EstadisticaEquipo();
            estadistica.setTorneoId(torneoGuardado.getId());
            estadistica.setEquipoId(equipo.getId());
            estadistica.setPartidosJugados(0);
            estadistica.setPartidosGanados(0);
            estadistica.setPartidosEmpatados(0);
            estadistica.setPartidosPerdidos(0);
            estadistica.setGolesFavor(0);
            estadistica.setGolesContra(0);
            estadistica.setPuntos(0);
            estadisticaRepo.save(estadistica);
        }

        return torneoGuardado;
    }

    public Torneo actualizar(Torneo torneo) {
        // Si el torneo está siendo finalizado, registrar al ganador
        if ("FINALIZADO".equalsIgnoreCase(torneo.getEstado()) && torneo.getNombreCampeon() == null) {
            registrarGanador(torneo);
        }
        return torneoRepo.save(torneo);
    }

    private void registrarGanador(Torneo torneo) {
        // Obtener todas las estadísticas del torneo
        List<EstadisticaEquipo> estadisticas = estadisticaRepo.findByTorneoId(torneo.getId());

        if (estadisticas == null || estadisticas.isEmpty()) {
            return;
        }

        // Ordenar por puntos (descendente) y por diferencia de goles (descendente) como desempate
        EstadisticaEquipo ganador = estadisticas.stream()
            .sorted(
                Comparator.comparing(EstadisticaEquipo::getPuntos)
                    .reversed()
                    .thenComparing(est -> est.getGolesFavor() - est.getGolesContra(), Comparator.reverseOrder())
            )
            .findFirst()
            .orElse(null);

        // Si encontramos un ganador, asignar su nombre al torneo
        if (ganador != null) {
            String nombreEquipoGanador = equipoService.obtenerPorId(ganador.getEquipoId())
                .map(equipo -> equipo.getNombre())
                .orElse("Desconocido");
            torneo.setNombreCampeon(nombreEquipoGanador);
        }
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
