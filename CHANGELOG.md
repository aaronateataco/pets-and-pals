# Changelog

## 0.27.0
- The tow rope now actually anchors at the back of the boat and the front of the raft instead of both centers - it used to draw straight through the middle of both hulls
- Fixed pets bouncing between swimming and re-summoning a ferry raft over multi-part crossings (a lake with an island, back-to-back rivers) - the raft used to let go the moment it found any dry patch ahead, even if that wasn't actually where you were; now it only lets the pet off once you're the one on dry land

## 0.26.0
- LeadPhysics is only required on this branch's counterparts that actually have a build for it (checked the Modrinth API directly - it only publishes for 26.1.x, nothing for 26.2 or the 26.3 snapshot). Dropped as a hard requirement on those two so the mod isn't permanently unable to launch there
- Added native rope sag for the raft's tow line on every branch, LeadPhysics or not - vanilla's own leash renderer already supports a sagging curve, it just needed turning on

## 0.25.0
- LeadPhysics is now a required dependency - Pets&Pals won't launch without it installed alongside it

## 0.24.0
- Raft tow has physical give again instead of an instant position lock - it eases toward its spot behind the boat with a spring, not a rope/drag simulation, so it still can't drift or desync the way the old model could
- Added [LeadPhysics](https://modrinth.com/mod/leadphysics) as a recommended (not required) companion mod - it replaces vanilla's straight leash line with a sagging rope curve for any leashed entity, including the raft's tow line

## 0.23.0
- Replaced the rope/spring raft-tow simulation with a rigid attachment: the raft's position is copied directly off the boat's own transform every tick (offset behind the stern), instead of being simulated independently with velocity and drag. It can no longer drift, lag behind, or float loose - only the visual yaw still eases in on sharp turns
- Fixed land pets zigzagging diagonally left-right instead of running in a straight line - the predictive lead point (added for normal, non-sprint following) read the owner's raw per-tick velocity, which has enough natural noise to flip the predicted direction by a few degrees every tick

## 0.22.0
- Fixed the boat actively pushing the raft away every tick, fighting directly against its own tow physics - this was the real cause behind the raft never settling in close behind the boat
- Fixed the tow rope floating up in the air above the raft instead of meeting the boat at deck level (it was anchored at an inherited "eye height" that doesn't match the raft's actual thin deck)
- Pets and the raft no longer physically collide with you, other entities, or boats - having them around should never get in your way
- Pets can range farther before being reeled back in while standing somewhere with open sky (fields, beaches) - the tight leash is now specific to tight indoor/underground spaces
- Fixed a second source of first-person sprint-alongside twitch: the head's look-ahead point used the pet's own raw, unsmoothed velocity
- Menagerie: browse arrows now hide during the naming page instead of floating over it; "Advanced settings" logs an error instead of silently doing nothing if it fails to open

## 0.21.0
- Pets no longer cast a shadow - the catch-up/reposition logic made it visibly snap around underneath them
- Smoothed the sprint-alongside steering lead, which was amplifying the sprinting owner's small per-tick velocity noise into a visible first-person twitch

## 0.20.0
- Sulfur cube backported as a pet on this version (26.1.2) - it's a real vanilla mob starting in 26.2 that doesn't exist here yet, so the geometry and textures were pulled straight from the newer client jar rather than approximated. Registered under the mod's own id so it can't collide with other backporting mods, and never spawnable via vanilla creative/summon - pets only
- Sulfur cube: 10 of the real mob's archetypes (bouncy, fast flat, fast sliding, high resistance, light, slow bouncy, slow flat, slow sliding, sticky, regular) are selectable with `/petskin` as a jump/speed flavor - explosive and hot were left out since those would just hurt the owner
- Fixed the raft trailing behind boats with a wide gap of open water instead of sitting close behind - also fixed it stalling out over uneven water (waterfalls, locks) where it could misread as "no water" and stop following entirely

## 0.19.0
- Removed the legacy custom title screen/splash text toggle (a petsmod holdover) - Pets&Pals no longer touches the vanilla main menu logo
- Fixed the "Advanced settings..." button doing nothing useful on the snapshot branch (it was reopening the Menagerie itself instead of the real config screen)
- Menagerie: added a Baby/Adult toggle and a golden dandelion you can drag onto your pet (same gesture as the name tag) to keep a baby pet from growing up
- Pets now lead their path toward where you're heading (blend of travel direction and where you're looking) instead of always chasing your exact position from half a second ago
- Fixed the combat-withdrawal nether star floating in front of you while sprinting instead of tucking behind your shoulder
- Smoothed the boat-tow raft physics to match the ferry's glide - it was noticeably twitchier despite using the same momentum model

## 0.18.0
- Fixed sprint run-alongside only catching up when you happened to be looking at the pet: the "watched" catch-up path used to wait almost twice as long as the "unwatched" one, so glancing at the pet actually stalled it. Catch-up is now uniform and camera-independent - if the pet falls behind while you're sprinting, it lands back in formation within half a second no matter where your camera's pointed
- Menagerie: renaming your pet is now its own page - hit "Name Tag...", type a name, then drag the tag icon onto your pet to apply it. The tag settles onto the pet with a little swing on the way down
- Closing the Menagerie now fades to black instead of cutting instantly

## 0.17.0
- Removed the original mod's non-vanilla pets for now: duck, raccoon, penguin, koi, stingray, dumbo octopus, floating head, and every April Fools joke mob (ray tracing, smiling creeper, nerd creeper, mega spud, moon cow, batato, and the rest). The catalog is entirely vanilla-backed while these get reworked properly
- Fixed the default pet on a fresh install (was "duck", now a valid vanilla pet)

## 0.16.0
- Menagerie redesign, part one: your pet now takes center stage in a big live preview with < > arrows to flip through the roster, the Land/Sky/Sea toggles and search sit right below it, and the species grid is centered underneath. Name and origin show under the stage
- The arrows browse without summoning - pick with the grid or hit Summon when you find the one
- Summon grays itself out for pets that aren't unlocked yet

## 0.15.2
- Ferry rafts now coast: when your pet is rafting after you (creative flight over water, swimming), the hull carries momentum and eases to a stop beside you instead of braking dead
- Smoother first-person sprint formation: the running position is eased so camera wiggle doesn't zigzag the pet, and it stops overshooting the mark when it's already at your side
- Shelved the original mod's custom species for now (racoon, duck, penguin, the potato crew, nerd creeper and friends) - the catalog is vanilla-backed pets only while the roster gets polished

## 0.15.1
- Animation polish pass: the nether star now grows in when the pet tucks away and shrinks out before the pet reforms, instead of popping
- The raft floats up out of the water when it appears instead of blinking into place
- The pet on a ferry raft turns smoothly with the hull instead of snapping toward the destination
- Hopping onto the boat raft now gets the same sparkle transition as the ferry

## 0.15.0
- Land pets no longer float behind your shoulder during combat - they tuck into the nether star instead, and it stays a star until the fight is over. Flyers keep the shoulder hover
- Flying pets no longer get a raft when you board a boat - they just fly along
- The raft now handles like a towed boat: the leashed bow swings around to face the rope when it goes taut, the hull turns heavily with its speed through the water, and it drifts around to face its motion when the rope slackens
- The raft sizes itself to its passenger - a fox gets the classic raft, bigger pets get a bigger deck (and a slightly longer tow rope)
- The pet on deck now turns with the hull instead of staying glued to the boat's heading

## 0.14.10
- Blazes now bounce: new air-hopping movement style (slow drift down, hops along the ground while following) instead of flying with the allay crowd
- The Menagerie shows where each pet comes from - Vanilla or a Pets&Pals addition - next to the selected pet
- Sky and Sea tabs are marked coming soon on public builds while those rosters get polished

## 0.14.9
- Fixed the raft cushion showing the whole cushion texture sheet instead of its proper faces (the cushion texture is an entity-style unwrap, not a flat tile)
- Public builds now unlock the polished pets first: Copper Golem, Fox, Cat (all 12 skins, including ocelot) and Bee - the rest of the catalog shows greyed out as "coming soon" while each species gets the full movement treatment

## 0.14.8
- Fixed disappearing pets, rafts and spawn orbs: every client-side entity was being created with the same internal id, so each new one silently deleted the last. Pets, dwellings, rafts and orbs now get their own ids
- Cushions backported from the 26.3 snapshot! Pick any of the 16 dye colors for your raft's cushion in the Menagerie (bare deck stays the default on this version)
- Menagerie: hover the raft or cushion button to see a live spinning preview of your raft
- Stairs no longer trip sprinting pets - shallow drops read as slopes, not cliffs
- Pets no longer hop onto carpets and other flat blocks they can simply walk over
- Pits deeper than 3 blocks now count as cliffs; pets stop at the edge instead of diving in
- If the pet can't catch up mid-sprint it now re-enters from behind the camera instead of lagging behind until you stop and sprint again

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
