# ADR-0001: Use multi-module architecture from day one

- **Status:** Accepted
- **Date:** 2026-04-23
- **Deciders:** project lead
- **Tags:** architecture, build

## Context and problem

PaisaVault will grow from an MVP to a production app with SMS parsing, ML, OCR, insights, and an iOS companion. We must choose between a single-module ("monolith") Android project and a multi-module setup.

Single-module is faster to start. Multi-module is faster to build as the codebase grows, enforces architecture via the Gradle graph, and prepares us for Kotlin Multiplatform reuse on iOS.

A monolith that outgrows its boundaries is painful to split later because feature code becomes tangled with data-layer internals before anyone notices. We want the boundaries enforced by the compiler from the first commit.

## Decision drivers

- Codebase is expected to reach ~20k SLOC over v1.0
- iOS companion requires shared domain and data modules; KMP is easier to adopt on an already-modularized project
- Solo part-time development — every minute of incremental build matters
- Test strategy relies on fake implementations at module boundaries
- Code review discipline — "you can't import that" is more enforceable than a style-guide rule

## Considered options

1. **Single-module** — everything in `:app`
2. **Two-module** — `:app` + `:common`
3. **Multi-module by layer** — `:app`, `:ui`, `:domain`, `:data`
4. **Multi-module by feature + layer** — `:app`, `:feature:*`, `:data:*`, `:core:*`

## Decision

**Chosen option: Option 4 — multi-module by feature + layer.**

We scaffold all planned feature modules (`:feature:home`, `:feature:add`, etc.) and data/core modules in Phase 0, even if most are empty. Convention plugins in `build-logic/` eliminate per-module config duplication so the overhead is a one-time setup cost.

Dependency rules (enforced by Gradle):

- `:feature:*` cannot depend on another `:feature:*`
- `:feature:*` cannot depend on `:data:*` or `:core:database`
- `:core:domain` is pure Kotlin (no Android imports)
- `:data:*` depends only on `:core:*`

## Consequences

### Positive
- Architecture is compiler-enforced
- Incremental builds stay fast as code grows
- Compose stability inference is better per smaller module
- Fakes per module boundary make integration testing straightforward
- KMP-ready: `:core:domain` can become a shared source set with minimal changes
- Multiple people could work on different features without merge conflicts

### Negative / trade-offs
- ~2 extra days in Phase 0 to set up convention plugins
- Navigation from `:feature:A` to `:feature:B` requires route interfaces in `:core:domain` or `:core:ui`
- More `build.gradle.kts` files to maintain (mitigated by convention plugins)

### Neutral
- IDE initial indexing takes longer
- Gradle configuration cache helps amortize first-run cost

## Options considered in detail

### Option 1 — Single-module
Fastest to start, no Gradle complexity. Rejected: boundaries become unenforceable, incremental build degrades past ~100 files, KMP migration later is a large refactor.

### Option 2 — Two-module (app + common)
Marginal improvement over Option 1. Rejected: doesn't solve the real problem (feature isolation).

### Option 3 — Multi-module by layer
Splits `ui`, `domain`, `data`. Better than Option 2, but features bleed across all three modules, so a change to one screen touches three modules. Rejected in favor of Option 4.

### Option 4 — Multi-module by feature + layer
Chosen. Best isolation, best incremental build performance, best KMP path.

## Validation

- Phase 0 exit criterion: empty multi-module project builds clean on CI, under 90 seconds from cache, under 3 minutes cold.
- Revisit at v1.0: measure incremental build times and compare to a hypothetical monolith (by collapsing modules in a branch).

## Links

- Nia reference architecture: https://github.com/android/nowinandroid
- Guide to app architecture: https://developer.android.com/topic/architecture
- Convention plugins pattern: https://developer.squareup.com/blog/herding-elephants/
