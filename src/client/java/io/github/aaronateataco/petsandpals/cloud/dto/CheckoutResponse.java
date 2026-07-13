package io.github.aaronateataco.petsandpals.cloud.dto;

/** Mirrors the Worker's POST /v1/currency/checkout response - checkoutUrl is opened
 *  in the player's system browser, never rendered in-game (no in-mod payment form). */
public class CheckoutResponse {
    public String error;
    public String checkoutUrl;
    public String sessionId;
}
