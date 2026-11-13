## Summary

<!-- What does this PR do and why? Link the requirement/handoff docs if any: docs/delivery/<requirement-slug>/ -->

## Modules affected

-

## Type of change

- [ ] Feature
- [ ] Bug fix
- [ ] Refactor (no behavior change)
- [ ] Docker image / CI configuration
- [ ] Documentation only
- [ ] Breaking change (requires migration notes below)

## Changes

<!-- Bullet list of the key changes. if a spec file is used to generate the code for this pr reference it here using the spec id-->

-

## Testing

- [ ] `mvn -B clean verify` — passed
- [ ] `docker build -t openjcockpit/embabel-agent-runtime:local .` — passed
- [ ] 100% JaCoCo line coverage for handwritten production code in scope — measured and enforced
- [ ] Sonar scan - passed
- [ ] Manual verification steps (describe if applicable):

## Security / auth impact

- [ ] No change to authN/authZ, tokens, roles, or Keycloak config
- [ ] Changed — 401/403/role/tenant boundaries covered by tests
- [ ] No secrets, tokens, or client secrets introduced in code, config, or frontend bundle
- [ ] TBD DAST tool action - passed

## Compatibility & contracts

- [ ] Backwards compatible, or breaking change explicitly agreed and documented
- [ ] `service-common` contracts kept in sync with the OpenJCockpit platform (`embabel-agent-service`)

## Docs & handoff

- [ ] Handoff/decision docs updated under `docs/delivery/<requirement-slug>/` if this followed the fixed workflow
- [ ] README / module docs updated if behavior, setup, or ports changed

## Risks & open items

<!-- Explicitly state any open risks, manual steps, or checks not performed -->

-
