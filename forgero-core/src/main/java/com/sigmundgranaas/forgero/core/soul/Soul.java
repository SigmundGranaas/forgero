package com.sigmundgranaas.forgero.core.soul;

import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.state.Identifiable;

public class Soul implements Identifiable, PropertyContainer {

    private final IdentifiableIntTracker blockTracker;

    private final IdentifiableIntTracker mobTracker;
    private final String name;
    private final int level;
    private final int xpTarget;
    private int xp;
    private LevelManager levelInfo;

    public Soul() {
        this.name = "unknown";
        this.xp = 0;
        this.level = 1;
        this.xpTarget = new LevelManager().getXpForLevel(level + 1);
        this.blockTracker = new IdentifiableIntTracker();
        this.mobTracker = new IdentifiableIntTracker();
    }

    public Soul(int xp) {
        this.name = "unknown";
        this.xp = xp;
        this.level = 1;
        this.xpTarget = new LevelManager().getXpForLevel(level + 1);
        this.blockTracker = new IdentifiableIntTracker();
        this.mobTracker = new IdentifiableIntTracker();
    }

    public Soul(int level, int xp, String name) {
        this.xp = xp;
        this.level = level;
        this.name = name;
        this.xpTarget = new LevelManager().getXpForLevel(level + 1);
        this.blockTracker = new IdentifiableIntTracker();
        this.mobTracker = new IdentifiableIntTracker();
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String nameSpace() {
        return Forgero.NAMESPACE;
    }

    public int getXp() {
        return xp;
    }

    public void addXp(int xp) {
        this.xp = this.xp + xp;
    }

    public int getLevel() {
        return level;
    }

    public int getXpTarget() {
        return xpTarget;
    }
}
