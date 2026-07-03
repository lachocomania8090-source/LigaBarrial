package com.example;

import com.example.modelos.*;
import com.example.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DatosIniciales implements CommandLineRunner {

    private final TorneoRepository torneoRepo;
    private final EquipoRepository equipoRepo;
    private final JugadorRepository jugadorRepo;
    private final EstadisticaEquipoRepository estadisticaRepo;
    private final PartidoRepository partidoRepo;
    private final InscripcionRepository inscripcionRepo;

    // Inyección de dependencias por constructor (Buena práctica)
    public DatosIniciales(TorneoRepository torneoRepo, EquipoRepository equipoRepo,
                          JugadorRepository jugadorRepo, EstadisticaEquipoRepository estadisticaRepo,
                          PartidoRepository partidoRepo, InscripcionRepository inscripcionRepo) {
        this.torneoRepo = torneoRepo;
        this.equipoRepo = equipoRepo;
        this.jugadorRepo = jugadorRepo;
        this.estadisticaRepo = estadisticaRepo;
        this.partidoRepo = partidoRepo;
        this.inscripcionRepo = inscripcionRepo;
    }

    @Override
    public void run(String... args) throws Exception {
        // CONDICIÓN: Solo inyectamos si no hay equipos registrados
        if (equipoRepo.count() == 0) {
            System.out.println("🧹 Base de datos vacía detectada. Limpiando residuos y cargando datos de prueba...");

            // PASO 1: Limpiar la base de datos (Borramos el torneo de prueba anterior)
            torneoRepo.deleteAll();
            partidoRepo.deleteAll();
            estadisticaRepo.deleteAll();
            jugadorRepo.deleteAll();
            inscripcionRepo.deleteAll();

            // PASO 2: INYECCIÓN DE DATOS

            // A. Crear Torneo
            Torneo torneo = new Torneo();
            torneo.setNombre("Liga Barrial Estudiantil 2026");
            torneo.setEstado("ACTIVO");
            torneo = torneoRepo.save(torneo);

            // B. Crear Equipos
            Equipo eq1 = new Equipo(); eq1.setNombre("Los Galácticos"); eq1.setCiudad("Quito"); eq1.setEntrenador("Prof. Galarza");
            Equipo eq2 = new Equipo(); eq2.setNombre("Real Bañil"); eq2.setCiudad("Quito"); eq2.setEntrenador("Luis Gómez");
            Equipo eq3 = new Equipo(); eq3.setNombre("Atlético San Pancho"); eq3.setCiudad("Quito"); eq3.setEntrenador("Mario Bros");
            Equipo eq4 = new Equipo(); eq4.setNombre("Deportivo UDLA"); eq4.setCiudad("Quito"); eq4.setEntrenador("Juan Pérez");

            eq1 = equipoRepo.save(eq1);
            eq2 = equipoRepo.save(eq2);
            eq3 = equipoRepo.save(eq3);
            eq4 = equipoRepo.save(eq4);

            // C. Crear Jugadores (Los desarrolladores del proyecto entran a la cancha)
            Jugador j1 = new Jugador(); j1.setNombre("Matheo Escobar"); j1.setEdad(21); j1.setPosicion("Delantero"); j1.setNumero(10); j1.setEquipoId(eq1.getId());
            Jugador j2 = new Jugador(); j2.setNombre("Esteban Chapaca"); j2.setEdad(21); j2.setPosicion("Defensa"); j2.setNumero(4); j2.setEquipoId(eq1.getId());
            Jugador j3 = new Jugador(); j3.setNombre("Michael Murillo"); j3.setEdad(21); j3.setPosicion("Mediocampista"); j3.setNumero(8); j3.setEquipoId(eq1.getId());

            jugadorRepo.save(j1);
            jugadorRepo.save(j2);
            jugadorRepo.save(j3);

            // D. Generar Estadísticas Falsas (Para que la tabla de posiciones ya tenga datos que mostrar)
            // eq1 va ganando la liga con 7 puntos
            estadisticaRepo.save(crearEstadistica(torneo.getId(), eq1.getId(), 3, 2, 1, 0, 8, 3, 7));
            estadisticaRepo.save(crearEstadistica(torneo.getId(), eq2.getId(), 3, 1, 1, 1, 4, 4, 4));
            estadisticaRepo.save(crearEstadistica(torneo.getId(), eq3.getId(), 3, 1, 0, 2, 3, 6, 3));
            estadisticaRepo.save(crearEstadistica(torneo.getId(), eq4.getId(), 3, 0, 2, 1, 2, 4, 2));

            // E. Programar un Partido Futuro
            Partido p1 = new Partido();
            p1.setTorneoId(torneo.getId());
            p1.setEquipoLocalId(eq1.getId());
            p1.setEquipoVisitanteId(eq4.getId());
            p1.setFechaHora(LocalDateTime.now().plusDays(3)); // Partido en 3 días
            p1.setEstado("PROGRAMADO");
            partidoRepo.save(p1);

            System.out.println("✅ ¡Inyección de datos completada con éxito!");

        } else {
            System.out.println("✅ La base de datos ya contiene equipos. Omitiendo inyección para no duplicar datos.");
        }
    }

    // Método auxiliar para no escribir tanto código al crear estadísticas
    private EstadisticaEquipo crearEstadistica(String torneoId, String equipoId, int pj, int pg, int pe, int pp, int gf, int gc, int pts) {
        EstadisticaEquipo est = new EstadisticaEquipo();
        est.setTorneoId(torneoId);
        est.setEquipoId(equipoId);
        est.setPartidosJugados(pj);
        est.setPartidosGanados(pg);
        est.setPartidosEmpatados(pe);
        est.setPartidosPerdidos(pp);
        est.setGolesFavor(gf);
        est.setGolesContra(gc);
        est.setPuntos(pts);
        return est;
    }
}