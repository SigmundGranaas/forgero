package com.sigmundgranaas.forgero.core.soul;

public class LevelManager {
    public int getXpForLevel(int i) {
        int xp = 1000;
        for (int j = 0; j < i; j++) {
            xp = xp * 2;
        }
        return xp;
    }
}
