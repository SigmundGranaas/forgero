package com.sigmundgranaas.forgero.core.gem.implementation;

import com.sigmundgranaas.forgero.core.gem.Gem;
import com.sigmundgranaas.forgero.core.gem.gems.AdditiveAttackDamageGemImpl;
import com.sigmundgranaas.forgero.core.gem.gems.AdditiveDurabilityGem;

import java.util.Locale;

public class GemFactory {
    public Gem createGem(GemPOJO gemPOJO) {
        String name = gemPOJO.name.toLowerCase(Locale.ROOT);
        return switch (gemPOJO.type) {
            case DURABILITY -> new AdditiveDurabilityGem(1, name + "_gem");
            case ATTACK_DAMAGE -> new AdditiveAttackDamageGemImpl(1, name + "_gem");
            case ATTACK_SPEED -> new AdditiveDurabilityGem(1, name + "_gem");
            case MINING_SPEED -> new AdditiveAttackDamageGemImpl(1, name + "_gem");
            case MINING_LEVEL -> null;
            case EMPTY -> null;
        };

    }
}
