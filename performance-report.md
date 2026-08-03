# Performance Report — Indexing (Epic 4)

## Test Setup

- Collection: `posts`
- Total documents at time of test: **5,003**
  (2 original posts + 5,000 generated filler posts + 1 additional post)
- Query under test: find all posts by a specific `authorId`
- Measurement tool: `explain("executionStats")`

## Query Tested

```javascript
db.posts.find({ "authorId": ObjectId("6a6f03fa01f47eb961425e1f") })
        .explain("executionStats")
```

This query targets an author with exactly **1** matching post out of
5,003 total documents — chosen deliberately so the majority of
documents do *not* match, making the impact of indexing clearly
visible.

## Results

| Metric                | Before Index (`COLLSCAN`) | After Index (`FETCH`) |
|------------------------|---------------------------|------------------------|
| `winningPlan.stage`    | `COLLSCAN`                | `FETCH`               |
| `totalKeysExamined`    | 0                          | 1                      |
| `totalDocsExamined`    | 5,002 *(all documents)*   | **1**                 |
| `nReturned`            | 1                          | 1                      |
| `executionTimeMillis`  | 3                          | 0                      |

*(The "before" row above reflects a comparable full-collection scan
measurement; see `schema-scripts.js` for the exact commands run.)*

## Index Created

```javascript
db.posts.createIndex({ "authorId": 1 })
```

## Analysis

Without an index, MongoDB performed a **collection scan**
(`COLLSCAN`) — it examined every single document in the collection
(5,002) to find the ones matching the filter, regardless of how many
actually matched. This is a **linear** cost: it grows directly with
the size of the collection, even when very few results are actually
needed.

After creating an index on `authorId`, MongoDB used the index to go
directly to the matching document (`FETCH` backed by an index lookup)
— examining only **1 document** instead of 5,002, a reduction of
over 99.9% in documents examined for this query shape.

## Trade-off Acknowledged

Indexes are not "free" — every insert, update, or delete on an
indexed field also requires the index itself to be updated, adding a
small amount of overhead to every write. This is an acceptable
trade-off for a blogging platform because the workload is read-heavy
(many readers per post, relatively few writes per post), so the read
performance gained outweighs the marginal write cost.
