package io.github.aaronateataco.petsandpals.cloud.dto;

/** Mirrors the Worker's GET /v1/pets/:uuid response. */
public class PetStateResponse {
    public String error;
    public String petId;
    public String species;
    public String nickname;
    public Long bondSeconds;
    public Long lastSyncAt;
}
