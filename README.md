# Pets & Pals

**Pets & Pals** brings your own client-side companions into your game — pets with real presence that walk at your side, react to the world, and follow you anywhere. From squids to iron golems to even penguins, Pets & Pals offers nearly every mob from vanilla Minecraft, and more! And it works no matter whether you are on a multiplayer server or your own singleplayer world.

Pets & Pals comes with its own configuration screen **and** custom commands to easily toggle between pets, change whether the mod is enabled at all, and switch up the appearance of your pets with **skins** and **names**.

> **Pets & Pals is a fork of [Pets Mod](https://github.com/downloadableduck/petsmod) by downloadableduck and The Pets Team** — full credit to them for the original mod this project is built on (which itself grew out of [DuckMod](https://modrinth.com/mod/duck--mod)). This fork is a ground-up redesign: real pathfinding-driven movement and animation, minimap compatibility, a proper addon API, a new companion-catalog UI, multi-version support, and opt-in networked pet visibility.

## Features
- Almost EVERY vanilla Minecraft mob
- Three CUSTOM mobs tailored specifically for this mod, complete with custom ambient sounds
- Custom interactions
- Completely client-sided and will work on ANY server, such as Hypixel, Mineplex, or MCC Island
- Custom names
- Skins for mobs that have multiple variants such as frogs, villagers, or cows

## Customization
Make your pets your own with **skins** and **names**! You can access these in two ways: by using our configuration menu or using `/petskin` and `/petname`. Both of these will switch up your pet's appearance in real time and make it yours!

There is no need to do this multiple times - all of this information is stored in a config file located in `.minecraft/config` and will be read every time you enter a new world.

To switch between pets, simply run `/petspecies <pet>`.

> **TIP:** Pet names are stored for each individual pet - if you have both a blaze and a duck, you can name them each individually!

## Interactions
Simply shift and right click on a mob with an **empty hand** to pick it up, and shift and jump to drop it again!

## Redesign Roadmap
See `RESEARCH.md` for the Phase 0 research behind the redesign. The phases, in order:
1. Real movement & animation (PathNavigation-driven following — no more floating or teleporting pets)
2. Minimap compatibility (Xaero's radar icons; JourneyMap when testable)
3. Menagerie UI (a companion catalog, not a settings list)
4. Addon API + user-created pets + generic "any mod's entity as a pet" support
5. Opt-in networked pet visibility (friends can see your pets)
6. Multi-version ports (1.21.11+ and the 26.x line first; older majors back to 1.16.5 as stretch)
7. Publish on Modrinth

## Requirements
This mod requires [Fabric API](https://modrinth.com/mod/fabric-api), [Cloth Config API](https://modrinth.com/mod/cloth-config), [YACL](https://modrinth.com/mod/yacl) (YetAnotherConfigLib) and [Mod Menu](https://modrinth.com/mod/modmenu). It's a lot, I know, but hopefully you already have most of them installed!

## FAQ
**Is this mod paid/are some features paid?**
No, and they never will be! This mod will forever remain free for everyone to use.

**How does this relate to the original Pets Mod?**
Pets & Pals started as a full redesign of Pets Mod (CC0-licensed) and is published as its own standalone mod, with credit to the original. Existing Pets Mod addons target the original mod's `pets-mod` id; a compatibility layer and a proper addon API are part of the roadmap (Phase 4).

**How can I contribute?**
Play the mod! If you have an issue, please report it on GitHub — we look forward to making this mod the best - and clearest of bugs - that it can be!
