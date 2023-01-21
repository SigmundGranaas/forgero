package com.sigmundgranaas.forgero.core.soul;

public class StatTracker {
    private final IdentifiableIntTracker blockTracker;
    private final IdentifiableIntTracker mobTracker;

    public StatTracker() {
        this.blockTracker = new IdentifiableIntTracker();
        this.mobTracker = new IdentifiableIntTracker();
    }

    public StatTracker(IdentifiableIntTracker blockTracker, IdentifiableIntTracker mobTracker) {
        this.blockTracker = blockTracker;
        this.mobTracker = mobTracker;
    }

    public void trackBlock(String id, int i) {
        this.blockTracker.add(id, i);
    }

    public void trackMob(String id, int i) {
        this.mobTracker.add(id, i);
    }

    public IdentifiableIntTracker blocks() {
        return blockTracker;
    }

    public IdentifiableIntTracker mobs() {
        return mobTracker;
    }
}
