package com.example.logica;

import com.example.modelos.Jugador;
import com.example.repository.JugadorRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.Optional;

@Service
public class JugadorService {

    @Autowired
    private JugadorRepository jugadorRepo;

    public List<Jugador> obtenerTodos() {
        return jugadorRepo.findAll();
    }

    public Optional<Jugador> obtenerPorId(String id) {
        return jugadorRepo.findById(id);
    }

    public List<Jugador> obtenerPorEquipo(String equipoId) {
        return jugadorRepo.findByEquipoId(equipoId);
    }

    public Jugador guardar(Jugador jugador) {
        return jugadorRepo.save(jugador);
    }

    public void eliminar(String id) {
        jugadorRepo.deleteById(id);
    }

    public void eliminar(Jugador jugador) {
        jugadorRepo.delete(jugador);
    }
}

