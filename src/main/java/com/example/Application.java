package com.example; // Asegúrate de que sea tu paquete

import com.example.modelos.Torneo;
import com.example.repository.TorneoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        // 👇 ESTA ES EL ARMA SECRETA 👇
        // Forzamos la conexión a la nube, ignorando el application.properties
        System.setProperty("spring.data.mongodb.uri", "mongodb+srv://adminLiga:F1lPiKTuoGFHL7JC@cluster0.rhe2iyi.mongodb.net/liga_barrial?retryWrites=true&w=majority");

        SpringApplication.run(Application.class, args);
    }

    @Bean
    public CommandLineRunner probarConexion(TorneoRepository torneoRepo) {
        return args -> {
            System.out.println("⏳ Conectando a MongoDB Atlas...");
            Torneo torneoPrueba = new Torneo();
            torneoPrueba.setNombre("Torneo de Prueba Blindado");
            torneoPrueba.setEstado("ACTIVO");
            torneoRepo.save(torneoPrueba);
            System.out.println("✅ ¡Conexión exitosa! Torneo guardado en la nube.");
        };
    }
}