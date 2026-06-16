package com.craftmmo.reference;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public final class ReferenceManifestLoader {
    private final ReferenceManifestParser parser = new ReferenceManifestParser();

    public String loadRawManifest() {
        try (InputStream inputStream = ReferenceManifestLoader.class.getResourceAsStream("/reference/mcmmo-2.2.053.json")) {
            if (inputStream == null) {
                throw new IllegalStateException("Reference manifest resource is missing");
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load reference manifest", ex);
        }
    }

    public ReferenceManifest load() {
        return parser.parse(loadRawManifest());
    }
}
