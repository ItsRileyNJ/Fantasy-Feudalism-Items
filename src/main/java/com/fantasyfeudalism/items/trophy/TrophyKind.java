package com.fantasyfeudalism.items.trophy;
public enum TrophyKind {
    ZOMBIE("Resistance I; hunger drains 25% faster"),
    SKELETON("15% more ranged damage; take 15% more damage"),
    CREEPER("40% less explosion damage, 50% quieter movement, 25% less hostile detection; 20% less melee damage"),
    WITHER_SKELETON("20% chance of Wither I for 4 seconds on melee hits; lose half a heart every 30 seconds"),
    STRAY("15% more damage and Slowness I for 4 seconds (except skeletons and polar bears); take 25% more fire damage"),
    BOGGED("15% more damage and Poison I for 4 seconds (except undead); take 25% more End-related damage"),
    PIGLIN("15% more gold weapon damage, 30% less gold durability loss, +1 armor per gold armor piece; XP orbs are destroyed without Mending"),
    ENDER_DRAGON("Endermen and Endermites neutral unless attacked; Night Vision and End particles; water/rain hurt and phantoms may spawn every night"),
    PLAYER("15% more damage to players; nametag visible from 50% farther away");
    public final String tooltip;
    TrophyKind(String tooltip) { this.tooltip = tooltip; }
    public String id() { return name().toLowerCase(java.util.Locale.ROOT); }
}
