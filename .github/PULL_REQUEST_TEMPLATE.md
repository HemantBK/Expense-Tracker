## Summary

<!-- What does this PR do, and why? One or two paragraphs. -->

## Type of change

- [ ] `feat` — new feature
- [ ] `fix` — bug fix
- [ ] `refactor` — restructure with no behavioral change
- [ ] `perf` — performance improvement
- [ ] `test` — adds or improves tests
- [ ] `docs` — documentation only
- [ ] `build` — build system / dependencies
- [ ] `ci` — CI configuration
- [ ] `chore` — housekeeping

## Linked issue

<!-- Closes #NNN  |  Related to #NNN  |  N/A with reason -->

## Architectural impact

- [ ] Introduces a new module
- [ ] Crosses an existing module boundary (please explain)
- [ ] Changes a public API of a `:core:*` module
- [ ] Adds an ADR (link below)
- [ ] None of the above

<!-- If architectural: link to ADR in docs/adr/ -->

## Test plan

<!-- How did you verify this change? What new tests did you add? -->

- [ ] Unit tests added / updated
- [ ] Integration tests added / updated
- [ ] UI tests added / updated
- [ ] Screenshot tests added / updated (design-system changes)
- [ ] Macrobenchmark added / updated (performance-affecting changes)
- [ ] Manual QA on physical device (describe below)

### Manual QA

<!-- Device(s), Android version(s), scenarios exercised. -->

## Accessibility (for UI changes)

- [ ] Interactive elements have `contentDescription` or semantics roles
- [ ] Touch targets ≥ 48dp
- [ ] TalkBack focus order verified
- [ ] Works at 200% font scale
- [ ] Contrast passes WCAG AA
- [ ] RTL-safe
- [ ] N/A (non-UI change)

## Internationalization (for UI changes)

- [ ] All new strings in `strings.xml`
- [ ] No string concatenation (parameterized formats used)
- [ ] Plurals use `<plurals>` resource where relevant
- [ ] Currency / date formatters are locale-aware
- [ ] N/A

## Security impact

- [ ] Touches crypto, Keystore, biometric, or SMS parsing
- [ ] Adds a new permission or changes manifest
- [ ] Adds network access
- [ ] Logs sensitive data (please explain why, or remove)
- [ ] None of the above

## Performance impact

<!-- If this could affect startup, memory, scroll, DB, or APK size, state the measured impact. -->

- [ ] APK size change: + / - ___ KB
- [ ] Benchmark diff (cold start / scroll): ___
- [ ] Negligible
- [ ] N/A

## FOSS / dependency impact

- [ ] No new dependencies
- [ ] New dependency added — license is OSI-approved (listed below)
- [ ] `gradle/verification-metadata.xml` regenerated and committed

<!-- If deps added: name, version, license, why needed, size impact -->

## Checklist

- [ ] `./gradlew check` passes locally
- [ ] Commit messages follow Conventional Commits
- [ ] Commits are signed
- [ ] CHANGELOG.md updated under Unreleased (if user-visible)
- [ ] Docs updated (README/BUILD/PRIVACY) if behavior changed
- [ ] No hardcoded strings; no `android.util.Log`; no `println`
- [ ] PR is focused on one logical change
