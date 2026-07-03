package com.example;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;

@Configuration
public class MongoConfig extends AbstractMongoClientConfiguration {

    @Override
    protected String getDatabaseName() {
        return "liga_barrial"; // Aquí definimos la base de datos
    }

    @Override
    public MongoClient mongoClient() {
        // Con esto obligamos al sistema a usar esta URI sí o sí
        return MongoClients.create("mongodb+srv://adminLiga:F1lPiKTuoGFHL7JC@cluster0.rhe2iyi.mongodb.net/?retryWrites=true&w=majority");
    }
}