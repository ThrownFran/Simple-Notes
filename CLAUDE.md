# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Simple Notes — single-module Android app (`applicationId brillembourg.notes.simple.fast`, package root `brillembourg.notes.simple`). Clean Architecture + MVVM, Kotlin coroutines/Flow, Room, XML views with data binding/view binding (no Jetpack Compose).

## Architecture

Package layout under `app/src/main/java/brillembourg/notes/simple/`:
- `data/` — Room database (`data/database/`), user prefs (`data/user/`)
- `domain/` — `models/`, `repositories/` (interfaces), `usecases/` (one per feature)
- `presentation/` — one package per feature (`home/`, `detail/`, `categories/`, `trash/`, `settings/`, `about/`, `license/`), each following `*ViewModel` + sealed `*UiState`. Single `MainActivity` hosts all Fragments via Navigation component.
- `di/` — Hilt modules (`AppModule`, `SharedStateModule`, `DataSourceModule`, `RepositoryModule`, `ActivityScopedModule`)

DI is Hilt with **kapt** (not KSP) — expect slower incremental builds.

## Build/test

- Build: `./gradlew assembleDebug`
- Unit tests: `./gradlew test`
- Instrumented tests: `./gradlew connectedAndroidTest` (currently just the default Espresso stub — no real instrumented coverage)
- Java/Kotlin target: 21. compileSdk/targetSdk 34, minSdk 21.
- Lint: `./gradlew ktlintCheck` / `./gradlew ktlintFormat` (ktlint, official Kotlin style, no rules disabled — root `.editorconfig` only sets `max_line_length = 140`). No wildcard imports; package names have no underscores; properties/params are camelCase (or SCREAMING_SNAKE_CASE for top-level `const val`s). A handful of Room migration SQL literals that can't be wrapped carry a local `@Suppress("ktlint:standard:max-line-length")` on the enclosing function instead of a blanket rule disable. Document properties/params with KDoc (`/** ... */` / multi-line block, `@param`), not line comments — ktlint disallows a trailing `//` comment inside a parameter list.
- Known pre-existing issue: `CategoriesViewModelTest.kt` fails to compile (`sut.onSelection()` calls missing required `isSelected`/`id` args) — `./gradlew test` currently fails because of this, unrelated to any specific change.

## Room schema policy

Room schema export is enabled (`room.schemaLocation` → `app/schemas/`), and `app/schemas/` is **not** gitignored — schema JSON files are meant to be committed. Whenever the `@Database` version in `AppDatabase.kt` (`data/database/`) is bumped, commit the corresponding `app/schemas/brillembourg.notes.simple.data.database.AppDatabase/<version>.json` in the same change. Use the `room-migration` skill for the full checklist. Note: there's a stale, orphaned `app/schemas/brillembourg.notes.simple.data.room.AppDatabase/` directory left over from an old package name — do not add to it.

## Commit style

Use Conventional Commits (`feat:`, `fix:`, `chore:`, etc.) going forward — existing history predates this convention and is informal, so don't pattern-match old commit messages.

## Gotchas

- `app/google-services.json` is intentionally committed (Firebase config).
- No release signing config in Gradle — release builds are unsigned as checked in.
