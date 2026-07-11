# Changelog

## 0.14.9
- Fixed the raft cushion showing the whole cushion texture sheet instead of its proper faces (the cushion texture is an entity-style unwrap, not a flat tile)
- Public builds now unlock the polished pets first: Copper Golem, Fox, Cat (all 12 skins, including ocelot) and Bee - the rest of the catalog shows greyed out as "coming soon" while each species gets the full movement treatment

## 0.14.8
- Fixed the endless spawn-animation loop (and disappearing rafts/orbs): every client-side entity was being created with the same internal id, so each new one silently deleted the last. Pets, dwellings, rafts and orbs now get their own ids
- Menagerie: hover the raft or cushion button to see a live spinning preview of your raft
- New "Cushion: None" option for a bare deck, and poplar as a 13th raft wood
- The cushion color you pick now actually applies to the raft (it was stuck on red)
- Stairs no longer trip sprinting pets - shallow drops read as slopes, not cliffs
- Pets no longer hop onto carpets and other flat blocks they can simply walk over
- Pits deeper than 3 blocks now count as cliffs; pets stop at the edge instead of diving in
- If the pet can't catch up mid-sprint it now re-enters from behind the camera instead of lagging behind until you stop and sprint again

## 0.14.2 (26.3 snapshot)
- Cushion colors! All 16 dyes on the Mk2 raft, with a favorite-cushion picker in the Menagerie - real snapshot cushion textures, so packs restyle them
- Fixed the cushion texture path (the snapshot ships cushions as entities; their textures are now merged into the block atlas)
- Includes the 0.14.1 fixes: cliff guard, ferry rafts, boat-matched raft wood
## 0.14.7
- Pets, spawn bushes, and repositions can no longer be placed underwater - fixes the spawn animation looping, foxes appearing under the sea, and raft flicker over water
- Default keybind moved from P (vanilla social menu) to ; (semicolon)
- Menagerie preview no longer gets stuck saying a world is needed
- Removed the mega spud from the catalog

## 0.14.6
- Fixed pets endlessly teleporting instead of walking on 26.3 snapshots: the snapshot broke client-side pathfinding, so pets now steer directly at you (with step-hopping and surface swimming) whenever pathfinding is unavailable

## 0.14.5
- Fixed the raft spawning a duplicate every tick while boating (the glitchy pile-up of rafts fighting over your pet)
- Ferry rafts now wait beside a swimming owner instead of dropping the pet back into the water

## 0.14.1
- Land pets no longer run or jump off cliffs while sprinting with you - they stop at edges with no safe landing
- Ferry rafts: if a land pet has to cross water to reach you, a raft blips in under it and carries it across at full speed, dropping it at the far shore
- The boat-side raft now matches your boat's wood automatically; your chosen style is used for ferries

## 0.14.0
- New: 26.3-snapshot-3 version! The raft upgrades to the Mk2 model with the new cushion on deck (all 12 wood styles included)
- Raft wood is now customizable: 12 plank styles (oak through warped) selectable in the Menagerie, using the game's own textures so resource packs restyle them
- Realistic tow physics: the raft trails BEHIND your boat on a taut-rope model - it drifts with water drag, gets yanked when the rope tightens, and swings wide through turns
- Rafts are locked to water: they can never slide onto land

## 0.13.1
- Sprinting pets now swim properly at the water surface instead of wallowing when the route crosses water
- Parkour: sprinting pets leap gaps when there's a landing within a few blocks, and hop steps before bumping into them

## 0.13.0
- The raft is now tied to your boat with a real lead - and it sits properly on the water surface (actual fluid height, not a guess)
- Raft uses the game's own spruce plank texture, so resource packs restyle it automatically; hitbox now matches its flat shape and pets stand right on the deck
- Sprinting pets now drive in a straight line parallel to you with pre-planned jumps instead of pathfinding - the start and obstacle handling should finally feel solid

## 0.12.2
- Fixed the sprint run-alongside only working while staring at your pet: it now reliably arrives in formation whenever you sprint, watched or not
- Brief sprint interruptions (wall bumps, brushing entities) no longer cancel run-alongside
- Raft now sits properly on top of the water
- Raft trails the boat with towed physics - it swings wide in turns and settles like it's on a rope
- Pets have little moods on the raft: they sit down, stand back up, and look around while you row

## 0.12.1
- Fixed broken raft texture mapping that could crash resource reloads with MoreCulling installed
- All releases are now marked alpha while the mod is in heavy development
- Added the Mk2 raft model (with the new cushion) ready for 26.3-snapshot-3+

## 0.12.0
- New: pet raft! When you get in a boat, a little wooden raft (model by aaronateataco) floats alongside and your pet rides it until you hop out

## 0.11.1
- Releases now include proper changelogs on GitHub and Modrinth
- Marked as client-side only on Modrinth
- Internal comment cleanup

## 0.11.0
- Fixed the Summon button being able to spawn a second pet on servers
- Sprint entrance now works from any position: a visible pet runs off camera behind you first, then swings back into view
- Pets react to turns with a slight delay and visibly correct course instead of snapping instantly
- Menagerie: new Land / Sky / Sea tabs
- Themed spawn animations for ~25 more species (foxes rise from a berry bush, zombies dig out of coarse dirt, slimes from slime blocks, and more)
- Flying pets no longer drift high above you - they sink back below ~3.5 blocks over your head

## 0.10.5
- Pets can no longer be lost: hard 20-block leash plus a watchdog that rescues a pet even if its chunk unloaded

## 0.10.4
- The ghost star is back as a last resort: if a pet is stuck with nowhere to teleport, it turns into a floating star that glides to you and reforms
- While sprinting, a stuck pet's star flies in the running position instead of hiding at your hip
- The sprint entrance timer now runs whether or not the pet is on screen

## 0.10.3
- Sprint entrance: the pet leaps in from behind the camera at full speed instead of appearing in place
- If the pet's side is blocked it swaps sides; in tunnels and hallways it runs single file ahead of you
- Fixed picking a pet in the Menagerie not actually applying until Summon was pressed
- Every pet now spawns in front of you on solid ground
- Added a Privacy & Compliance section to the readme

## 0.10.2
- Major follow fix: pets no longer stutter or give up at gaps and stairs
- Pets never teleport while you are facing their location, even through walls
- Lost-pet handling moved out of the follow goal so it can't be reset mid-chase

## 0.10.1
- Menagerie now shows a live animated preview of the selected pet next to your player at true scale
- Removed the ghost star in favor of particle + chime repositioning (it returned in 0.10.4 as a fallback)
- Releases can auto-publish to Modrinth

## 0.10.0
- New Menagerie screen: searchable catalog of all pets with summon, speed and volume controls

## 0.9.x
- Real vanilla movement AI for all pets (pathfinding, animations, per-species speeds)
- Fortnite-style sprint run-alongside and combat shoulder-perch
- Bee nest spawn animation, pet volume setting, Xaero's Minimap radar icons
- Renamed to Pets&Pals with a new fox icon
