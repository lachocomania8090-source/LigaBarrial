package com.example.logica;

import com.example.modelos.Equipo;
import com.example.repository.EquipoRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.Optional;

@Service
public class EquipoService {

    @Autowired
    private EquipoRepository equipoRepo;

    public List<Equipo> obtenerTodos() {
        return equipoRepo.findAll();
    }

    public Optional<Equipo> obtenerPorId(String id) {
        return equipoRepo.findById(id);
    }

    public Equipo guardar(Equipo equipo) {
        return equipoRepo.save(equipo);
    }

    public void eliminar(String id) {
        equipoRepo.deleteById(id);
    }

    public void eliminar(Equipo equipo) {
        equipoRepo.delete(equipo);
    }
}

