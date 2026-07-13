package io.github.aaronateataco.petsandpals.cloud;

import com.google.gson.Gson;
import io.github.aaronateataco.petsandpals.cloud.dto.AdoptAdditionalResponse;
import io.github.aaronateataco.petsandpals.cloud.dto.AdoptResponse;
import io.github.aaronateataco.petsandpals.cloud.dto.CheckoutResponse;
import io.github.aaronateataco.petsandpals.cloud.dto.CurrencyBalanceResponse;
import io.github.aaronateataco.petsandpals.cloud.dto.OwnedPetsResponse;
import io.github.aaronateataco.petsandpals.cloud.dto.PetStateResponse;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Talks to the Pets&amp;Pals cloud API (Cloudflare Worker + D1). Every call here
 * runs on {@link #EXECUTOR} - never call these from the render thread and block on
 * the result. Callers are responsible for hopping back with
 * {@code Minecraft.getInstance().execute(...)} before touching any client-thread
 * state (CONFIG, screens, entities), the same pattern already used for the
 * tick/join handlers in Central.java.
 */
public final class PetsCloudClient {

    private static final String BASE_URL = "https://pets-and-pals-api.zeraoralegendary.workers.dev";
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(6);

    private static final Gson GSON = new Gson();
    private static final ExecutorService EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            // pinned instead of the default HTTP/2-with-downgrade negotiation: a
            // handful of players reported "no connection" while their connection was
            // fine, and Java's HttpClient failing HTTP/2 ALPN negotiation (common
            // behind certain routers/CGNAT/some ISPs) rather than cleanly falling
            // back to HTTP/1.1 is a well-known cause of exactly that symptom. Every
            // request here is a single small JSON call, so there's no real perf cost
            // to giving up HTTP/2 multiplexing.
            .version(HttpClient.Version.HTTP_1_1)
            .build();

    private PetsCloudClient() {
    }

    public static CompletableFuture<AdoptResponse> adopt(UUID uuid, String species, String clientModVersion) {
        Map<String, String> body = new HashMap<>();
        body.put("uuid", uuid.toString());
        body.put("species", species);
        body.put("clientModVersion", clientModVersion);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/v1/adopt"))
                .timeout(REQUEST_TIMEOUT)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(body)))
                .build();

        return CompletableFuture.supplyAsync(() -> send(request, AdoptResponse.class), EXECUTOR);
    }

    public static CompletableFuture<PetStateResponse> getPet(UUID uuid) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/v1/pets/" + uuid))
                .timeout(REQUEST_TIMEOUT)
                .GET()
                .build();

        return CompletableFuture.supplyAsync(() -> send(request, PetStateResponse.class), EXECUTOR);
    }

    /** Every species this UUID has ever adopted (starter included) - drives the
     *  Menagerie's "already owned, free to switch to" vs. "needs adopting" gate. */
    public static CompletableFuture<OwnedPetsResponse> getOwnedPets(UUID uuid) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/v1/pets/mine/" + uuid))
                .timeout(REQUEST_TIMEOUT)
                .GET()
                .build();

        return CompletableFuture.supplyAsync(() -> send(request, OwnedPetsResponse.class), EXECUTOR);
    }

    /** Adopts a species beyond the player's starter pet - server-gated by a 3-day
     *  cooldown since their last non-starter adoption, unless {@code skipCooldown} is
     *  true and they have enough currency (see the Worker's no-overdraft trigger for
     *  why an insufficient-balance skip request can never partially succeed). */
    public static CompletableFuture<AdoptAdditionalResponse> adoptAdditional(
            UUID uuid, String secret, String species, boolean skipCooldown, String clientModVersion) {
        Map<String, Object> body = new HashMap<>();
        body.put("uuid", uuid.toString());
        body.put("secret", secret);
        body.put("species", species);
        body.put("skipCooldown", skipCooldown);
        body.put("clientModVersion", clientModVersion);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/v1/pets/adopt-additional"))
                .timeout(REQUEST_TIMEOUT)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(body)))
                .build();

        return CompletableFuture.supplyAsync(() -> send(request, AdoptAdditionalResponse.class), EXECUTOR);
    }

    /** Current Paw Coin balance - authenticated via the same per-player secret
     *  {@link PlayerKeystore} already persists from starter adoption, since a GET
     *  can't carry a JSON body the way every other call here does. */
    public static CompletableFuture<CurrencyBalanceResponse> getCurrencyBalance(UUID uuid, String secret) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/v1/currency/balance/" + uuid))
                .timeout(REQUEST_TIMEOUT)
                .header("X-Player-Secret", secret)
                .GET()
                .build();

        return CompletableFuture.supplyAsync(() -> send(request, CurrencyBalanceResponse.class), EXECUTOR);
    }

    /** Starts a Stripe Checkout session for the given currency pack ("small"/"medium"/
     *  "large" - see worker.js's CURRENCY_PACKS). The returned checkoutUrl is meant to
     *  be opened in the player's system browser, never rendered in-game - this mod
     *  never collects payment details itself. */
    public static CompletableFuture<CheckoutResponse> createCurrencyCheckout(UUID uuid, String secret, String packId) {
        Map<String, String> body = new HashMap<>();
        body.put("uuid", uuid.toString());
        body.put("secret", secret);
        body.put("packId", packId);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/v1/currency/checkout"))
                .timeout(REQUEST_TIMEOUT)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(body)))
                .build();

        return CompletableFuture.supplyAsync(() -> send(request, CheckoutResponse.class), EXECUTOR);
    }

    private static <T> T send(HttpRequest request, Class<T> responseType) {
        try {
            HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
            // every response body from this API is JSON, success or error (see worker.js's
            // json() helper) - a non-2xx status still parses cleanly into the same DTO,
            // it'll just have `error` set and the success fields null/absent
            return GSON.fromJson(response.body(), responseType);
        } catch (Exception e) {
            // network failure, timeout, DNS, etc. - callers treat a null result as
            // "couldn't reach the server", not as a specific error response. Logged
            // (previously silent) so a false "no connection" report is actually
            // diagnosable from the player's log instead of a total guess.
            io.github.aaronateataco.petsandpals.PetsInitializer.LOGGER.warn(
                    "[Pets&Pals] cloud request to {} failed: {}", request.uri(), e.toString());
            return null;
        }
    }
}
