package io.github.aaronateataco.petsandpals.cloud.dto;

/** Mirrors the Worker's POST /v1/bond/claim response - creditedCoins is 0 when
 *  claimed too recently for enough bond time to have accrued yet. */
public class BondClaimResponse {
    public String error;
    public Integer creditedCoins;
    public Integer balance;
}
