package io.github.aaronateataco.petsandpals.cloud.dto;

import java.util.List;

/** Mirrors the Worker's GET /v1/pets/mine/:uuid response - every species this UUID
 *  has ever adopted (starter included), used to gate the Menagerie catalog. */
public class OwnedPetsResponse {
    public String error;
    public String uuid;
    public List<String> species;
    public Long lastNonStarterAdoptionAt;
}
