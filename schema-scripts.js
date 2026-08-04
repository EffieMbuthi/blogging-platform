/**
 * Blogging Platform - MongoDB (NoSQL) Implementation Script
 * Database: blogging_platform
 * Cluster: MongoDB Atlas (Cluster0, af-south-1 / Cape Town)
 *
 * This script documents the schema creation, sample data, indexes,
 * and validation rules for the NoSQL implementation of the
 * Blogging Platform project.
 *
 * Run these commands in mongosh, connected to your Atlas cluster:
 *   use blogging_platform
 */

// ============================================================
// 1. USERS COLLECTION
// ============================================================
// Design decision: Users are REFERENCED by Posts/Comments/Reviews,
// never embedded, because user data (name, email) is queried
// independently of any single post, and one user relates to
// MANY posts/comments/reviews (unbounded growth on the "many" side).

db.users.insertOne({ "name": "Effie", "email": "effie@gmail.com" });
db.users.insertOne({ "name": "James", "email": "james@gmail.com" });

// ============================================================
// 2. POSTS COLLECTION
// ============================================================
// Design decisions:
// - authorId: REFERENCE to users._id (ObjectId type, not string).
//   Many posts -> one user, and user data is queried independently.
// - tags: EMBEDDED array of strings. Tags are small, bounded (a few
//   words per post), and always read together with the post itself
//   (used for description + filtering). No independent query need
//   that would justify a separate collection.

db.posts.insertOne({
  "authorId": ObjectId("6a6dcb285515732ba4b4c7ac"), // Effie's _id
  "title": "Voting",
  "body": "women voting....."
});

db.posts.insertOne({
  "authorId": ObjectId("6a6dcb285515732ba4b4c7ac"),
  "title": "AI influence",
  "body": "The disruption....."
});

// Embedding tags directly into an existing post document
db.posts.updateOne(
  { "title": "Voting" },
  { $set: { "tags": ["politics", "history", "rights"] } }
);

// ============================================================
// 3. COMMENTS COLLECTION
// ============================================================
// Design decision: REFERENCED, not embedded. A post can receive an
// UNBOUNDED number of comments over its lifetime. Embedding would
// risk hitting MongoDB's 16MB document size limit and would force
// every read of a post to drag along every comment ever written on
// it, even when only the post itself is needed.
// Comments and posts are usually displayed TOGETHER on screen, but
// "displayed together" does not require "stored together" - this
// is handled at query time via $lookup instead.

db.comments.insertOne({
  "postId": ObjectId("6a6dc89a5515732ba4b4c7ab"),   // "Voting" post
  "authorId": ObjectId("6a6f03fa01f47eb961425e1f"),  // James
  "body": "This is bullshit....."
});

// ============================================================
// 4. REVIEWS COLLECTION
// ============================================================
// Design decision: REFERENCED, same unbounded-growth reasoning as
// Comments. Distinguishing field vs. Comments: "rating" (a bounded
// numeric evaluation), which Comments do not have.
//
// SCHEMA VALIDATION: MongoDB does not enforce structure by default
// (schema-on-read). To prevent invalid data (e.g. rating: 999),
// a $jsonSchema validator is applied to enforce type + range rules,
// similar in spirit to a SQL CHECK constraint.

db.runCommand({
  collMod: "reviews",
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["postId", "authorId", "rating"],
      properties: {
        rating: {
          bsonType: "int",
          minimum: 1,
          maximum: 5,
          description: "rating must be a whole number between 1 and 5"
        }
      }
    }
  }
});

// Valid review (passes validation)
db.reviews.insertOne({
  "postId": ObjectId("6a6dc89a5515732ba4b4c7ab"),
  "authorId": ObjectId("6a6f03fa01f47eb961425e1f"),
  "rating": 5
});

// Example of an invalid insert that the validator correctly REJECTS
// (kept here as documented proof the validator works - do not run
// expecting success):
//
// db.reviews.insertOne({
//   "postId": ObjectId("6a6dc89a5515732ba4b4c7ab"),
//   "authorId": ObjectId("6a6f03fa01f47eb961425e1f"),
//   "rating": 999
// });
// --> MongoServerError: Document failed validation (maximum: 5 violated)

// ============================================================
// 5. INDEXING (User Story 1.2)
// ============================================================
// authorId is indexed because posts are frequently queried/filtered
// by author (e.g. "show all posts by this author").
// Ascending (1) direction chosen; for an exact-match-only field the
// direction has little practical effect, but MongoDB requires one
// to be specified.

db.posts.createIndex({ "authorId": 1 });

// title is indexed to support keyword search (User Story 3.1)
db.posts.createIndex({ "title": 1 });

// ============================================================
// 6. JOIN QUERIES ($lookup) - used to reconnect referenced data
// ============================================================

// Posts -> Users (get author info attached to each post)
db.posts.aggregate([
  {
    $lookup: {
      from: "users",
      localField: "authorId",
      foreignField: "_id",
      as: "authorInfo"
    }
  }
]);

// Posts -> Comments (get comments attached to each post)
// Note: this is a LEFT OUTER JOIN - every post is returned, even
// posts with zero comments (empty array), which matches expected
// real-world blog behavior (a new post shows "0 comments", it is
// not hidden).
db.posts.aggregate([
  {
    $lookup: {
      from: "comments",
      localField: "_id",
      foreignField: "postId",
      as: "comments"
    }
  }
]);

// ============================================================
// 7. SEARCH (User Story 3.1) - case-insensitive keyword search
// ============================================================
// $regex with the "i" option matches regardless of letter case,
// e.g. searching "voting" (lowercase) still matches "Voting".

db.posts.find({ "title": { $regex: "voting", $options: "i" } });

// ============================================================
// 8. SORTING - newest posts first
// ============================================================
// Requires a createdAt field on each post. -1 = descending
// (largest/most recent timestamp first).

db.posts.updateOne({ "title": "Voting" }, { $set: { "createdAt": new Date() } });
db.posts.updateOne({ "title": "AI influence" }, { $set: { "createdAt": new Date() } });

db.posts.find().sort({ "createdAt": -1 });

// NOTE: Filler posts generated earlier in this script do not have
// a createdAt field, since it was added only to two posts as a
// demonstration. In production, createdAt should be set on every
// post at insert time (not patched on afterward) to avoid
// inconsistent sort behavior for documents missing the field.

// ============================================================
// 9. PERFORMANCE VERIFICATION (see performance-report.md)
// ============================================================
// db.posts.find({ "authorId": ObjectId("...") }).explain("executionStats")
// Before index -> stage: COLLSCAN, totalDocsExamined: 5002
// After index  -> stage: FETCH,    totalDocsExamined: 1 (for a query
//                 matching a single distinct author)
