package com.craftmmo.skills;

import com.craftmmo.api.skill.ReferenceStatus;
import com.craftmmo.api.skill.SkillCategory;
import com.craftmmo.api.skill.SkillDefinition;
import com.craftmmo.api.skill.SkillId;
import com.craftmmo.api.skill.SkillRegistry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class DefaultSkillRegistry implements SkillRegistry {
    private final Map<SkillId, SkillDefinition> definitions;
    private final List<SkillDefinition> snapshot;

    private DefaultSkillRegistry(Collection<SkillDefinition> definitions) {
        EnumMap<SkillId, SkillDefinition> byId = new EnumMap<>(SkillId.class);
        for (SkillDefinition definition : definitions) {
            SkillDefinition previous = byId.putIfAbsent(definition.id(), definition);
            if (previous != null) {
                throw new IllegalArgumentException("Duplicate skill definition: " + definition.id().stableId());
            }
        }
        for (SkillId skillId : SkillId.values()) {
            if (!byId.containsKey(skillId)) {
                throw new IllegalArgumentException("Missing skill definition: " + skillId.stableId());
            }
        }
        this.definitions = Map.copyOf(byId);
        this.snapshot = byId.values().stream()
                .sorted(Comparator.comparing(definition -> definition.id().stableId()))
                .toList();
    }

    public static DefaultSkillRegistry create() {
        List<SkillDefinition> definitions = new ArrayList<>();
        add(definitions, SkillId.ACROBATICS, "Acrobatics", SkillCategory.UTILITY);
        add(definitions, SkillId.ALCHEMY, "Alchemy", SkillCategory.UTILITY);
        add(definitions, SkillId.ARCHERY, "Archery", SkillCategory.COMBAT);
        add(definitions, SkillId.AXES, "Axes", SkillCategory.COMBAT);
        add(definitions, SkillId.CROSSBOWS, "Crossbows", SkillCategory.COMBAT);
        add(definitions, SkillId.EXCAVATION, "Excavation", SkillCategory.GATHERING);
        add(definitions, SkillId.FISHING, "Fishing", SkillCategory.GATHERING);
        add(definitions, SkillId.HERBALISM, "Herbalism", SkillCategory.GATHERING);
        add(definitions, SkillId.MACES, "Maces", SkillCategory.COMBAT);
        add(definitions, SkillId.MINING, "Mining", SkillCategory.GATHERING);
        add(definitions, SkillId.REPAIR, "Repair", SkillCategory.UTILITY);
        addChild(definitions, SkillId.SALVAGE, "Salvage", SkillId.REPAIR, SkillId.FISHING);
        addChild(definitions, SkillId.SMELTING, "Smelting", SkillId.MINING, SkillId.REPAIR);
        add(definitions, SkillId.SPEARS, "Spears", SkillCategory.COMBAT);
        add(definitions, SkillId.SWORDS, "Swords", SkillCategory.COMBAT);
        add(definitions, SkillId.TAMING, "Taming", SkillCategory.COMBAT);
        add(definitions, SkillId.TRIDENTS, "Tridents", SkillCategory.COMBAT);
        add(definitions, SkillId.UNARMED, "Unarmed", SkillCategory.COMBAT);
        add(definitions, SkillId.WOODCUTTING, "Woodcutting", SkillCategory.GATHERING);
        return new DefaultSkillRegistry(definitions);
    }

    public static DefaultSkillRegistry fromDefinitions(Collection<SkillDefinition> definitions) {
        return new DefaultSkillRegistry(List.copyOf(Objects.requireNonNull(definitions, "definitions")));
    }

    private static void add(List<SkillDefinition> definitions, SkillId id, String displayName, SkillCategory category) {
        definitions.add(new SkillDefinition(id, displayName, category, true, false, List.of(), ReferenceStatus.NEEDS_REVIEW));
    }

    private static void addChild(List<SkillDefinition> definitions, SkillId id, String displayName, SkillId first, SkillId second) {
        definitions.add(new SkillDefinition(id, displayName, SkillCategory.CHILD, true, true, List.of(first, second), ReferenceStatus.NEEDS_REVIEW));
    }

    @Override
    public Collection<SkillDefinition> all() {
        return snapshot;
    }

    @Override
    public Optional<SkillDefinition> find(SkillId id) {
        return Optional.ofNullable(definitions.get(id));
    }
}
