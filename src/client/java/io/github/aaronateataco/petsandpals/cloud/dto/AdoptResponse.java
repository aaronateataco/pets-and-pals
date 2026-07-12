package io.github.aaronateataco.petsandpals.cloud.dto;

/**
 * Mirrors the Worker's POST /v1/adopt response - both the 201 (fresh adopt) and 409
 * (already adopted) shapes overlap on petId/species/adoptedAt, so one class covers
 * both. playerSecret/bondSeconds/lastSyncToken are only ever present on a fresh
 * adopt (201) - the server intentionally never re-issues a secret on a 409, so a
 * third party who learns someone's UUID can't silently steal their session by just
 * calling adopt again.
 */
public class AdoptResponse {
    public String error;
    public String petId;
    public String species;
    public Long adoptedAt;
    public String playerSecret;
    public Long bondSeconds;
    public String lastSyncToken;
}
