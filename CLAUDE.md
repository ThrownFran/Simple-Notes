# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Simple Notes — single-module Android app (`applicationId brillembourg.notes.simple.fast`, package root `brillembourg.notes.simple`). Clean Architecture + MVVM, Kotlin coroutines/Flow, Room, XML views with data binding/view binding (no Jetpack Compose).

## Architecture

Package layout under `app/src/main/java/brillembourg/notes/simple/`:
- `data/` — Room database (`data/database/`), user prefs (`data/user/`)
- `domain/` — `models/`, `repositories/` (interfaces), `use_cases/` (one per feature)
- `presentation/` — one package per feature (`home/`, `detail/`, `categories/`, `trash/`, `settings/`, `about/`, `license/`), each following `*ViewModel` + sealed `*UiState`. Single `MainActivity` hosts all Fragments via Navigation component.
- `di/` — Hilt modules (`AppModule`, `SharedStateModule`, `DataSourceModule`, `RepositoryModule`, `ActivityScopedModule`)

DI is Hilt with **kapt** (not KSP) — expect slower incremental builds.

## Build/test

- Build: `./gradlew assembleDebug`
- Unit tests: `./gradlew test`
- Instrumented tests: `./gradlew connectedAndroidTest` (currently just the default Espresso stub — no real instrumented coverage)
- Java/Kotlin target: 21. compileSdk/targetSdk 34, minSdk 21.
- No ktlint/detekt/.editorconfig configured — match surrounding code style manually.

## Room schema policy

Room schema export is enabled (`room.schemaLocation` → `app/schemas/`), and `app/schemas/` is **not** gitignored — schema JSON files are meant to be committed. Whenever the `@Database` version in `AppDatabase.kt` (`data/database/`) is bumped, commit the corresponding `app/schemas/brillembourg.notes.simple.data.database.AppDatabase/<version>.json` in the same change. Use the `room-migration` skill for the full checklist. Note: there's a stale, orphaned `app/schemas/brillembourg.notes.simple.data.room.AppDatabase/` directory left over from an old package name — do not add to it.

## Commit style

Use Conventional Commits (`feat:`, `fix:`, `chore:`, etc.) going forward — existing history predates this convention and is informal, so don't pattern-match old commit messages.

## Gotchas

- `app/google-services.json` is intentionally committed (Firebase config).
- No release signing config in Gradle — release builds are unsigned as checked in.
