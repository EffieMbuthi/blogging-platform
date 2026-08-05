package org.example;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.types.ObjectId;

public class PostDAO {
    //Java object representing one Collection and has methods in it like [insertOne, find, updateOne]
    private MongoCollection<Document> postsCollection;

    public PostDAO(){
        MongoDatabase database= MongoConnection.getDatabase(); // use blogging_platform
        this.postsCollection= database.getCollection("posts"); //db.posts
        // give me posts collection as a Java Object so i can call methods on
    }

    // .insertOne()
    public void createPost(ObjectId authorId, String title, String body){
        //key and value(json format)
        Document newPost= new Document()
                .append("authorId", authorId)
                .append("title", title)
                .append("body", body);

        postsCollection.insertOne(newPost);
        System.out.println("Post inserted with id: " + newPost.get("_id"));
    }

    //.find()
    public FindIterable<Document> findAllPosts(){
        return postsCollection.find();
    }

    // Filter = "WHERE" --> has a Filters class with methods mirroring SQL comparison operators
    // — eq, gt, lt, regex for pattern matching,
    public Document findPostById(ObjectId id){
        return postsCollection.find(Filters.eq("_id", id)).first();
    }
}
