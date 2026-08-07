package org.example;

import io.github.cdimascio.dotenv.Dotenv;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class MongoConnectionTest {
    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.load();
        String connectionString = dotenv.get("MONGO_URI");

        try (MongoClient mongoClient = MongoClients.create(connectionString)) {
            MongoDatabase database = mongoClient.getDatabase("blogging_platform");
            System.out.println("Connected successfully to: " + database.getName());
        }
    }
}