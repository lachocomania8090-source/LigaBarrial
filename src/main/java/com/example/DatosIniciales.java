package com.example;

import com.example.modelos.*;
import com.example.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class DatosIniciales implements CommandLineRunner {

    private final TorneoRepository torneoRepo;
    private final EquipoRepository equipoRepo;
    private final JugadorRepository jugadorRepo;
    private final EstadisticaEquipoRepository estadisticaRepo;
    private final PartidoRepository partidoRepo;

    public DatosIniciales(TorneoRepository torneoRepo, EquipoRepository equipoRepo,
                          JugadorRepository jugadorRepo, EstadisticaEquipoRepository estadisticaRepo,
                          PartidoRepository partidoRepo) {
        this.torneoRepo = torneoRepo;
        this.equipoRepo = equipoRepo;
        this.jugadorRepo = jugadorRepo;
        this.estadisticaRepo = estadisticaRepo;
        this.partidoRepo = partidoRepo;
    }

    @Override
    public void run(String... args) throws Exception {
        // Forzamos la limpieza absoluta al arrancar para reestructurar la BD
        System.out.println("🧹 Limpiando base de datos completa para nueva inyección de datos estructurales...");
        equipoRepo.deleteAll();
        torneoRepo.deleteAll();
        partidoRepo.deleteAll();
        estadisticaRepo.deleteAll();
        jugadorRepo.deleteAll();

        System.out.println("🚀 Iniciando inyección masiva de datos (6 Equipos, 90 Jugadores, 8 Partidos)...");

        // 1. Crear Torneo Principal
        Torneo torneo = new Torneo();
        torneo.setNombre("Copa Liga Barrial UDLA 2026");
        torneo.setEstado("ACTIVO");
        torneo = torneoRepo.save(torneo);
        String torneoId = torneo.getId();

        // 2. Crear 6 Equipos
        List<Equipo> equipos = new ArrayList<>();
        equipos.add(crearEquipo("Los Galácticos", "Quito Norte", "Prof. Galarza"));
        equipos.add(crearEquipo("Real Bañil", "Quito Centro", "Luis Gómez"));
        equipos.add(crearEquipo("Atlético San Pancho", "Cumbayá", "Mario Bros"));
        equipos.add(crearEquipo("Deportivo UDLA", "Quito Sur", "Juan Pérez"));
        equipos.add(crearEquipo("F.C. Pumas Barriales", "Carapungo", "Carlos Tévez"));
        equipos.add(crearEquipo("Inter de la Mariscal", "La Mariscal", "Hugo Sánchez"));

        for (int i = 0; i < equipos.size(); i++) {
            equipos.set(i, equipoRepo.save(equipos.get(i)));
        }

        // 3. Generar Automáticamente 15 Jugadores por cada uno de los 6 Equipos (90 en total)
        String[] nombresPool = {"Matheo", "Esteban", "Michael", "Carlos", "Juan", "Luis", "Andrés", "Diego", "Jose", "David", "Kevin", "Bryan", "Javier", "Daniel", "Mateo"};
        String[] apellidosPool = {"Escobar", "Chapaca", "Murillo", "Gómez", "Pérez", "López", "Castro", "Mendoza", "Rodríguez", "Aguirre", "Torres", "Silva", "Suárez", "Vaca", "Paredes"};
        String[] posiciones = {"Portero", "Defensa", "Mediocampista", "Delantero"};

        Random random = new Random();

        for (Equipo eq : equipos) {
            for (int j = 1; j <= 15; j++) {
                Jugador jugador = new Jugador();
                // Combinaciones aleatorias para simular realismo, asegurando a los desarrolladores en el equipo 1
                if (eq.getNombre().equals("Los Galácticos") && j == 1) {
                    jugador.setNombre("Matheo Escobar");
                } else if (eq.getNombre().equals("Los Galácticos") && j == 2) {
                    jugador.setNombre("Esteban Chapaca");
                } else if (eq.getNombre().equals("Los Galácticos") && j == 3) {
                    jugador.setNombre("Michael Murillo");
                } else {
                    jugador.setNombre(nombresPool[random.nextInt(nombresPool.length)] + " " + apellidosPool[random.nextInt(apellidosPool.length)]);
                }

                jugador.setEdad(18 + random.nextInt(15)); // Edades entre 18 y 33 años
                jugador.setNumero(j); // Números del 1 al 15
                jugador.setPosicion(posiciones[random.nextInt(posiciones.length)]);
                jugador.setEquipoId(eq.getId());
                jugadorRepo.save(jugador);
            }
        }
        System.out.println("🏃‍♂️ ¡90 Jugadores inyectados con éxito (15 por equipo)!");

        // 4. Inyectar 8 Partidos Disputados con marcadores fijos
        // Guardaremos los partidos en orden cronológico inverso para simular fechas pasadas
        List<Partido> partidosHistorial = new ArrayList<>();

        // Partido 1: Galácticos vs Real Bañil (3 - 1)
        partidosHistorial.add(crearPartidoJugado(torneoId, equipos.get(0).getId(), equipos.get(1).getId(), 3, 1, 10));
        // Partido 2: San Pancho vs Deportivo UDLA (0 - 2)
        partidosHistorial.add(crearPartidoJugado(torneoId, equipos.get(2).getId(), equipos.get(3).getId(), 0, 2, 9));
        // Partido 3: Pumas vs Inter (2 - 2)
        partidosHistorial.add(crearPartidoJugado(torneoId, equipos.get(4).getId(), equipos.get(5).getId(), 2, 2, 8));
        // Partido 4: Galácticos vs San Pancho (4 - 2)
        partidosHistorial.add(crearPartidoJugado(torneoId, equipos.get(0).getId(), equipos.get(2).getId(), 4, 2, 7));
        // Partido 5: Real Bañil vs Pumas (1 - 0)
        partidosHistorial.add(crearPartidoJugado(torneoId, equipos.get(1).getId(), equipos.get(4).getId(), 1, 0, 6));
        // Partido 6: Deportivo UDLA vs Inter (1 - 1)
        partidosHistorial.add(crearPartidoJugado(torneoId, equipos.get(3).getId(), equipos.get(5).getId(), 1, 1, 5));
        // Partido 7: Galácticos vs Pumas (2 - 2)
        partidosHistorial.add(crearPartidoJugado(torneoId, equipos.get(0).getId(), equipos.get(4).getId(), 2, 2, 4));
        // Partido 8: Real Bañil vs Deportivo UDLA (0 - 3)
        partidosHistorial.add(crearPartidoJugado(torneoId, equipos.get(1).getId(), equipos.get(3).getId(), 0, 3, 3));

        partidoRepo.saveAll(partidosHistorial);
        System.out.println("⚽ ¡8 Partidos históricos registrados en la base de datos!");

        // 5. Inyectar Estadísticas del Torneo que coinciden matemáticamente con los 8 partidos jugados
        // Formato: crearEstadistica(torneoId, equipoId, PJ, PG, PE, PP, GF, GC, Puntos)

        // Los Galácticos: 3 partidos (2 ganados, 1 empatado, 0 perdidos). GF: 9, GC: 5. Puntos: 7
        estadisticaRepo.save(crearEstadistica(torneoId, equipos.get(0).getId(), 3, 2, 1, 0, 9, 5, 7));

        // Deportivo UDLA: 3 partidos (2 ganados, 1 empatado, 0 perdidos). GF: 6, GC: 1. Puntos: 7
        estadisticaRepo.save(crearEstadistica(torneoId, equipos.get(3).getId(), 3, 2, 1, 0, 6, 1, 7));

        // Real Bañil: 3 partidos (1 ganado, 0 empatados, 2 perdidos). GF: 2, GC: 6. Puntos: 3
        estadisticaRepo.save(crearEstadistica(torneoId, equipos.get(1).getId(), 3, 1, 0, 2, 2, 6, 3));

        // Inter de la Mariscal: 2 partidos (0 ganados, 2 empatados, 0 perdidos). GF: 3, GC: 3. Puntos: 2
        estadisticaRepo.save(crearEstadistica(torneoId, equipos.get(5).getId(), 2, 0, 2, 0, 3, 3, 2));

        // F.C. Pumas Barriales: 3 partidos (0 ganados, 2 empatados, 1 perdido). GF: 4, GC: 5. Puntos: 2
        estadisticaRepo.save(crearEstadistica(torneoId, equipos.get(4).getId(), 3, 0, 2, 1, 4, 5, 2));

        // Atlético San Pancho: 2 partidos (0 ganados, 0 empatados, 2 perdidos). GF: 2, GC: 6. Puntos: 0
        estadisticaRepo.save(crearEstadistica(torneoId, equipos.get(2).getId(), 2, 0, 0, 2, 2, 6, 0));

        // 6. Registrar un partido futuro (PROGRAMADO) para mantener dinámica la sección de partidos
        Partido partidoFuturo = new Partido();
        partidoFuturo.setTorneoId(torneoId);
        partidoFuturo.setEquipoLocalId(equipos.get(2).getId()); // San Pancho
        partidoFuturo.setEquipoVisitanteId(equipos.get(5).getId()); // Inter
        partidoFuturo.setFechaHora(LocalDateTime.now().plusDays(4));
        partidoFuturo.setEstado("PROGRAMADO");
        partidoRepo.save(partidoFuturo);

        System.out.println("✅ ¡Inyección masiva estructural completada de manera exitosa!");
    }

    // Métodos auxiliares de creación rápida
    private Equipo crearEquipo(String nombre, String ciudad, String entrenador) {
        Equipo eq = new Equipo();
        eq.setNombre(nombre);
        eq.setCiudad(ciudad);
        eq.setEntrenador(entrenador);
        return eq;
    }

    private Partido crearPartidoJugado(String torneoId, String localId, String visitanteId, int golesL, int golesV, int diasAtras) {
        Partido p = new Partido();
        p.setTorneoId(torneoId);
        p.setEquipoLocalId(localId);
        p.setEquipoVisitanteId(visitanteId);
        p.setGolesLocal(golesL);
        p.setGolesVisitante(golesV);
        p.setFechaHora(LocalDateTime.now().minusDays(diasAtras));
        p.setEstado("JUGADO");
        return p;
    }

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