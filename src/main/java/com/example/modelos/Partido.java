package com.example.modelos;

import java.time.LocalDateTime;

public class Partido {
    private String id;
    private String torneoId;
    private String equipoLocalId;
    private String equipoVisitanteId;
    private LocalDateTime fechaHora;
    private int golesLocal;
    private int golesVisitante;
    private String estado;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTorneoId() {
        return torneoId;
    }

    public void setTorneoId(String torneoId) {
        this.torneoId = torneoId;
    }

    public String getEquipoLocalId() {
        return equipoLocalId;
    }

    public void setEquipoLocalId(String equipoLocalId) {
        this.equipoLocalId = equipoLocalId;
    }

    public String getEquipoVisitanteId() {
        return equipoVisitanteId;
    }

    public void setEquipoVisitanteId(String equipoVisitanteId) {
        this.equipoVisitanteId = equipoVisitanteId;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public int getGolesLocal() {
        return golesLocal;
    }

    public void setGolesLocal(int golesLocal) {
        this.golesLocal = golesLocal;
    }

    public int getGolesVisitante() {
        return golesVisitante;
    }

    public void setGolesVisitante(int golesVisitante) {
        this.golesVisitante = golesVisitante;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
