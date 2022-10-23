package com.sigmundgranaas.forgero.texture.utils;

import java.util.List;

public record Offset(int x, int y) {
    public static Offset of(List<Integer> offSetList) {
        if (offSetList == null || offSetList.size() < 2) {
            return new Offset(0, 0);
        }
        return new Offset(offSetList.get(0), offSetList.get(1));
    }

    public Offset apply(Offset offset) {
        return new Offset(x + offset.x(), y + offset.y);
    }
}
