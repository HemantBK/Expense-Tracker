# ADR-0005: Use MVI (UDF) for screen state

- **Status:** Accepted
- **Date:** 2026-04-23
- **Deciders:** project lead
- **Tags:** architecture, ui

## Context and problem

Every Compose screen needs a reliable contract for: what state to render, what events the UI can raise, and what one-shot side effects (navigation, snackbar) to fire. Three common shapes compete:

- **MVVM with observable fields** — ViewModel exposes multiple `StateFlow`s. Simple for small screens but state becomes scattered.
- **Plain ViewModel + single `UiState`** — one state holder, one flow. Cleaner, but no formal event/effect contract.
- **MVI (Model–View–Intent)** — one state, one event sink, one effect channel. Unidirectional. Compose-friendly.

We need consistency across 6+ screens and eventual team contribution.

## Decision drivers

- Compose's model is naturally unidirectional — MVI matches
- We want navigation and errors handled as *effects*, not as state (replaying a navigation on config change is a bug)
- Testability: given events, assert state transitions and effects
- Consistency: one screen should look like every other screen

## Considered options

1. **MVVM, ad-hoc state holders per screen**
2. **MVVM with single `UiState` data class**
3. **MVI with UiState + UiEvent + UiEffect**

## Decision

**Chosen option: Option 3 — MVI.**

Every screen gets:
- `FooState : UiState` — a data class; one flow
- `FooEvent : UiEvent` — sealed hierarchy of things the UI can do
- `FooEffect : UiEffect` — sealed hierarchy of one-shot events (nav, snackbar)
- `FooViewModel : MviViewModel<FooState, FooEvent, FooEffect>` — reduces events into state, sends effects

The base class lives in `:core:ui` (`MviViewModel`). Compose collects state via `collectAsStateWithLifecycle()` and effects via `LaunchedEffect { vm.effects.collect { ... } }`.

## Consequences

### Positive
- One consistent shape across all features
- Effects survive correctly across recomposition without re-firing
- Pure-function state reducers are trivially unit-testable
- Navigation commands live outside state → no navigation-replay bugs on config change
- New contributors learn the pattern once

### Negative / trade-offs
- Small amount of boilerplate per screen (3 files: contract, VM, screen)
- Channel-based effects don't survive process death (we accept this — effects are one-shot)

### Neutral
- Some screens with trivial state feel over-engineered; we still apply the pattern for consistency

## Validation

- Screen reviews check: does it follow the contract? (compile-time via `MviViewModel<...>`)
- UI tests can send events and assert state without touching Compose (tests the VM directly)

## Links

- Android UI state guide: https://developer.android.com/topic/architecture/ui-layer
- Nia's UI pattern: https://github.com/android/nowinandroid
