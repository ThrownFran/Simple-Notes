---
name: room-migration
description: Checklist for bumping the Room database version in Simple Notes (AppDatabase in data/database/) — writing the Migration, updating the version number, and committing the exported schema JSON. Use whenever entities/DAOs change in a way that requires a schema migration, or the user asks to "add a Room migration" or "bump the DB version".
---

Simple Notes exports Room schemas to `app/schemas/` (configured via `room.schemaLocation` in `app/build.gradle`), and that directory is tracked in git — schema JSON files must be committed alongside version bumps. This has drifted before: the DB is on version 10 but the last committed schema was for an old package name at version 6.

When bumping the Room database version:

1. Make the entity/DAO changes.
2. In `app/src/main/java/brillembourg/notes/simple/data/database/AppDatabase.kt`, increment the `version` in the `@Database` annotation.
3. Write a `Migration(oldVersion, newVersion)` covering the schema change and register it where the database is built (check `di/` for the `Room.databaseBuilder` call).
4. Build once (`./gradlew assembleDebug` or run unit tests) so Room exports the new schema — this generates `app/schemas/brillembourg.notes.simple.data.database.AppDatabase/<newVersion>.json`.
5. Verify that JSON file was actually generated and stage it for commit along with the code changes. Do not skip this — it's the step that has been missed before.
6. Ignore/do not touch the stale `app/schemas/brillembourg.notes.simple.data.room.AppDatabase/` directory — it's from a since-renamed package and is not part of the active schema history.
