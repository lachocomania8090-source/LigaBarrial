package com.example.logica;

import com.example.modelos.EstadisticaEquipo;
import com.example.modelos.Partido;
import com.example.repository.EstadisticaEquipoRepository;
import com.example.repository.PartidoRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.Optional;

@Service
public class PartidoService {

    @Autowired
    private PartidoRepository partidoRepo;
    @Autowired
    private EstadisticaEquipoRepository estadisticaRepo;

    public List<Partido> obtenerTodos() {
        return partidoRepo.findAll();
    }

    public Optional<Partido> obtenerPorId(String id) {
        return partidoRepo.findById(id);
    }

    public Partido guardar(Partido partido) {
        return partidoRepo.save(partido);
    }

    public void eliminar(String id) {
        partidoRepo.deleteById(id);
    }

    public Partido registrarResultado(String partidoId, int golesLocal, int golesVisitante) {
        Partido partido = partidoRepo.findById(partidoId)
                .orElseThrow(() -> new RuntimeException("Partido no encontrado"));

        if ("JUGADO".equals(partido.getEstado())) {
            throw new RuntimeException("El partido ya fue jugado");
        }

        // Actualizar datos del partido
        partido.setGolesLocal(golesLocal);
        partido.setGolesVisitante(golesVisitante);
        partido.setEstado("JUGADO");
        partidoRepo.save(partido);

        // Actualizar estadísticas de ambos equipos
        actualizarEstadisticas(partido);

        return partido;
    }

    private void actualizarEstadisticas(Partido partido) {
        EstadisticaEquipo local = estadisticaRepo.findByTorneoIdAndEquipoId(
                partido.getTorneoId(), partido.getEquipoLocalId());
        EstadisticaEquipo visitante = estadisticaRepo.findByTorneoIdAndEquipoId(
                partido.getTorneoId(), partido.getEquipoVisitanteId());

        // Actualizar goles y partidos jugados
        local.setPartidosJugados(local.getPartidosJugados() + 1);
        local.setGolesFavor(local.getGolesFavor() + partido.getGolesLocal());
        local.setGolesContra(local.getGolesContra() + partido.getGolesVisitante());

        visitante.setPartidosJugados(visitante.getPartidosJugados() + 1);
        visitante.setGolesFavor(visitante.getGolesFavor() + partido.getGolesVisitante());
        visitante.setGolesContra(visitante.getGolesContra() + partido.getGolesLocal());

        // Lógica de puntos
        if (partido.getGolesLocal() > partido.getGolesVisitante()) {
            local.setPuntos(local.getPuntos() + 3);
            local.setPartidosGanados(local.getPartidosGanados() + 1);
            visitante.setPartidosPerdidos(visitante.getPartidosPerdidos() + 1);
        } else if (partido.getGolesLocal() < partido.getGolesVisitante()) {
            visitante.setPuntos(visitante.getPuntos() + 3);
            visitante.setPartidosGanados(visitante.getPartidosGanados() + 1);
            local.setPartidosPerdidos(local.getPartidosPerdidos() + 1);
        } else {
            local.setPuntos(local.getPuntos() + 1);
            local.setPartidosEmpatados(local.getPartidosEmpatados() + 1);
            visitante.setPuntos(visitante.getPuntos() + 1);
            visitante.setPartidosEmpatados(visitante.getPartidosEmpatados() + 1);
        }

        estadisticaRepo.save(local);
        estadisticaRepo.save(visitante);
    }
}