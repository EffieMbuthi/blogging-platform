package org.example;

import io.github.cdimascio.dotenv.Dotenv;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class MongoConnection {
    private static MongoClient client;

    public static MongoDatabase getDatabase() {
        if (client == null) {
            Dotenv dotenv = Dotenv.load();
            String connectionString = dotenv.get("MONGO_URI");
            client = MongoClients.create(connectionString);
        }
        return client.getDatabase("blogging_platform");
    }
}