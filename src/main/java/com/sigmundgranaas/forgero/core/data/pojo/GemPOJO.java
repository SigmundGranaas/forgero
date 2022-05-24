package com.sigmundgranaas.forgero.core.data.pojo;

import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPartTypes;

import java.util.List;

public class GemPOJO {
    public String name;
    public String itemIdentifier;
    public List<ForgeroToolPartTypes> placement;
    public Palette palette;
    public PropertyPOJO properties;

    public static class Palette {
        public String name;
        public List<String> include;
        public List<String> exclude;
    }
}
