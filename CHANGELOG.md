# Changelog

## 0.39.0
- Made the Shop an actual overlay instead of a solid-looking wall: the live game world now renders behind it (it was never rendering at all before - same root cause and fix as the adoption screen's black-background bug from earlier this session, just never applied here), the dim is much lighter, and everything is grouped into one bounded panel with real margin around it instead of every button floating loose across the entire screen. No existing button moved - the panel bounds were computed from the layout that was already there, not a redo of the ~40 already-tuned positions

## 0.38.2
- Fixed the Shop's search box rendering as a plain black bar - vanilla's own EditBox border is nearly invisible against this theme's dark background, so it now gets an explicit themed panel behind it
- Fixed almost every non-public species in the catalog grid showing a broken/fallback glyph instead of a testing-only marker - traced to a Unicode alembic character (⚗) with no glyph in Minecraft's default font, replaced with plain text
- Recolored the category tabs to match reference concept art: Land/Sky/Sea each get their own distinct color (earthy orange, sky blue, teal) instead of all looking identical, tabs widened to show the category name alongside the icon instead of icon-only+tooltip. Kept the corners square rather than the concept's rounded pills, per an explicit decision to match the rest of the theme's existing chrome

## 0.38.1
- Added a "Reset Progress" button to the Shop for wiping a test account's cloud adoptions/currency/bond-claim clock and running through the whole flow again from scratch. Testing-only (same `.pnp_testing` gate the rest of the dev catalog uses) - never visible in a normal build. Requires two clicks to actually fire. Verified live against production with two synthetic test accounts side by side to confirm a reset only ever touches the account it was called for

## 0.38.0
- Reworked the Menagerie into a proper Shop layout: Land/Sky/Sea/All category tabs moved from a row under the preview to an icon-only rail down the left edge, and the right-side controls (Summon/Skin/Baby/Speed/Volume/Raft/Cushion/Advanced settings) are now explicitly labeled as the "Equip" section. Same screen, same underlying catalog/adopt-prompt logic - just reorganized
- Added a free, no-payment way to earn Paw Coins: bond-time rewards. Opening the Shop passively credits coins for real time elapsed since you last opened it (10/hour, capped per claim so returning after a long break doesn't lump-sum a huge payout). Considered a "watch ads" mechanic for this instead and looked into it properly first - real ad SDKs don't have any embedding path into a Java desktop game, and Mojang's guidelines separately restrict mods from showing unrelated third-party ad/brand content without approval, so it would've been a clearer compliance problem than the currency system already shipped, not a safer one. Bond-time rewards are fully first-party instead - no ads, no external content, reuses the mod's own already-planned (previously unused) bond-time schema
- Added the drag-a-nametag-onto-your-pet interaction to the starter adoption screen (previously Menagerie-only) - type a name, drag the tag onto the centered pet, watch it settle with the same little dangle animation
- New migration (`0003_bond_time_currency.sql`): adds `players.last_bond_claim_at` and widens the currency ledger's allowed reasons to include bond-time credits - verified live against production (including a full rebuild-in-place of the ledger table to widen its constraint, since SQLite can't ALTER a CHECK directly) with zero data loss and the existing no-overdraft trigger confirmed still intact afterward

## 0.37.0
- Added additional-pet adoption: beyond your starter pet, adopting any new species from the Menagerie now waits 3 real days since your last adoption, or can be skipped immediately by spending Paw Coins (a cloud-tracked currency with no cash-out value, bought via Stripe). Species you've already adopted stay free to switch to/from, same as before - this only gates species you've never owned. Clicking an unowned-but-unlocked catalog tile now opens an adopt prompt instead of switching instantly, showing the cooldown state and a "Buy Paw Coins" option that opens checkout in your system browser (this mod never collects payment details itself)
- New Cloudflare Worker endpoints and D1 schema (`cloudflare-worker/migrations/0002_currency_and_additional_adoptions.sql`) back this: an append-only currency ledger (balance is always derived from history, never a mutable column, for auditability), a database-level trigger that makes an overdrawn currency spend impossible even under concurrent requests, and a Stripe webhook verified via HMAC signature checking. Closed a real gap found while building this: the per-player secret issued at starter adoption was previously generated and stored but never actually checked by anything - it now authenticates every new endpoint
- Not yet wired up: live Stripe products (needs a Stripe account set up on the deploying side) and the worker isn't deployed with this change - see `cloudflare-worker/README.md`
- Fixed the bee preview pose fix from 0.36.0 only covering the in-world pet: menu previews use bee instances that are never ticked, so they always read as mid-flight the same broken way regardless of the earlier fix. Both menus now default a never-ticked preview bee to its grounded pose
- Fixed adoption sometimes reporting "no connection" on a working connection with the actual failure reason silently discarded - pinned the HTTP client to HTTP/1.1 (a common cause of Java's HttpClient failing HTTP/2 negotiation on some networks instead of falling back cleanly) and failures now log a reason
- Added a Baby/Adult toggle to the starter-pet adoption screen, next to Continue
- Fixed the fork's Maven/Modrinth publishing metadata still pointing at the original Pets Mod project's identity (org, developer, repo URL, and a stale Modrinth project id that actually resolves to the original mod, not this fork)

## 0.36.0
- Gave the mod an original dark, square-cornered UI theme (green accent sampled from the mod's own icon, not a copy of any other mod's look - built from scratch as nine-slice textures) and applied it across both the Menagerie and the starter-pet adoption screen
- Rebuilt the starter-pet adoption screen as a translucent glass-style carousel overlaying the game: the centered species renders big with its variant swatches underneath, neighboring species peek in smaller at each side with arrows (or a click) to cycle between them, and only the centered species' own tile keeps a box - there's no single panel boxing in the whole carousel
- Retheme pass on the Menagerie: every button reskinned, a themed box around the live preview stage, and small original pixel-art icons on the Land/Sky/Sea/All category tabs. Advanced Settings and the speed/volume sliders still use the old vanilla look - they're built entirely by a third-party library and vanilla's own slider widget respectively, neither with theming hooks, so reskinning either is a separate, bigger undertaking
- Fixed a real invisibility bug hit while building this: the moment adoption finishes, the game auto-summons your real fox in the background, which includes a "emerges from a sweet berry bush" spawn animation that makes the entity briefly invisible mid-animation - the adoption screen now always previews its own independent, never-spawned pet instances so the real pet's spawn animation can never make the preview vanish
- Fixed the pet previews being scaled to fill their frames edge to edge, cropping ears/tails/wide poses at the borders - they're sized with real headroom on every side now, like a normal character-select screen
- Fixed the adoption screen's variant swatches all silently showing whichever skin happened to be equipped instead of their own: picking a skin for a preview render only takes effect at the moment the game actually draws that frame, which happens after the screen code that briefly swaps the skin and swaps it back has already returned, so every swatch in the same frame was reading back the same (real) skin. Fixed by baking the intended skin into the fox's own render state at the point it's captured, instead of re-reading the shared config later
- Fixed the adoption screen's Continue button never appearing despite working correctly otherwise: it was being drawn before the screen's own background, which then painted directly over it every frame
- Fixed the adoption screen showing solid black instead of the dimmed game behind it: menu-type screens tell the game not to render the 3D scene behind them at all while open (not just freeze it, skip it outright) unless told otherwise, and since this screen opens before the world's ever rendered a single frame there was never anything for the translucent overlay to show. It now stays a genuine live, playing overlay rather than a blocking menu
- Fixed the bee's preview looking broken (wings splayed oddly, body tilted) - traced to a real, pre-existing gap: bee rendering never populated several fields vanilla's own bee animation logic depends on to know it's not mid-flight, so the model was permanently stuck posing for a flight frame that was never actually happening. The first pass only fixed the in-world pet, not the menu previews: those render independent, never-spawned bee instances, and a bee that's never actually been ticked always reads as "not on the ground" the same broken way, since nothing ever ran the physics that would say otherwise. Both menus now default a never-ticked preview bee to its grounded pose
- Fixed adoption sometimes reporting "no connection" on a working connection with zero information logged about why - the underlying HTTP client could fail to negotiate HTTP/2 on some networks (routers/CGNAT/some ISPs) instead of cleanly falling back to HTTP/1.1, and the failure was silently swallowed. Pinned the client to HTTP/1.1 (no real cost for these tiny one-off requests) and any future failure now actually logs a reason
- Added a Baby/Adult toggle to the starter-pet adoption screen, next to Continue - previews the age scale live on whichever species is centered, same global cosmetic flag the Menagerie's own toggle already used

## 0.35.0
- Added the first piece of the cloud adoption system (Phase 1): on your first join with pets on, you'll be asked to adopt a starter pet (a fox, for now) if you're online. It syncs to a small cloud backend so the adoption is confirmed server-side; if you're offline, you can play with a fox right away and it'll properly adopt next time you're connected. Once adopted, everything works fully offline as before - only the adoption itself needs a connection
- Not yet included (later phases): a real species picker beyond fox, the bond-time-with-your-pet stat, and cloud sync of anything beyond the initial adoption

## 0.34.0
- Fixed the witch pet having no working texture at all - it was pointing at "textures/entity/witch.png", which doesn't exist; vanilla moved witch's texture into its own subfolder and this was never updated to match
- Fixed rabbit skin selection doing nothing: the texture switch was checking CONFIG.activePet (always just the literal word "rabbit") instead of CONFIG.rabbitSkin, so no skin name ever matched and every rabbit rendered as the same brown fallback regardless of what you picked
- Fixed the "brown" rabbit skin's own texture path (missing the "rabbit_" prefix every other file in that folder has) and horse's "dark brown" skin, which was quietly rendering as plain "brown" instead of its own distinct texture
- Fixed the cat "ocelot" skin pointing at a texture file that doesn't exist (extra "cat_" prefix that the real file doesn't have)
- Cross-checked every hardcoded vanilla texture path across all renderers against the actual files shipped in the game - this batch is everything that didn't match

## 0.33.0
- Fixed parrot's 4th color doing nothing when selected: every other file (the /petskin command, Advanced Settings, the skin enum) calls it "cyan," but the renderer only recognized "yellow" (vanilla's real internal name for that variant) - selecting it left the parrot's texture unchanged instead of switching
- Fixed parrot's "gray" skin pointing at a texture file that doesn't exist - vanilla spells this one "grey" for parrots specifically, unlike every other gray texture in the game
- Continued the same audit that caught the shulker/wolf bugs across the renderers that use if/else chains instead of switch statements (bee, chicken, copper golem, squid, strider): none had a fallback branch, so an unrecognized or corrupted skin value left the texture path unset instead of falling back to a default like the switch-based renderers already do

## 0.32.0
- Fixed the sprint-alongside pet's whole body swaying left-right even while you're running in a dead-straight line: the formation point it steers at was aimed entirely off your head yaw (look direction), which never holds perfectly still - even a straight sprint has natural camera micro-movement. It's now aimed mostly off your actual movement direction instead, with only a little pull from where you're looking, the same balance already used for the normal (non-sprint) follow lead

## 0.31.0
- Tropical fish is now an actual, summonable pet - the mob class and its 10 named skins (cichlid, clownfish, cotton candy betta, goatfish, parrotfish, queen angelfish, red lipped blenny, tomato clownfish, triggerfish, yellowtail parrotfish) already existed in the code but were never wired into the catalog, commands, or a renderer, so it was completely unreachable. Its skins reuse vanilla's own small/large body plus tinted pattern-overlay rendering, matched against the real predefined variants vanilla itself spawns tropical fish as
- Fixed two typos in the tropical fish skin list ("chichlid" -> "cichlid", "cotten candy betta" -> "cotton candy betta")

## 0.30.0
- Fixed 3 of the shulker's 16 skin colors (blue, gray, green) silently rendering as the default undyed shulker - the texture renderer's switch was missing those cases even though they were selectable. Also added "blue" to the /petskin command's shulker list, which was missing it entirely
- Fixed opening Advanced Settings while a wolf is your active pet throwing an error and doing nothing: unlike every other pet, wolfSkin was never given a default, so a wolf that never had its skin explicitly set crashed the settings screen the instant it built its species dropdown

## 0.29.0
- Fixed pets getting stuck bobbing offshore on the ferry raft instead of actually reaching land: 0.28.0's fix for stranding in deep water accidentally required the raft to reach a spot it structurally never settles on (it always tracks the water surface, never dry ground), so it could hold forever a couple blocks short of the owner. It now lets go as soon as you're on land like before, but if the pet's still sitting in water when it does, it gets moved to the nearest dry spot next to you instead of being left to fend for itself

## 0.28.0
- Fixed the raft riding noticeably lower in the water than the boat towing it - it was copying the boat's own position directly, which sits at the bottom of the boat's hitbox rather than the waterline; now it reads the actual water surface like the ferry raft always has
- Fixed the ferry raft occasionally stranding a pet in open water even after you'd already reached dry land - it now waits until the raft itself has also reached shore (or is basically at your feet) before letting go, instead of releasing early and leaving the pet to flail
- Menagerie: fixed the golden dandelion icon overlapping the Cancel button on the naming page for baby pets, and shrank the grey backdrop panel so it doesn't loom over as much space below it
- Fixed "Advanced settings..." doing nothing: it was hard-coded to build its Pet Species dropdown around "racoon," a pet removed from the roster back in 0.17.0, which threw on every single open
- Menagerie: added a "Skin" button next to Name Tag that cycles through your active pet's skins right there, instead of needing Advanced Settings or the /petskin command

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
