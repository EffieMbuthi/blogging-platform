# Blogging Platform — Database & Application Layer

## Overview

This repository contains the database design and Java application
layer for the Smart Blogging Platform project. Two full database
implementations were built and evaluated — **MongoDB (NoSQL)** and
**PostgreSQL (SQL)** — with **PostgreSQL selected as the final
submitted implementation**. See `performance-report-comparison.md`
for the measured performance comparison and reasoning behind that
choice.

Both implementations model the same blogging domain — Users, Posts,
Comments, Reviews, and Tags — through a layered Controller → Service
→ DAO architecture, built with JavaFX and Java.

## Repository Structure

```
blogging-platform/
├── nosql-mongodb/              # MongoDB schema design, scripts, docs
├── nosql-mongodb-java/         # MongoDB Java application (evaluated alternative)
├── sql-postgresql/             # PostgreSQL schema script and design document
├── sql-postgresql-java/        # PostgreSQL Java application (FINAL SUBMISSION)
├── performance-report-comparison.md   # MongoDB vs PostgreSQL comparison
└── README.md                   # This file
```

## Final Submission: PostgreSQL

The **`sql-postgresql-java/`** project is the complete, final
application:

- **`sql-postgresql/schema.sql`** — full table definitions,
  constraints, indexes, and sample data
- **`sql-postgresql/sql-postgresql-design.md`** — conceptual, logical,
  and physical database design, with justifications
- **`sql-postgresql-java/`** — the JavaFX application, connected via
  JDBC

### Features implemented

- Full CRUD for Posts through the JavaFX interface (create, read,
  update, delete)
- Case-insensitive search on post titles (button-triggered and
  live-filtering as you type)
- Pagination (20 posts per page, Next/Previous navigation)
- In-memory caching of post reads, with fine-grained cache
  invalidation on create/update/delete
- Full Model/DAO/Service layers for Users, Comments, Reviews, and
  Tags (CRUD available; not yet wired into a dedicated UI screen)
- Referential integrity and `CHECK` constraints enforced at the
  database level, verified through deliberate failure testing

## MongoDB Implementation (Evaluated Alternative)

The **`nosql-mongodb/`** and **`nosql-mongodb-java/`** folders contain
a complete, working NoSQL implementation of the same domain, built
first during development to compare against the relational approach.
It includes schema validation (`$jsonSchema`), indexing, a full
JavaFX application with CRUD, and its own performance report. It is
retained as evidence for the database comparison, not as the final
submission.

## Tech Stack

**PostgreSQL implementation (final):**
- PostgreSQL 17, managed via pgAdmin
- Java 17, JDBC (`org.postgresql:postgresql` driver)
- JavaFX 21
- Maven

**MongoDB implementation (comparison):**
- MongoDB Atlas (free tier)
- MongoDB Java Driver (`mongodb-driver-sync`)
- JavaFX 21

Both projects use `dotenv-java` to load credentials from a local
`.env` file, which is excluded from version control.

## Setup Instructions — PostgreSQL (Final Submission)

1. Install PostgreSQL and pgAdmin.
2. Create a database named `blogging_platform`.
3. Open pgAdmin's Query Tool and run `sql-postgresql/schema.sql` to
   create all tables, constraints, indexes, and sample data.
4. Open `sql-postgresql-java/` in IntelliJ (or your preferred IDE).
5. Create a `.env` file in the project root:
   ```
   POSTGRES_URL=jdbc:postgresql://localhost:5432/blogging_platform
   POSTGRES_USER=your_postgres_username
   POSTGRES_PASSWORD=your_postgres_password
   ```
6. Run the application via the Maven `javafx:run` goal (Maven tool
   window → Plugins → javafx → `javafx:run`), since JavaFX requires
   this plugin to launch correctly with the required runtime modules.

## Setup Instructions — MongoDB (Comparison Reference)

See `nosql-mongodb/README.md` and `nosql-mongodb-java/` for full
setup instructions for the MongoDB implementation.

## Security Note

Database credentials are **never committed to this repository** in
either implementation. Credentials are stored locally in `.env`
files, excluded via `.gitignore` — never hardcoded into application
code or scripts pushed to version control.

## Documentation Index

| Document | Covers |
|---|---|
| `performance-report-comparison.md` | MongoDB vs PostgreSQL performance comparison and final choice justification |
| `sql-postgresql/sql-postgresql-design.md` | PostgreSQL schema design (final submission) |
| `nosql-mongodb/database-design.md` | MongoDB schema design (comparison reference) |
| `nosql-mongodb/performance-report.md` | MongoDB-only indexing performance report |
