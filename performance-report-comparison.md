# Performance Report — MongoDB vs PostgreSQL

## 1. Purpose

This report presents the raw, measured query-performance evidence
collected from both database implementations built for the Blogging
Platform project — MongoDB (NoSQL) and PostgreSQL (SQL). It contains
**measurements only**; for the reasoning that turns these numbers
(plus structural/design factors) into a final database choice, see
`analysis-report.md`.

**Note on the test dataset:** the ~5,000-row comparison in Sections 2
and 3 used a temporary bulk-generated dataset (a standard technique
for making an index's effect visible at scale), not the small sample
data committed in `sql-postgresql/schema.sql` (which is intentionally
kept minimal for demoing CRUD, not for performance testing). No
fabricated numbers are reported here — every figure below is a
directly captured `explain()`/`EXPLAIN ANALYZE` result or a printed
`System.nanoTime()` measurement.

Both index tests use an equivalent query shape: find all posts
belonging to a specific author, out of a dataset where that author has
exactly one matching post among ~5,000 total posts. This shape was
chosen deliberately so the majority of rows/documents do *not* match,
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

## 5. Optimization Coverage Beyond Posts

The indexing evidence above focuses on `posts.user_id` specifically,
since it gives the clearest before/after contrast. The optimization
work is not limited to that one table — see
`sql-postgresql/sql-postgresql-design.md` (§5, "Indexing coverage
across all tables") for the full index list, covering `comments`,
`reviews`, `tags`, and `post_tags` as well.

`sql-postgresql-java/src/main/java/org/example/CachePerformanceTest.java`
times four operations, not just the Post cache:

| Operation | What it measures |
|---|---|
| `PostService.getAllPosts()` (1st vs 2nd call) | In-memory cache: DB read vs cache hit |
| `CommentDAO.findCommentsByPostId(1)` | `idx_comments_post_id` — used every time a post is selected in the UI |
| `PostDAO.searchPostsByTag("politics")` | `idx_tags_name`, plus the `posts → post_tags → tags` two-hop join |
| `ReportService.getPostEngagementReport()` | The Analytics tab's 4-table `JOIN` + `GROUP BY` + `COUNT`/`AVG` aggregate query |

**Measured results** (run against the seeded local database, IntelliJ/Maven, 2026-08-19):

| Operation | Time (ms) |
|---|---|
| Posts — cache miss | 1637.2996 |
| Posts — cache hit | 0.0016 |
| Comments by post_id | 140.5008 |
| Posts by tag | 239.8389 |
| Post Engagement report | 391.0850 |

**Cache effect:** cache hit vs. cache miss is a ~1,000,000x
difference (1637 ms → 0.0016 ms) — once `PostService` has the post
list in memory, a repeat read costs nothing, exactly as designed in
Section 8 of `sql-postgresql-design.md`.

**Why the other rows aren't "fast and constant" in absolute terms:**
`PostgresConnection.getConnection()` opens a brand-new JDBC connection
on every single DAO call (no pooling) — so each row above is paying
full connection-establishment cost (TCP handshake + auth + driver
initialization), not just query execution time. That overhead
explains the very first row's outsized 1637 ms (also absorbing JVM/
class-loading startup) and the fact that indexed lookups still show
triple-digit milliseconds. The comparison that isolates the *indexing*
effect specifically is Section 3 above (`EXPLAIN ANALYZE`, same
connection, before/after index: 1.914 ms → 0.250 ms) — that's the
number that isolates "indexing works." This run's rows demonstrate a
different, equally real finding: connection-per-call is the dominant
cost in this application today. See `analysis-report.md` for what
that implies going forward.
