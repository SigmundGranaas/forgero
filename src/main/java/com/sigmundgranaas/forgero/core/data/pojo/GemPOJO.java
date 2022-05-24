package com.sigmundgranaas.forgero.core.data.pojo;

import com.sigmundgranaas.forgero.core.data.ForgeroDataResource;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPartTypes;

import java.util.ArrayList;
import java.util.List;

public class GemPOJO extends ForgeroDataResource {
    public String itemIdentifier;
    public List<ForgeroToolPartTypes> placement = new ArrayList<>();
    public PalettePOJO palette;
}
