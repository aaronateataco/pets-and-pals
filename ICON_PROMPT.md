# Pets&Pals — mod icon generation prompt

Paste the prompt below into ChatGPT (with image generation) **together with three attachments**:
1. your baby chicken photo (the subject),
2. the Friends & Foes mod icon (primary style reference),
3. the Villages & Pillages mod icon (secondary style reference).

When you get a result you like, downscale/export it as a square PNG and replace
`src/main/resources/assets/pets-and-pals/icon.png` (and use the big version for the Modrinth page).

---

## Prompt

Create a square 1024x1024 Minecraft mod icon for a mod called "Pets&Pals".

**Subject:** a baby chicken (chick), based on the attached chick photo, but redesigned as a
cute *Minecraft-style* creature — blocky/cuboid head and body like an official Minecraft mob,
NOT a realistic bird. Big head, small body, tiny blocky wings, simple dot eyes, small flat
beak. It should read instantly as "adorable Minecraft pet". Front-facing bust (head and upper
body), centered, looking slightly toward the viewer.

**Style — match the two attached mod icons (Friends & Foes, Villages & Pillages):**
- soft, high-quality 3D render of a blocky Minecraft-style model with gentle painterly
  shading and subtle ambient occlusion — the "modern Minecraft marketing art" look
- warm, saturated but not neon colors; soft studio lighting from the upper left
- the mob fills most of the frame, slightly cropped at the bottom like a portrait bust
- simple rounded-square background in a single soft color (a warm pastel yellow-orange that
  compliments the chick) with a very subtle radial glow behind the subject
- crisp silhouette; a faint darker outline or rim light so it pops as a small icon
- NO text, NO watermark, NO border frame, nothing else in the scene

**Mood:** friendly, loyal companion — like a Fortnite pet/back-bling portrait but Minecraft.

The icon must stay readable at 64x64: bold shapes, high contrast between subject and
background, no fine details that disappear when scaled down.

---

## Variations worth asking for in the same chat
- "Same icon but the chick has a tiny red collar" (pet identity)
- "Same icon but add a small heart particle floating beside its head"
- "Same style but zoomed out enough to show its little feet"

## Also needed later, same style (title screen art)
- a wide "Pets&Pals" logo/wordmark texture to replace
  `assets/pets-and-pals/textures/title/petsmod.png` and `modpets.png`
  (the in-game custom title screen). Ask: "Using the same style, render the text
  'Pets&Pals' as a Minecraft-style logo with the chick perched on the ampersand."
