# Testing Evidence — Blogging Platform (PostgreSQL Implementation)

## 1. Purpose

This document collects the manual tests performed during development
to verify the correctness of the database schema, constraints, and
application logic, as required by the "Testing Evidence" deliverable.

## 2. Referential Integrity (Foreign Keys)

**Test:** Insert a comment referencing a `post_id` that does not exist.

```sql
INSERT INTO comments (post_id, user_id, body) VALUES (999, 1, 'This should fail');
```

**Result:** Rejected.
```
ERROR: insert or update on table "comments" violates foreign key
constraint "comments_post_id_fkey"
Detail: Key (post_id)=(999) is not present in table "posts".
```

**Conclusion:** Foreign key constraints are enforced automatically by
PostgreSQL, with no additional application-level code required. This
was compared directly against the MongoDB implementation, where an
equivalent invalid reference (a non-existent `authorId`) was accepted
silently with no error — a structural advantage of the relational
implementation.

## 3. CHECK Constraint (Rating Range)

**Test:** Insert a review with a rating outside the valid 1–5 range.

```sql
INSERT INTO reviews (post_id, user_id, rating) VALUES (1, 1, 999);
```

**Result:** Rejected.
```
ERROR: new row for relation "reviews" violates check constraint
"reviews_rating_check"
```

**Test (valid case):** Insert a review with `rating = 5`.
**Result:** Accepted successfully.

**Conclusion:** The `CHECK` constraint correctly enforces the valid
rating range at the database level. This mirrors a test performed on
the MongoDB implementation, where an identical invalid value
(`rating: 999`) was initially accepted with no validation, then
correctly rejected after a `$jsonSchema` validator was added — the
PostgreSQL implementation enforces this rule from the moment the
table is created, with no separate step required.

## 4. Composite Primary Key (post_tags)

**Test:** Insert the same `(post_id, tag_id)` pairing twice.

**Result:** The second insert is rejected with a primary key
violation, confirming the same post cannot be tagged with the same
tag more than once.

## 5. Indexing Performance

**Test:** Compare `EXPLAIN ANALYZE` output for a query filtering
`posts` by `user_id`, before and after creating an index, against a
dataset of ~5,000 posts where only one matches the filter.

| Metric | Before Index | After Index |
|---|---|---|
| Scan type | Seq Scan | Index Scan |
| Rows unnecessarily checked | 5,004 | 0 |
| Execution time | 1.914 ms | 0.250 ms |

**Conclusion:** Indexing eliminated all unnecessary row scans for
this query shape. Full details and the equivalent MongoDB comparison
are in `performance-report-comparison.md`.

## 6. Application-Layer Validation

**Test:** Attempt to create a post through the JavaFX UI with an
empty title.

**Result:** `PostService.createPost` throws
`IllegalArgumentException("Title cannot be empty.")`, caught by
`BlogApp` and displayed in the status label as
`"Error: Title cannot be empty."` — no exception propagates to crash
the application, and no invalid row is inserted.

**Test:** Repeat with an empty body.
**Result:** Same behavior, with the message
`"Body cannot be empty."`.

## 7. Cache Correctness (In-Memory Caching)

**Test:** Create a new post through the UI, then immediately view the
post list without triggering a fresh database read.

**Result:** The new post appears immediately in the list. Verified
that `PostDAO.createPost` returns the newly generated `id` (via
`Statement.RETURN_GENERATED_KEYS`), used to construct a `Post` object
added directly into `PostService`'s cache — confirming the cache was
updated in place rather than requiring a full re-fetch.

**Test:** Update a post's title through the UI, then reopen the post
list.

**Result:** The updated title appears correctly, confirming the
cached `Post` object's field was updated in place
(`post.setTitle(...)`), not left stale.

**Test:** Delete a post through the UI, then reopen the post list.

**Result:** The deleted post no longer appears, confirming
`cachedPosts.removeIf(...)` correctly removed the specific entry
without requiring a full cache clear.

## 8. Search Functionality

**Test:** Search posts by title keyword (partial, case-insensitive),
e.g. searching `"vot"` while a post titled `"Voting"` exists.

**Result:** Matching post returned correctly, confirming the
`ILIKE '%keyword%'` pattern match works as intended, both via the
Search button and via live-filtering as text is typed.

**Test:** Search posts by author name, e.g. `"Effie"`.

**Result:** Only posts authored by that user are returned, confirming
the `JOIN posts ON users` query correctly filters by the joined
table's column.

**Test:** Search posts by tag name, e.g. `"politics"`.

**Result:** Only posts linked to that tag (via `post_tags`) are
returned, confirming the two-hop join (`posts → post_tags → tags`)
resolves correctly.

## 9. Pagination

**Test:** Load the application with more than 20 posts in the
database, then click "Next" and "Previous".

**Result:** Each page displays exactly `PAGE_SIZE` (20) posts, using
the calculated `OFFSET = (pageNumber - 1) * pageSize`. Clicking
"Previous" from page 1 does not attempt to go below page 1
(guarded by `if (currentPage > 1)`).

## 10. Comments

**Test:** Select a post in the list, add a new comment through the
UI.

**Result:** The comment is persisted (verified via
`CommentDAO.createComment`, using `RETURN_GENERATED_KEYS` the same
way as posts) and appears immediately in the comment list for that
post, without requiring an application restart.
