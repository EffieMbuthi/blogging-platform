package org.example;

import org.bson.types.ObjectId;

public class Main {
    public static void main(String[] args) {
        PostDAO postDAO= new PostDAO();
        ObjectId effieId= new ObjectId("6a6dcb285515732ba4b4c7ac");
        postDAO.createPost(effieId, "My First Java Post","Created through the DAO layer, not the shell");
    }
}
