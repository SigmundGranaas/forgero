package com.sigmundgranaas.forgerocore.data.v2.json;

import javax.annotation.Nullable;

public class JsonContext {
    public String fileName;

    @Nullable
    public String folder;

    @Nullable
    public String path;

    @Nullable
    public JsonResource defaults;

    public JsonContext copy() {
        var context = new JsonContext();
        context.fileName = fileName;
        context.path = path;
        context.folder = folder;
        if (defaults != null) {
            context.defaults = defaults.copy();
        }
        return context;
    }
}
