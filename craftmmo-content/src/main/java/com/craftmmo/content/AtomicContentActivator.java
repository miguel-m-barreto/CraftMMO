package com.craftmmo.content;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public final class AtomicContentActivator {
    private final ContentSnapshotValidator validator;
    private final AtomicReference<ContentSnapshot> active = new AtomicReference<>();

    public AtomicContentActivator(ContentSnapshotValidator validator) {
        this.validator = Objects.requireNonNull(validator, "validator");
    }

    public ContentValidationResult activate(ContentSnapshot snapshot) {
        ContentValidationResult result = validator.validate(snapshot);
        if (result.valid()) {
            active.set(snapshot);
        }
        return result;
    }

    public Optional<ContentSnapshot> active() {
        return Optional.ofNullable(active.get());
    }
}
