package com.sigmundgranaas.forgero.core.gem.implementation;

import com.sigmundgranaas.forgero.core.gem.GemTypes;

import java.util.List;

public class GemPOJO {
    public String name;
    public String itemIdentifier;
    public GemTypes type;
    public String effect;
    public String amount;

    public static class Palette {
        public String name;
        public List<String> include;
        public List<String> exclude;
    }
}
