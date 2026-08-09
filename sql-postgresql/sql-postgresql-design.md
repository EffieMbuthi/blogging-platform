# Database Design Document — Blogging Platform (SQL / PostgreSQL)

## 1. Overview

This document covers the relational (PostgreSQL) database design for
the Smart Blogging Platform, submitted as the final database
implementation for this project (see `performance-report-comparison.md`
for the reasoning behind choosing PostgreSQL over the MongoDB
alternative that was also built and evaluated during development).

The design follows the three-stage modeling process required by the
project brief: a **conceptual model** (entities and relationships),
a **logical model** (attributes, keys, cardinality), and a
**physical model** (actual SQL types and constraints, normalized to
3NF).

## 2. Conceptual Model

Five core entities were identified: **Users, Posts, Comments,
Reviews, Tags**. Their relationships:

- **Users → Posts**: one-to-many (one user authors many posts)
- **Posts → Comments**: one-to-many (one post has many comments);
  **Comments → Users**: many-to-one (many comments can be written
  by the same user)
- **Posts → Reviews**: one-to-many; **Reviews → Users**: many-to-one
- **Posts ↔ Tags**: many-to-many (one post can have several tags;
  one tag can apply to many posts)

The many-to-many Posts↔Tags relationship requires a **junction
table** (`post_tags`), since a single foreign key column cannot
represent a relationship where both sides can have multiple
matches.

## 3. Logical Model

| Entity | Primary Key | Foreign Keys | Key Attributes |
|---|---|---|---|
| users | id | — | name, email (unique) |
| posts | id | user_id → users(id) | title, body |
| comments | id | post_id → posts(id), user_id → users(id) | body |
| reviews | id | post_id → posts(id), user_id → users(id) | rating |
| tags | id | — | name (unique) |
| post_tags | (post_id, tag_id) *composite* | post_id → posts(id), tag_id → tags(id) | — |

`post_tags` uses a **composite primary key** — the pair
`(post_id, tag_id)` together, rather than a separate auto-incrementing
`id` — because the meaningful uniqueness constraint is "this
post/tag pairing has not already been recorded," not an arbitrary
row number. This was verified directly: attempting to insert the
same `(post_id, tag_id)` pair twice is rejected by the primary key
constraint.

## 4. Physical Model (3NF)

All tables satisfy Third Normal Form:
- Every column holds a single, atomic value (1NF) — this is
  precisely why `tags` could not be a comma-separated list on
  `posts`; a proper `tags` table plus `post_tags` junction table
  was required instead.
- Composite-key tables (`post_tags`) have no non-key attributes,
  avoiding partial-key dependency issues (2NF).
- No column depends on a non-key column of the same table (3NF) —
  for example, an author's email is never duplicated onto `posts`;
  it is always resolved through the `user_id` foreign key.

Full `CREATE TABLE` statements, including constraints, are in
`sql-postgresql/schema.sql`.

### Key constraints applied

- **`NOT NULL`** on required fields (title, body, rating, etc.) —
  enforced at the database level, in addition to Service-layer
  validation in the Java application (defense in depth: the
  application should reject invalid input first, for good user
  feedback, but the database itself never accepts it either way).
- **`UNIQUE`** on `users.email` and `tags.name` — prevents duplicate
  registrations and duplicate tag names.
- **`CHECK (rating >= 1 AND rating <= 5)`** on `reviews.rating` —
  the direct SQL equivalent of the `$jsonSchema` validator used on
  the MongoDB implementation's `reviews` collection. Verified by
  attempting to insert `rating = 999`, which PostgreSQL rejected
  immediately with a constraint violation error.
- **Foreign keys** on every reference column — verified by attempting
  to insert a `comments` row with a `post_id` that does not exist,
  which PostgreSQL rejected automatically. This is a structural
  advantage over the MongoDB implementation, where an equivalent
  invalid reference is accepted silently by default.

## 5. Indexing (User Story 1.2)

Indexes were added on the columns the application queries or
filters by frequently:

```sql
CREATE INDEX idx_posts_user_id ON posts(user_id);
CREATE INDEX idx_posts_title ON posts(title);
CREATE INDEX idx_comments_post_id ON comments(post_id);
CREATE INDEX idx_reviews_post_id ON reviews(post_id);
CREATE INDEX idx_tags_name ON tags(name);
```

Primary key columns (`id` on every table) are indexed automatically
by PostgreSQL and required no explicit action.

Measured impact of indexing `posts.user_id`: a query for a single
author's post, out of ~5,000 posts, went from a full sequential scan
(5,004 rows unnecessarily checked, 1.914 ms) to an index scan (0 rows
unnecessarily checked, 0.250 ms). Full results in
`performance-report-comparison.md`.

## 6. Application-Layer Architecture

The Java application follows a layered architecture, matching the
Technical Requirements (Controller → Service → DAO):

- **`connection/PostgresConnection.java`** — opens a JDBC connection
  using credentials loaded from a `.env` file (never hardcoded or
  committed to version control).
- **`dao/`** — one DAO class per entity (`PostDAO`, `CommentDAO`,
  `ReviewDAO`, `TagDAO`, `UserDAO`), each responsible only for
  translating method calls into parameterized SQL (`PreparedStatement`)
  and mapping `ResultSet` rows into plain model objects. DAOs contain
  no business rules.
- **`service/`** — one Service class per entity, responsible for
  validation (e.g. rejecting blank titles, rejecting ratings outside
  1–5) before delegating to the DAO. `PostService` additionally
  implements in-memory caching (see Section 7).
- **`model/`** — plain Java objects (`Post`, `Comment`, `Review`,
  `Tag`, `User`) representing one row each, decoupled from any
  database-specific type (e.g. `ResultSet` is fully consumed inside
  the DAO and never exposed to callers).
- **`BlogApp.java`** — the JavaFX Controller layer: builds the UI,
  handles user actions, and calls Service methods. Contains no
  direct SQL or JDBC code.

## 7. In-Memory Caching (User Story 3.2)

`PostService` caches the result of `getAllPosts()` in a
`List<Post>` field. Cache invalidation is **fine-grained** rather
than a full cache clear on every write:

- **Create**: the new post's generated id is retrieved directly from
  PostgreSQL (via `Statement.RETURN_GENERATED_KEYS`), and the new
  `Post` object is added directly into the cache — no re-fetch
  required.
- **Update**: the matching cached `Post` object's fields are updated
  in place.
- **Delete**: the matching cached `Post` object is removed from the
  cache directly (`List.removeIf`).

This guarantees the cache is never stale after a write performed by
the application itself, while avoiding an unnecessary full re-fetch
from the database on every change.

## 8. Known Scope Decisions

- Caching was applied only to `Post` reads, matching the specific
  wording of User Story 3.2. The same pattern is directly extensible
  to other entities if required.
- The JavaFX UI implements full CRUD, search (case-insensitive,
  live-filtering on title), and pagination for Posts. Comments,
  Reviews, and Tags have complete Model/DAO/Service coverage but are
  not yet wired into a dedicated screen — this was a deliberate time
  allocation decision given the entity CRUD pattern is already fully
  demonstrated and directly transferable.
