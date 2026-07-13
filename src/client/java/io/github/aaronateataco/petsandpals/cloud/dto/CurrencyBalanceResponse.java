package io.github.aaronateataco.petsandpals.cloud.dto;

/** Mirrors the Worker's GET /v1/currency/balance/:uuid response. */
public class CurrencyBalanceResponse {
    public String error;
    public String uuid;
    public Integer balance;
}
