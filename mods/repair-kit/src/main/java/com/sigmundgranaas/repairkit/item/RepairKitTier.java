package com.sigmundgranaas.repairkit.item;

/**
 * Defines the tier of a repair kit, affecting how much durability is restored per use.
 */
public enum RepairKitTier {
    SCRAPPY("scrappy", 0.25f),
    STANDARD("standard", 0.50f),
    REFINED("refined", 0.75f),
    MASTERCRAFT("mastercraft", 1.0f);

    private final String name;
    private final float repairMultiplier;

    RepairKitTier(String name, float repairMultiplier) {
        this.name = name;
        this.repairMultiplier = repairMultiplier;
    }

    /**
     * @return The tier name used in item IDs and translations
     */
    public String getName() {
        return name;
    }

    /**
     * @return The fraction of max durability restored per repair (0.25 to 1.0)
     */
    public float getRepairMultiplier() {
        return repairMultiplier;
    }

    /**
     * Calculate how much durability to restore for an item with the given max durability.
     *
     * @param maxDurability The maximum durability of the item being repaired
     * @return The amount of durability to restore
     */
    public int calculateRepairAmount(int maxDurability) {
        return Math.max(1, (int) (maxDurability * repairMultiplier));
    }
}
