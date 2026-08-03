# Blogging Platform — NoSQL (MongoDB) Implementation

## Overview

This repository contains the NoSQL database design and implementation
for the Smart Blogging Platform project, built on **MongoDB Atlas**.
It models a blogging domain — Users, Posts, Comments, Reviews, and
Tags — using a document-model schema, with deliberate embedding vs.
referencing decisions justified in `database-design.md`.

## Files

| File                     | Purpose                                                        |
|--------------------------|-----------------------------------------------------------------|
| `database-design.md`     | Full schema design document — entity shapes and justifications |
| `schema-scripts.js`      | All MongoDB commands used to build and populate the database   |
| `performance-report.md`  | Before/after indexing performance measurements                 |
| `README.md`              | This file                                                       |

## Tech Stack

- **Database:** MongoDB (Atlas, free tier, `af-south-1` / Cape Town)
- **Shell:** `mongosh` 2.9.2

## Setup Instructions

1. Create a free MongoDB Atlas cluster at
   [mongodb.com/cloud/atlas](https://www.mongodb.com/cloud/atlas/register).
2. Install `mongosh`:
   [mongodb.com/try/download/shell](https://www.mongodb.com/try/download/shell)
3. Add your current IP address under **Network Access** in Atlas
   (required every time your IP changes, e.g. after reconnecting to a
   different network).
4. Connect using the connection string provided by Atlas:
   ```
   mongosh "mongodb+srv://<your-cluster-url>/" --apiVersion 1 --username <your-username> --password <your-password>
   ```
5. Switch to the project database:
   ```javascript
   use blogging_platform
   ```
6. Run the commands in `schema-scripts.js` (in order) to recreate the
   collections, sample data, indexes, and validation rules.

## Security Note

Database credentials are **never committed to this repository**.
Store connection credentials locally (e.g. in a `.env` file, which is
excluded via `.gitignore`) — never hardcode a username/password
directly into application code or scripts pushed to version control.

## Entity Summary

- **Users** — referenced by Posts/Comments/Reviews (own collection)
- **Posts** — references `authorId`; embeds `tags` array
- **Comments** — references `postId` and `authorId` (own collection)
- **Reviews** — references `postId` and `authorId`; enforces
  `rating` (1–5) via `$jsonSchema` validation (own collection)

Full reasoning for each decision is in `database-design.md`.
