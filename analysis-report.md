# Analysis Report — Database Choice & Design Rationale

## 1. Purpose

`performance-report-comparison.md` contains the raw, measured
evidence (query timings, `EXPLAIN`/`explain()` output, cache timings).
This report is the **analysis** built on top of that evidence: it
weighs the measurements against the project's actual data model and
requirements to justify why **PostgreSQL** was selected as the final
submitted implementation over the MongoDB alternative that was also
built and evaluated during development.

## 2. Raw Speed Alone Doesn't Decide It

Section 4 of the performance report shows both databases achieving
comparable, strong indexed-query performance — MongoDB's index
reduced documents examined from 5,002 to 1; PostgreSQL's index
reduced rows checked from 5,004 to 0, cutting execution time by ~87%.
Neither result is decisively better than the other, so query speed
was not the deciding factor. The deciding factors were structural,
based on the project's actual data model:

- **Referential integrity.** PostgreSQL rejects an insert referencing
  a non-existent foreign key automatically (tested directly — an
  attempt to insert a comment with an invalid `post_id` was rejected
  with a clear constraint violation error; see `testing-evidence.md`
  §2). MongoDB accepted the equivalent invalid reference silently,
  with no built-in protection until custom validation was added.
- **Data shape.** Every entity in this domain (Users, Posts, Comments,
  Reviews, Tags) is inherently relational — most relationships
  resolved to standard one-to-many or many-to-one references in both
  implementations. Only Tags benefited meaningfully from MongoDB's
  embedding capability; every other entity ended up structurally
  equivalent to a relational design regardless of which database
  was used.
- **Validation enforcement.** PostgreSQL's `CHECK` constraint on
  `reviews.rating` is enforced at table-creation time, for every
  row, with no separate step (see `testing-evidence.md` §3). The
  equivalent MongoDB `$jsonSchema` validator had to be added
  afterward, and a test insert (`rating: 999`) was able to violate the
  rule before validation was introduced.
- **Reporting and aggregation.** The project's course content
  emphasizes joins, aggregations, and window functions — features
  SQL is built around natively. The Analytics tab's Post Engagement
  report (`ReportDAO.findPostEngagement`) is a direct demonstration:
  one query joins four tables (`posts`, `users`, `comments`,
  `reviews`, `post_tags`) and aggregates with `COUNT`/`AVG`/
  `GROUP BY` — the kind of query that maps less directly onto
  MongoDB's aggregation pipeline for genuinely relational data.

## 3. What the Broader Timing Data Implies

The performance report's §5 measured results show every non-cached
operation (comments lookup, tag search, the engagement report) in the
triple-digit milliseconds, dominated by `PostgresConnection` opening a
brand-new JDBC connection on every DAO call. This is a real,
measured finding, not a flaw specific to PostgreSQL — the same
per-call connection cost would apply to any JDBC-based application
without pooling. The analysis: if this project continued past this
submission, introducing a connection pool (e.g. HikariCP) is the next
concrete, evidence-backed optimization — it would not change *what*
is indexed or cached, only remove the repeated per-call connection
overhead sitting on top of already-fast, already-indexed queries.

## 4. Conclusion

Both databases showed measurable, significant performance gains from
indexing frequently-queried fields, following the same underlying
principle (avoid scanning unmatched records) — neither database "won"
on raw speed. PostgreSQL was selected as the final implementation
based on its enforced referential integrity, native support for the
project's relational data model, and constraint enforcement built
directly into the schema — reducing the amount of validation logic
that must be independently maintained in the application layer, and
making the multi-table aggregation the Analytics tab depends on a
natural fit rather than a workaround.
