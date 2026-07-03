package com.example.logica;

import com.example.Modelos.Torneo;

public class TorneoService {

    public Torneo crearTorneo(Torneo torneo) {
        torneo.setEstado("ACTIVO");
        return torneoRepo.save(torneo);
    }
}
