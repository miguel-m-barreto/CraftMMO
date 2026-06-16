package com.craftmmo.api.skill;

import java.util.Locale;

public enum SkillId {
    ACROBATICS("acrobatics"),
    ALCHEMY("alchemy"),
    ARCHERY("archery"),
    AXES("axes"),
    CROSSBOWS("crossbows"),
    EXCAVATION("excavation"),
    FISHING("fishing"),
    HERBALISM("herbalism"),
    MACES("maces"),
    MINING("mining"),
    REPAIR("repair"),
    SALVAGE("salvage"),
    SMELTING("smelting"),
    SPEARS("spears"),
    SWORDS("swords"),
    TAMING("taming"),
    TRIDENTS("tridents"),
    UNARMED("unarmed"),
    WOODCUTTING("woodcutting");

    private final String stableId;

    SkillId(String stableId) {
        this.stableId = stableId;
    }

    public String stableId() {
        return stableId;
    }

    public static SkillId fromStableId(String stableId) {
        String normalized = stableId.toLowerCase(Locale.ROOT);
        for (SkillId skillId : values()) {
            if (skillId.stableId.equals(normalized)) {
                return skillId;
            }
        }
        throw new IllegalArgumentException("Unknown skill ID: " + stableId);
    }
}
