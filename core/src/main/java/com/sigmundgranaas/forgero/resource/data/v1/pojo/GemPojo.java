package com.sigmundgranaas.forgero.resource.data.v1.pojo;

import com.sigmundgranaas.forgero.resource.data.v1.ForgeroDataResource;
import com.sigmundgranaas.forgero.toolpart.ForgeroToolPartTypes;

import java.util.ArrayList;
import java.util.List;

public class GemPojo extends ForgeroDataResource {
    public String itemIdentifier;
    public List<ForgeroToolPartTypes> placement = new ArrayList<>();
    public PalettePojo palette;
}
