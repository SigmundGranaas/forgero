package com.sigmundgranaas.forgero.core.data.factory;

import com.sigmundgranaas.forgero.core.data.pojo.GemPOJO;
import com.sigmundgranaas.forgero.core.gem.ForgeroGem;
import com.sigmundgranaas.forgero.core.gem.Gem;
import com.sigmundgranaas.forgero.core.property.PropertyBuilder;

import java.util.Locale;

public class GemFactory {
    public Gem createGem(GemPOJO gemPOJO) {
        String name = gemPOJO.name.toLowerCase(Locale.ROOT);
        var properties = PropertyBuilder.createPropertyListFromPOJO(gemPOJO.properties);
        return new ForgeroGem(1, name + "_gem", properties, gemPOJO.placement);
    }
}
