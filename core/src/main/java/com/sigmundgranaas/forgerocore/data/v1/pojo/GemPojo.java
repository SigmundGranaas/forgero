package com.sigmundgranaas.forgerocore.data.v1.pojo;

import com.sigmundgranaas.forgerocore.data.v1.ForgeroDataResource;
import com.sigmundgranaas.forgerocore.toolpart.ForgeroToolPartTypes;

import java.util.ArrayList;
import java.util.List;

public class GemPojo extends ForgeroDataResource {
    public String itemIdentifier;
    public List<ForgeroToolPartTypes> placement = new ArrayList<>();
    public PalettePojo palette;
}
