package com.sigmundgranaas.forgero.core.gem.implementation;

import com.sigmundgranaas.forgero.core.gem.Gem;
import com.sigmundgranaas.forgero.core.gem.GemLoader;
import com.sigmundgranaas.forgero.core.gem.gems.AdditiveAttackDamageGemImpl;
import com.sigmundgranaas.forgero.core.gem.gems.AdditiveDurabilityGem;
import com.sigmundgranaas.forgero.core.gem.gems.AdditiveMiningSpeedGem;

import java.util.List;

public class BasicGemLoader implements GemLoader {
    @Override
    public List<Gem> loadGems() {
        return List.of(
                new AdditiveMiningSpeedGem(1, "miningspeed_gem"),
                new AdditiveAttackDamageGemImpl(1, "attackdamage_gem"),
                new AdditiveDurabilityGem(1, "durability_gem")
        );
    }
}
