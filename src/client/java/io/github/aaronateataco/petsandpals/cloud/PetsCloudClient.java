package io.github.aaronateataco.petsandpals.cloud;

import com.google.gson.Gson;
import io.github.aaronateataco.petsandpals.cloud.dto.AdoptResponse;
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

    private static <T> T send(HttpRequest request, Class<T> responseType) {
        try {
            HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
            // every response body from this API is JSON, success or error (see worker.js's
            // json() helper) - a non-2xx status still parses cleanly into the same DTO,
            // it'll just have `error` set and the success fields null/absent
            return GSON.fromJson(response.body(), responseType);
        } catch (Exception e) {
            // network failure, timeout, DNS, etc. - callers treat a null result as
            // "couldn't reach the server", not as a specific error response
            return null;
        }
    }
}
