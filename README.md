# Mob Size Diversity

Fabric mod for Minecraft **26.2**. Living mobs roll a **persistent** random scale. Commons and elites use different size bands so silhouette stays an elite marker. Attack reach follows scale by default. Other mods' mobs are opt-in via JSON (primary) or datapack.

Does not change HP, damage, or loot. Elite spawn and loot stay in [Mob Loot Rebalance](https://github.com/spicato-minecraft/mob_loot_rebalance). Gait and Better Combat stay in Mob Combat Rebalance.

## Compatibility

| Component     | Version           |
|---------------|-------------------|
| Minecraft     | 26.2              |
| Fabric Loader | 0.19.5+           |
| Fabric API    | 0.161.0+26.2      |
| Java          | 25+               |

## Build

From this directory:

```bash
./gradlew build
```

The output JAR is at `build/libs/mob_size_diversity-1.0.0.jar` (version from `gradle.properties`).

## Test

Play the indexed local set from [`../dev-pack-26.2`](../dev-pack-26.2/README.md) (`./gradlew runClient` there). This repo’s `runClient` is for isolated Size work; GameTests still pull local MLR when that repo is also on 26.2.

```bash
./gradlew test          # unit tests (JUnit + Fabric Loader bootstrap)
./gradlew runGameTest   # server GameTests
./gradlew runClient     # isolated Size client (not the pack launcher)
./gradlew build         # full CI gate (includes unit tests + GameTests)
```

## Install

1. Install [Fabric Loader](https://fabricmc.net/use/) for Minecraft 26.2.
2. Download [Fabric API](https://modrinth.com/mod/fabric-api) for 26.2 and place it in your mods folder.
3. Copy the built JAR into your Minecraft `mods` folder.

## Branch Policy

Each supported Minecraft version has its own long-lived branch named after the exact `minecraft_version` in `gradle.properties`. Version branches are the source of truth; `main` is not used.

- **Default branch:** `26.2`
- **Feature/fix work:** branch from the target version as `{version}/feature-name`, merge back into the version branch

```bash
git clone https://github.com/spicato-minecraft/mob_size_diversity.git
cd mob_size_diversity
git checkout 26.2
```

## Releases and Modrinth

Merging code does **not** publish to Modrinth. Publishing happens only when a GitHub Release is created.

Release tags use `{minecraft_version}-{mod_version}` (e.g. `26.2-1.0.0`), matching `gradle.properties`.

1. Merge your changes into the version branch (e.g. `26.2`).
2. Bump `mod_version` in `gradle.properties` if shipping new player-facing changes.
3. Add a changelog at `releases/{tag}.md` — see `releases/TEMPLATE.md`.
4. Create a GitHub Release with tag `{minecraft_version}-{mod_version}` targeting the version branch.

See [minecraft-mods/docs/README.md](../docs/README.md) for org-wide release conventions.

## Test Strategy

| Layer      | Purpose |
|------------|---------|
| Unit tests | Sampler PDF; rule matcher; extra reach identity when coupling is off |
| GameTests  | Persist; armor stand skipped; cow Set C vs zombie Set A; extra reach follows roll |

Behavior tests are tracked in [Notion Tasks](https://app.notion.com/p/3b716e8d4da380adbd93cf0a73b39d5c). Manual UAT items live in the Mod Catalog.

## Architecture

JSON at `config/mob_size_diversity.json` is the primary rule surface (presets + tag/entity/namespace rules). Datapack entity tags can add mobs to `#mob_size_diversity:set_a`; they do not define curves. Unknown namespaces do not roll until JSON or that tag includes them. `reach_scales_with_size` (default true) couples extra attack reach to the roll — including a multiply on Mob Combat Rebalance / Better Combat range. Off keeps `SCALE` (hitbox, step, eye height) and drops that extra coupling.

Requirements: [Mob Size Diversity](https://app.notion.com/p/3cf16e8d4da381799edbfaedb6e73e2a).
