# Diagrams

Source-controlled diagrams for the repo. Two formats, each with its own job.

---

## Rendering conventions

| Format | File extension | Where it renders | When to use |
|---|---|---|---|
| **Mermaid** | inline fenced block in `.md` | Natively on GitHub / GitLab / VS Code | 80% of diagrams — sequence, state, flowchart, C4 context |
| **D2** | `*.d2` in this folder | Rendered to `*.svg` via `d2` CLI; SVG committed alongside | Dense layered diagrams that Mermaid makes ugly (the module graph) |

Rule of thumb:
- If it fits inline in markdown and GitHub shows it nicely, use **Mermaid**.
- If it needs multi-layer grouping, color themes, and > ~15 nodes, use **D2**.

Everything else (draw.io, Figma, Excalidraw, Lucid, Miro) is banned from the repo.
Binary diagram sources are not diffable, not reviewable, and rot silently.

---

## Files in this folder

| File | Purpose |
|---|---|
| [`module-graph.d2`](module-graph.d2) | D2 source — 22-module dependency graph with layers |
| `module-graph.svg` | Rendered output (regenerated from `.d2`, committed for readers) |

---

## Regenerating the D2 diagrams

Install the D2 CLI once. It's a single Go binary with no runtime dependencies.

```bash
# macOS
brew install d2

# Linux / WSL
curl -fsSL https://d2lang.com/install.sh | sh -s --

# Windows (winget)
winget install Terrastruct.d2
```

Render:

```bash
cd docs/diagrams
d2 --theme=0 --layout=elk module-graph.d2 module-graph.svg
```

Flags explained:
- `--theme=0` — neutral default theme. Other useful themes: `200` (Flag green), `101` (Terminal dark).
- `--layout=elk` — the Eclipse Layout Kernel engine. Much better for dense graphs than D2's default `dagre`.

Commit both the updated `.d2` and the new `.svg` in the same PR.

## Regenerating when modules change

The D2 graph is hand-maintained, not auto-generated, so it can drift. Rule:

> **If you add, remove, or re-wire a module, update `module-graph.d2` in the same PR.**

A CI check will fail the build if `settings.gradle.kts` has modules not listed in
`module-graph.d2` (pending — will land with the release workflow).

---

## Mermaid diagrams (no tooling)

Mermaid diagrams live inside markdown files — there's no source file in this folder.
Look for fenced code blocks tagged ` ```mermaid ` in:

- [`../architecture.md`](../architecture.md) — C4 context, module overview, 4 data flows,
  lock state, MVI state
- [`../development.md`](../development.md) — dev loop diagrams (future)
- [`../adr/*.md`](../adr/) — decision context diagrams

To preview before pushing: any modern IDE's markdown viewer (VS Code, IntelliJ,
Android Studio) renders Mermaid inline.

---

## Why not PlantUML, draw.io, Excalidraw, or screenshots?

| Tool | Why rejected |
|---|---|
| **PlantUML** | Needs Java to render, looks dated, no benefit over Mermaid for our use cases |
| **draw.io / diagrams.net** | XML is technically diffable but reviewers hit-and-miss; shape library is a temptation toward over-decoration |
| **Excalidraw / tldraw** | Great for whiteboarding; JSON isn't reviewable in a PR |
| **Figma / Lucid / Miro** | Not committable; access-controlled; link-rot |
| **Pasted screenshots** | Rot silently when code changes; PR reviewers can't verify against reality |

The whole repo's diagram stack must be something a new contributor can render from
source with a single command.

---

## Future diagrams (not yet drawn)

- Release pipeline (tag → signed APK → GitHub Release)
- Data model ER diagram (auto-generated from Room schemas)
- Threat tree (STRIDE breakdown) — may live in `docs/threat-model.md`
- Build cache + CI flow
