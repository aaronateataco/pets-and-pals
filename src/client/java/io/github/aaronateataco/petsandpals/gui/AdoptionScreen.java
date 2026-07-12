package io.github.aaronateataco.petsandpals.gui;

import io.github.aaronateataco.petsandpals.Central;
import io.github.aaronateataco.petsandpals.PetsInitializer;
import io.github.aaronateataco.petsandpals.cloud.PetsCloudClient;
import io.github.aaronateataco.petsandpals.cloud.PlayerKeystore;
import io.github.aaronateataco.petsandpals.cloud.dto.AdoptResponse;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import static io.github.aaronateataco.petsandpals.Central.CONFIG;

/**
 * First-launch "adopt your starter pet" screen. Phase 1 is fox-only (matches the
 * mod's existing default) - a real species picker is a later phase, reusing
 * {@link MenagerieScreen}'s grid rather than duplicating it.
 *
 * <p>Requires a connection to complete a real adoption; offers an explicit
 * play-offline-for-now fallback that does NOT mark the adoption as done, so
 * {@code Central.createJoinHandler()} offers this screen again on a later join once
 * the player's actually online.
 */
public class AdoptionScreen extends Screen {

    private enum State { CONNECTING, ADOPTED, OFFLINE, ERROR }

    private final Screen parent;
    private volatile State state = State.CONNECTING;
    private Button continueButton;

    public AdoptionScreen(Screen parent) {
        super(Component.literal("Adopt your pet"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.continueButton = this.addRenderableWidget(Button.builder(Component.literal("Continue"), b -> this.finish())
                .bounds(this.width / 2 - 60, this.height / 2 + 40, 120, 20).build());
        this.continueButton.visible = false;
        this.beginAdoption();
    }

    private void beginAdoption() {
        if (this.minecraft == null || this.minecraft.player == null) {
            this.state = State.ERROR;
            return;
        }
        UUID uuid = this.minecraft.player.getUUID();
        String modVersion = FabricLoader.getInstance().getModContainer(PetsInitializer.MOD_ID)
                .map(c -> c.getMetadata().getVersion().getFriendlyString())
                .orElse("unknown");

        PetsCloudClient.adopt(uuid, "fox", modVersion).thenAccept(response ->
                Minecraft.getInstance().execute(() -> this.handleAdoptResponse(uuid, response)));
    }

    private void handleAdoptResponse(UUID uuid, AdoptResponse response) {
        if (response == null) {
            // network failure/timeout - not a server rejection, see PetsCloudClient.send()
            this.state = State.OFFLINE;
            if (this.continueButton != null) this.continueButton.visible = true;
            return;
        }
        if (response.error != null && !"already_adopted".equals(response.error)) {
            this.state = State.ERROR;
            PetsInitializer.LOGGER.error("[Pets&Pals] adoption failed: {}", response.error);
            if (this.continueButton != null) this.continueButton.visible = true;
            return;
        }

        // fresh adopt (201) carries a secret to persist; already_adopted (409) means
        // this UUID has a confirmed adoption from before (a previous install, or a
        // race with another client) - either way, the account is adopted, so treat
        // both as success. Only persist a secret when one was actually issued.
        if (response.playerSecret != null) {
            PlayerKeystore.saveSecret(uuid, response.playerSecret);
        }
        CONFIG.activePet = response.species != null ? response.species : "fox";
        CONFIG.hasAdoptedStarterPet = true;
        AutoConfig.getConfigHolder(io.github.aaronateataco.petsandpals.PetsConfig.class).save();
        this.state = State.ADOPTED;
        if (this.continueButton != null) this.continueButton.visible = true;
    }

    /** Offline fallback: play with a fox now, retry the real adoption on a later join. */
    private void playOffline() {
        CONFIG.activePet = "fox";
        // deliberately NOT hasAdoptedStarterPet = true - see class javadoc
        AutoConfig.getConfigHolder(io.github.aaronateataco.petsandpals.PetsConfig.class).save();
        this.finish();
    }

    private void finish() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }

    @Override
    public void extractRenderState(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        String message = switch (this.state) {
            case CONNECTING -> "Adopting your first pet...";
            case ADOPTED -> "Adopted! A fox will be with you shortly.";
            case OFFLINE -> "No connection right now - you can play offline for now,\nand adopt properly next time you're online.";
            case ERROR -> "Something went wrong adopting your pet. You can still\nplay offline for now.";
        };
        int y = this.height / 2 - 20;
        for (String line : message.split("\n")) {
            graphics.text(this.font, Component.literal(line),
                    this.width / 2 - this.font.width(line) / 2, y, 0xFFFFFFFF);
            y += 12;
        }
    }

    @Override
    public void onClose() {
        // closing this screen any way other than the Continue button (Esc, etc.)
        // still needs to leave the player with a playable pet - route it through the
        // same offline fallback as the explicit button. If an in-flight adopt() call
        // completes afterward, its callback still runs (Minecraft.execute doesn't
        // care whether this screen is still open) and upgrades activePet/
        // hasAdoptedStarterPet to the real adopted state at that point.
        this.playOffline();
    }
}
