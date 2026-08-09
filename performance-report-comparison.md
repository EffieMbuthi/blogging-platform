# Performance Report — MongoDB vs PostgreSQL

## 1. Purpose

This report compares indexed query performance between the two
database implementations built for the Blogging Platform project —
MongoDB (NoSQL) and PostgreSQL (SQL) — and uses the results to
justify the final choice of **PostgreSQL** as the submitted
implementation.

Both tests use an equivalent query shape: find all posts belonging
to a specific author, out of a dataset where that author has exactly
one matching post among ~5,000 total posts. This shape was chosen
deliberately so the majority of rows/documents do *not* match,
making the impact of indexing clearly visible.

## 2. MongoDB (NoSQL) Results

**Query:**
```javascript
db.posts.find({ "authorId": ObjectId("...") }).explain("executionStats")
```

| Metric | Before Index (`COLLSCAN`) | After Index (`FETCH` via index) |
|---|---|---|
| Stage | COLLSCAN | FETCH |
| Documents examined | 5,002 | 1 |
| Documents returned | 1 | 1 |
| Execution time | 3 ms | 0 ms |

**Index created:** `db.posts.createIndex({ "authorId": 1 })`

**Observation:** Without an index, MongoDB examined every single
document in the collection (5,002) regardless of how many actually
matched. With the index, it examined exactly 1 — a reduction of
over 99.9% in documents examined for this query shape.

## 3. PostgreSQL (SQL) Results

**Query:**
```sql
EXPLAIN ANALYZE SELECT * FROM posts WHERE user_id = 2;
```

| Metric | Before Index (`Seq Scan`) | After Index (`Index Scan`) |
|---|---|---|
| Scan type | Seq Scan | Index Scan |
| Rows matched | 1 | 1 |
| Rows unnecessarily checked | 5,004 | 0 |
| Execution time | 1.914 ms | 0.250 ms |

**Index created:** `CREATE INDEX idx_posts_user_id ON posts(user_id);`

**Observation:** The sequential scan checked and discarded 5,004
non-matching rows before finding the 1 match. After indexing,
PostgreSQL used the index to go directly to the matching row,
eliminating all unnecessary row checks and reducing execution time
by approximately 87% (1.914 ms → 0.250 ms).

## 4. Side-by-Side Comparison

| | MongoDB | PostgreSQL |
|---|---|---|
| Unindexed scan type | Collection Scan | Sequential Scan |
| Indexed scan type | Index-backed Fetch | Index Scan |
| Records examined before indexing | 5,002 | 5,005 |
| Records examined after indexing | 1 | 1 |
| Relative improvement | Documents examined reduced to near-zero | ~87% faster execution, 0 wasted row checks |

**Both databases demonstrate the same underlying principle:**
without an index, query cost scales with the size of the entire
collection/table, regardless of how selective the query is. With an
index, cost scales with the number of *matching* results instead —
the central justification for indexing frequently-queried fields in
either database technology.

## 5. Beyond Raw Speed — Why PostgreSQL Was Chosen

Query speed alone does not fully justify the final database choice;
both implementations achieved comparable, strong indexed-query
performance. The deciding factors were structural, based on the
project's data model:

- **Referential integrity.** PostgreSQL rejects an insert referencing
  a non-existent foreign key automatically (tested directly — an
  attempt to insert a comment with an invalid `post_id` was rejected
  with a clear constraint violation error). MongoDB accepted the
  equivalent invalid reference silently, with no built-in protection
  until custom validation was added.
- **Data shape.** Every entity in this domain (Users, Posts, Comments,
  Reviews, Tags) is inherently relational — most relationships
  resolved to standard one-to-many or many-to-one references in both
  implementations. Only Tags benefited meaningfully from MongoDB's
  embedding capability; every other entity ended up structurally
  equivalent to a relational design regardless of which database
  was used.
- **Validation enforcement.** PostgreSQL's `CHECK` constraint on
  `reviews.rating` is enforced at table-creation time, for every
  row, with no separate step. The equivalent MongoDB `$jsonSchema`
  validator had to be added afterward, and a test insert
  (`rating: 999`) was able to violate the rule before validation was
  introduced.
- **Reporting and aggregation.** The project's course content
  emphasizes joins, aggregations, and window functions — features
  SQL is built around natively, and which map less directly onto
  MongoDB's aggregation pipeline for genuinely relational queries.

## 6. Conclusion

Both databases showed measurable, significant performance gains from
indexing frequently-queried fields, following the same underlying
principle (avoid scanning unmatched records). Given comparable
performance, PostgreSQL was selected as the final implementation
based on its enforced referential integrity, native support for the
project's relational data model, and constraint enforcement built
directly into the schema — reducing the amount of validation logic
that must be independently maintained in the application layer.
