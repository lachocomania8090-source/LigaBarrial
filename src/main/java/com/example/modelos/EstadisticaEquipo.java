package com.example.modelos;

public class EstadisticaEquipo {

    private String id;
    private String torneoId;
    private String equipoId;

    // Estadísticas para la tabla de posiciones
    private int partidosJugados = 0;
    private int partidosGanados = 0;
    private int partidosEmpatados = 0;
    private int partidosPerdidos = 0;
    private int golesFavor = 0;
    private int golesContra = 0;
    private int puntos = 0;

    //Metodo para calcular diferencia de goles de ser necesario
    public int getDiferenciaGoles() {
        return golesFavor - golesContra;
    }

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

    public String getEquipoId() {
        return equipoId;
    }

    public void setEquipoId(String equipoId) {
        this.equipoId = equipoId;
    }

    public int getPartidosJugados() {
        return partidosJugados;
    }

    public void setPartidosJugados(int partidosJugados) {
        this.partidosJugados = partidosJugados;
    }

    public int getPartidosGanados() {
        return partidosGanados;
    }

    public void setPartidosGanados(int partidosGanados) {
        this.partidosGanados = partidosGanados;
    }

    public int getPartidosEmpatados() {
        return partidosEmpatados;
    }

    public void setPartidosEmpatados(int partidosEmpatados) {
        this.partidosEmpatados = partidosEmpatados;
    }

    public int getPartidosPerdidos() {
        return partidosPerdidos;
    }

    public void setPartidosPerdidos(int partidosPerdidos) {
        this.partidosPerdidos = partidosPerdidos;
    }

    public int getGolesFavor() {
        return golesFavor;
    }

    public void setGolesFavor(int golesFavor) {
        this.golesFavor = golesFavor;
    }

    public int getGolesContra() {
        return golesContra;
    }

    public void setGolesContra(int golesContra) {
        this.golesContra = golesContra;
    }

    public int getPuntos() {
        return puntos;
    }

    public void setPuntos(int puntos) {
        this.puntos = puntos;
    }
}
