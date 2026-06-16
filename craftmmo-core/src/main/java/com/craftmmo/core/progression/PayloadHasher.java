package com.craftmmo.core.progression;

import com.craftmmo.api.identity.PayloadHash;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;
import java.util.TreeMap;

public final class PayloadHasher {
    private PayloadHasher() {
    }

    public static PayloadHash sha256(Map<String, String> canonicalPayload) {
        TreeMap<String, String> sorted = new TreeMap<>(canonicalPayload);
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> entry : sorted.entrySet()) {
            builder.append(entry.getKey()).append('=').append(entry.getValue()).append('\n');
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return new PayloadHash(HexFormat.of().formatHex(digest.digest(builder.toString().getBytes(StandardCharsets.UTF_8))));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }
}
