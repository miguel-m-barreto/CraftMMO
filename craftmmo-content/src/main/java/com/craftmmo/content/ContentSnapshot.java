package com.craftmmo.content;

import com.craftmmo.api.skill.SkillDefinition;
import com.craftmmo.api.version.ContentVersion;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

public record ContentSnapshot(ContentVersion version, List<SkillDefinition> skills, Instant createdAt) {
    public ContentSnapshot {
        Objects.requireNonNull(version, "version");
        Objects.requireNonNull(skills, "skills");
        Objects.requireNonNull(createdAt, "createdAt");
        skills = List.copyOf(skills);
    }
}
