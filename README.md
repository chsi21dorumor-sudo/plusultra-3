# Plus Ultra — a My Hero Academia fan mod (NeoForge 1.21.1)

Unofficial fan project. *My Hero Academia* belongs to Kohei Horikoshi / Shueisha.
All code, textures and data in this project are original; nothing is taken from Clover Craft or any other mod.

## Features

| System | What it does |
|---|---|
| **Quirks** | 6 Quirks (One For All, Half-Cold Half-Hot, Explosion, Hardening, Zero Gravity, Engine), 3 abilities each |
| **Stamina** | Resource bar with regen, used by every ability (free in creative and in the Training Ground) |
| **Levels / Plus Ultra** | Using abilities earns XP. Level scales damage and max stamina. The 3rd ability unlocks at level 5 (configurable) |
| **Mission Board** | Right-click for a Patrol (kill 10 hostiles) or Villain mission. Sneak + right-click abandons it |
| **Villain boss** | Tough melee boss with boss bar and a periodic Decay shockwave. Drops emeralds and sometimes a Quirk Orb |
| **Hero ranks** | Missions give hero points: Trainee → Rookie → Pro → Top 10 → Number One. Hero License item shows your rank |
| **Hero Agencies** | `/agency create|join|leave|info|list`. Members within 16 blocks get +20% stamina regen each (max +60%) |
| **Training Ground** | `/training` / `/training leave`. Flat, always-daytime dimension; abilities cost no stamina |
| **Items** | Quirk Orb (awakens a random Quirk), Quirk Eraser (removes it), Hero License, Mission Board, Villain Spawn Egg |

Keys (rebindable): **Z / X / C** = abilities 1 / 2 / 3.

Admin commands: `/plusultra quirk set <player> <id>`, `/plusultra quirk clear <player>`, `/plusultra level set <player> <n>`, `/plusultra info`.

Config: `config/plusultra-common.toml` (damage multiplier, stamina regen, max level, Plus Ultra unlock level,
whether quirks can hurt players, whether Explosion abilities break blocks).

## Building

Requires JDK 21 and Gradle 8.8+ (the Gradle wrapper is not included).

    gradle wrapper        # once, to generate ./gradlew
    ./gradlew runClient   # test in a dev client
    ./gradlew build       # jar ends up in build/libs/

Versions are in `gradle.properties` (`neo_version`) and `build.gradle` (ModDevGradle plugin version).
Bump them if Gradle can't resolve them.

## Status

Written without access to Minecraft/NeoForge jars, so it has **not been compiled or run yet**. Expect to fix a few
small API mismatches on the first build. Likely spots: `SoundEvents` constants that are `Holder`s rather than
`SoundEvent`s in your NeoForge version, `AttachmentType` builder overloads, and `RegisterGuiLayersEvent` naming.

## Project layout

    core/      registries (items, blocks, entities, attachments, tabs, dimension key) + server events
    quirk/     Quirk enum, per-player data, ability effects, ability manager (cost / cooldown / XP)
    mission/   mission board block, mission logic, ranks
    agency/    saved-data Hero Agencies
    entity/    Villain boss
    item/      Quirk Orb, Quirk Eraser, Hero License
    net/       payloads: UseAbility (client→server), Sync (server→client)
    client/    keybinds, HUD, villain renderer
    command/   /plusultra, /agency, /training

## Extending

- **New Quirk**: add an enum constant in `Quirk`, write its three ability methods in `Abilities`, add lang keys
  `quirk.plusultra.<id>` and `ability.plusultra.<id>.<ability>`.
- **Better visuals/animations**: swap the vanilla humanoid renderer for GeckoLib models; add Photon/particle effects.
- **New mission types**: add a constant and a case in `Missions.assign / onKill / describe`.
