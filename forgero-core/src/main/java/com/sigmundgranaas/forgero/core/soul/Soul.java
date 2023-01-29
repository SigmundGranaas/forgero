package com.sigmundgranaas.forgero.core.soul;

import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.property.Property;
import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.state.Identifiable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Soul implements Identifiable, PropertyContainer {

    private final SoulSource soulSource;

    private final StatTracker tracker;
    private final int level;
    private final int xpTarget;
    private int xp;
    private LevelManager levelInfo;

    public Soul() {
        this.soulSource = new SoulSource("minecraft:zombie");
        this.xp = 0;
        this.level = 1;
        this.xpTarget = new LevelManager().getXpForLevel(level + 1);
        this.tracker = new StatTracker();
    }


    public Soul(int level, int xp, SoulSource source, StatTracker tracker) {
        this.soulSource = source;
        this.xp = xp;
        this.level = level;
        this.xpTarget = new LevelManager().getXpForLevel(level + 1);
        this.tracker = tracker;
    }

    public Soul(SoulSource source) {
        this.soulSource = source;
        this.xp = 0;
        this.level = 1;
        this.xpTarget = new LevelManager().getXpForLevel(level + 1);
        this.tracker = new StatTracker();
    }

    @Override
    public String name() {
        return soulSource.name();
    }

    @Override
    public String nameSpace() {
        return Forgero.NAMESPACE;
    }

    public int getXp() {
        return xp;
    }

    public Soul addXp(int xp) {
        this.xp = this.xp + xp;
        return tryLevelUp();
    }

    public int getLevel() {
        return level;
    }

    public Soul tryLevelUp() {
        if (xp >= xpTarget) {
            int remainingXp = xp - xpTarget;
            int level = this.level + 1;
            Soul leveledUp = new Soul(level, remainingXp, soulSource, tracker);
            return leveledUp.tryLevelUp();
        }
        return this;
    }

    public int getXpTarget() {
        return xpTarget;
    }

    public void trackBlock(String id, int i) {
        this.tracker.trackBlock(id, i);
    }

    public void trackMob(String id, int i) {
        this.tracker.trackMob(id, i);
    }

    public StatTracker tracker() {
        return tracker;
    }

    @Override
    public @NotNull List<Property> getRootProperties() {
        return new SoulStatToAttributes(tracker).getRootProperties();
    }
}