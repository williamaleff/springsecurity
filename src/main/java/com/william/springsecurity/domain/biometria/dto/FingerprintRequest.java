package com.william.springsecurity.domain.biometria.dto;

public class FingerprintRequest {
    private String fingerprint;

    public String getFingerprint() {
        return fingerprint;
    }

    public void setFingerprint(String fingerprint) {
        this.fingerprint = fingerprint;
    }
}