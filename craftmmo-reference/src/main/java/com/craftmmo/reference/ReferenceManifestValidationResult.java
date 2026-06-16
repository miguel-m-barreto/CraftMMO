package com.craftmmo.reference;

import java.util.List;

public record ReferenceManifestValidationResult(boolean valid, List<String> diagnostics) {
    public ReferenceManifestValidationResult {
        diagnostics = List.copyOf(diagnostics);
    }
}
