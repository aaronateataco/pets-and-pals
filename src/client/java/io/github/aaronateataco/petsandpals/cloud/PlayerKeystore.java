package io.github.aaronateataco.petsandpals.cloud;

import io.github.aaronateataco.petsandpals.PetsInitializer;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/**
 * Persists the per-player bearer secret the server hands out at adoption, kept
 * outside the human-editable petsconfig.json (nothing here belongs in a file
 * players are expected to poke at). Phase 1: secret storage only. The bond-time
 * data file this secret will eventually sign is Phase 2.
 */
public final class PlayerKeystore {

    private PlayerKeystore() {
    }

    private static Path bondDir() {
        return FabricLoader.getInstance().getConfigDir().resolve("pets-and-pals").resolve("bond");
    }

    private static Path keyFile(UUID uuid) {
        return bondDir().resolve(uuid + ".key");
    }

    public static boolean hasSecret(UUID uuid) {
        return Files.isRegularFile(keyFile(uuid));
    }

    public static String loadSecret(UUID uuid) {
        try {
            return Files.readString(keyFile(uuid), StandardCharsets.UTF_8).trim();
        } catch (IOException e) {
            return null;
        }
    }

    public static void saveSecret(UUID uuid, String secret) {
        try {
            Files.createDirectories(bondDir());
            Files.writeString(keyFile(uuid), secret, StandardCharsets.UTF_8);
        } catch (IOException e) {
            // best-effort - if this fails, the mod just retries adoption next launch
            // (hasAdoptedStarterPet only gets set true after this succeeds, see
            // AdoptionScreen) rather than crash
            PetsInitializer.LOGGER.error("[Pets&Pals] failed to save player secret", e);
        }
    }
}
