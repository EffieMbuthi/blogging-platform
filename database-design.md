# Database Design Document — Blogging Platform (NoSQL / MongoDB)

## 1. Overview

This document covers the NoSQL (MongoDB) database design for the Smart
Blogging Platform. It follows a document-model approach and explains,
for each entity, why data was **embedded** or **referenced**, based on
two questions:

1. **Bounded vs. unbounded growth** — does this data stay small and
   predictable, or can it grow without limit?
2. **Co-read vs. independent query** — is this data only ever needed
   alongside its parent, or might the application need to query it
   on its own?

MongoDB is **schema-on-read**: it does not enforce document structure
at write time the way a relational database enforces column
definitions. This gives flexibility but shifts responsibility for
data correctness onto the application layer (and, where used here,
onto explicit `$jsonSchema` validation rules).

## 2. Entities and Design Decisions

### 2.1 Users (own collection)

```json
{
  "_id": "ObjectId",
  "name": "string",
  "email": "string"
}
```

Users are never embedded into any other document. A single user can
be the author of an unbounded number of posts, comments, and reviews.
Embedding a user's data into every post/comment/review they create
would duplicate that data endlessly and make simple updates (e.g. a
name change) require rewriting every document that referenced it.
User data is also frequently queried on its own (e.g. login, profile
lookup), independent of any specific post — a further reason to keep
it in its own collection.

### 2.2 Posts (own collection, with one embedded field)

```json
{
  "_id": "ObjectId",
  "authorId": "ObjectId (reference -> users._id)",
  "title": "string",
  "body": "string",
  "tags": ["string", "..."]
}
```

- **`authorId` — REFERENCED.** Many posts point to one user. Author
  data is queried independently of posts (e.g. an admin editing a
  user's profile), so duplicating it into every post would create a
  data-consistency problem: every post would need to be updated if
  the author's details changed.
- **`tags` — EMBEDDED.** Tags are small (a handful of short strings),
  bounded in size, and are always needed alongside the post itself —
  both for display (describing what the post is about) and for
  filtering/search (User Story 3.1: search by keyword, tag, or
  author). There is no independent-query need for tags that would
  justify a separate collection, and no risk of unbounded growth.

### 2.3 Comments (own collection)

```json
{
  "_id": "ObjectId",
  "postId": "ObjectId (reference -> posts._id)",
  "authorId": "ObjectId (reference -> users._id)",
  "body": "string"
}
```

Comments are **referenced**, not embedded, because a single post can
receive an unbounded number of comments over its lifetime — from zero
to potentially tens of thousands. Embedding comments inside the post
document risks:

- Approaching MongoDB's 16MB per-document size limit on popular posts.
- Forcing every read of a post (even just to show its title) to load
  every comment ever written on it.

Although comments are almost always **displayed** alongside their
post, "displayed together" does not require "stored together." The
application retrieves posts and comments as two related queries (or
a single `$lookup` aggregation), giving the appearance of unified data
to the user while keeping storage scalable underneath.

### 2.4 Reviews (own collection)

```json
{
  "_id": "ObjectId",
  "postId": "ObjectId (reference -> posts._id)",
  "authorId": "ObjectId (reference -> users._id)",
  "rating": "integer (1-5)"
}
```

Same unbounded-growth reasoning as Comments applies — any number of
users could review the same post. The distinguishing field from a
Comment is `rating`, a bounded numeric evaluation (whole number,
1–5).

**Schema validation.** Since MongoDB does not enforce field types or
ranges by default, a `$jsonSchema` validator was applied to the
`reviews` collection:

- `postId`, `authorId`, and `rating` are `required`.
- `rating` must be a `bsonType: "int"` between `minimum: 1` and
  `maximum: 5`.

This was deliberately tested: an insert with `rating: 999` was
attempted before validation existed (succeeded — proving the risk of
schema-on-read with no safeguards) and again after validation was
added (rejected with a clear `$jsonSchema` validation error). This is
the closest MongoDB equivalent to a SQL `CHECK` constraint.

### 2.5 Tags (embedded, not a separate collection)

Tags are implemented as an embedded array of strings directly on each
`posts` document (see 2.2), not as their own collection. This was a
deliberate decision, not an oversight: unlike Comments/Reviews, tags
do not grow unboundedly per post (a post realistically has a handful
of tags) and are always needed together with the post they describe.

## 3. Indexing (User Story 1.2)

An index was created on `posts.authorId`:

```javascript
db.posts.createIndex({ "authorId": 1 })
```

**Justification:** Posts are read/filtered by author frequently
(e.g. "show all posts by this author"), and a blogging platform is a
read-heavy system (many readers per post, one author writing it).
Indexes trade a small write-time cost (every insert/update must also
update the index) for a large read-time benefit — a trade-off that
favors read-heavy systems like this one.

See `performance-report.md` for measured before/after results using
`explain("executionStats")`.

## 4. Known Trade-offs / Limitations

- MongoDB does not enforce referential integrity by default — e.g.
  nothing prevents a `posts.authorId` from pointing to a user that no
  longer exists, the way a SQL `FOREIGN KEY` constraint would. This is
  managed at the application layer instead (validated before insert).
- `$lookup` performs a **left outer join** by default — every
  document in the base collection is returned even when no match
  exists in the joined collection (e.g. a post with zero comments
  still appears, with an empty `comments` array). This matches the
  expected real-world behavior for this application (a new post
  should be visible immediately, with "0 comments," not hidden).
