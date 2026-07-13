package io.github.aaronateataco.petsandpals.cloud.dto;

/**
 * Mirrors the Worker's POST /v1/pets/adopt-additional response. Covers all three
 * outcomes with one class: 201 fresh adoption (petId/species/adoptedAt/skippedCooldown),
 * 409 cooldown_active (nextEligibleAt), and 402 insufficient_currency (balance/required).
 */
public class AdoptAdditionalResponse {
    public String error;
    public String petId;
    public String species;
    public Long adoptedAt;
    public Boolean skippedCooldown;
    public Long nextEligibleAt;
    public Integer balance;
    public Integer required;
}
