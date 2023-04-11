package com.sigmundgranaas.forgero.core.soul;

import com.sigmundgranaas.forgero.core.property.Property;
import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.state.Identifiable;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

public class Soul implements Identifiable, PropertyContainer {

	private final SoulSource soulSource;

	private final StatTracker tracker;
	private final int level;
	private final int xpTarget;
	private final SoulLevelPropertyHandler levelPropertyHandler;
	private float xp;

	public Soul() {
		this.soulSource = new SoulSource("minecraft:zombie", "zombie");
		this.xp = 0;
		this.level = 1;
		this.xpTarget = new LevelManager().getXpForLevel(level + 1);
		this.tracker = new StatTracker();
		this.levelPropertyHandler = new SoulLevelPropertyHandler(new HashMap<>());
	}

	public Soul(int level, float xp, SoulSource source, StatTracker tracker, SoulLevelPropertyHandler levelPropertyHandler) {
		this.soulSource = source;
		this.xp = xp;
		this.level = level;
		this.xpTarget = new LevelManager().getXpForLevel(level + 1);
		this.tracker = tracker;
		this.levelPropertyHandler = levelPropertyHandler;
	}

	public Soul(SoulSource source, SoulLevelPropertyHandler levelPropertyHandler) {
		this.soulSource = source;
		this.xp = 0;
		this.level = 1;
		this.xpTarget = new LevelManager().getXpForLevel(level + 1);
		this.tracker = new StatTracker();
		this.levelPropertyHandler = levelPropertyHandler;
	}

	@Override
	public String name() {
		return soulSource.name();
	}

	@Override
	public String nameSpace() {
		return soulSource.nameSpace();
	}

	@Override
	public String identifier() {
		return soulSource.identifier();
	}

	public float getXp() {
		return xp;
	}

	public Soul addXp(float xp) {
		this.xp = this.xp + xp;
		return tryLevelUp();
	}

	public int getLevel() {
		return level;
	}

	public Soul tryLevelUp() {
		if (xp >= xpTarget) {
			float remainingXp = xp - xpTarget;
			int level = this.level + 1;
			Soul leveledUp = new Soul(level, remainingXp, soulSource, tracker, levelPropertyHandler);
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
	public @NotNull
	List<Property> getRootProperties() {
		List<Property> levelProps;
		if (level == 1) {
			levelProps = Collections.emptyList();
		} else {
			levelProps = levelPropertyHandler.apply(level - 1, soulSource.identifier());
		}
		var trackerProps = new SoulStatToAttributes(tracker).getRootProperties();
		return Stream.of(levelProps, trackerProps).flatMap(List::stream).toList();
	}
}
