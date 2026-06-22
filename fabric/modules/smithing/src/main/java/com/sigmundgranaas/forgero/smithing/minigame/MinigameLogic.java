package com.sigmundgranaas.forgero.smithing.minigame;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.minecraft.common.match.MinecraftContextKeys;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;
import com.sigmundgranaas.forgero.smithing.condition.PredicateConditionLootRegistry;
import com.sigmundgranaas.forgero.smithing.item.custom.MorphedItem;
import com.sigmundgranaas.forgero.smithing.temperature.TemperatureProfile;
import com.sigmundgranaas.forgero.smithing.temperature.TemperatureRules;
import com.sigmundgranaas.forgero.smithing.temperature.TemperatureRules.TemperatureStage;
import com.sigmundgranaas.forgero.smithing.temperature.TemperatureState;
import com.sigmundgranaas.forgero.smithing.util.SchematicMaterialCost;

import lombok.Getter;
import lombok.Setter;

import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.world.World;

@Getter
public class MinigameLogic {
	public static final int INITIAL_MARKER_DELAY_TICKS = 25;
	public static final int SUBSEQUENT_MARKER_DELAY_TICKS = 15;
	public static final int MARKER_LIFETIME_TICKS_NORMAL = 35;
	public static final int MARKER_LIFETIME_TICKS_COOLING = 20;
	public static final int TOTAL_MARKERS = 10;
	public static final int MAX_MARKERS = 15;
	public static final int COOLING_MARKERS = 3;
	public static final int ONE_MATERIAL_REQUIRED_HITS = 7;
	public static final int TWO_MATERIAL_REQUIRED_HITS = 10;
	public static final int THREE_MATERIAL_REQUIRED_HITS = 12;
	public static final int FOUR_MATERIAL_REQUIRED_HITS = 15;
	public static final int MAX_MISSES_BEFORE_RUINED = 5;
	public static final int MARKER_TIMEOUT_COOLING = 10;
	public static final int NORMAL_MARKER_HEAT_CHANGE = 40;
	public static final int COOLING_MARKER_HEAT_CHANGE = -25;
	public static final int PERFECT_NORMAL_MARKER_HEAT_CHANGE = 25;
	public static final int POOR_NORMAL_MARKER_HEAT_CHANGE = 55;
	public static final int PERFECT_COOLING_MARKER_HEAT_CHANGE = -40;
	public static final int POOR_COOLING_MARKER_HEAT_CHANGE = -10;
	private static final int STAGE_COUNT = 5;
	private static final int STRIKE_QUALITY_COUNT = 3;

	private static final double MARKER_HIT_RADIUS_SQ = 0.0075d;
	private static final double PERFECT_TIMING_START = 0.35d;
	private static final double PERFECT_TIMING_END = 0.65d;
	private static final double GOOD_TIMING_START = 0.20d;
	private static final double GOOD_TIMING_END = 0.80d;
	private static final double COOLING_MARKER_UPPER_WORKABLE_START = 0.65d;
	private static final double COOLING_MARKER_MAX_CHANCE = 0.75d;

	private static final String HITS_NBT_KEY = "forgero_markerHitsCount";
	private static final String ATTEMPTS_NBT_KEY = "forgero_markerAttempts";
	private static final String COOLING_MARKER_HITS_NBT_KEY = "forgero_coolingMarkerHits";
	private static final String MISS_MARKER_NBT_KEY = "forgero_missMarkerHits";
	private static final String REQUIRED_HITS_NBT_KEY = "forgero_required_hits";
	private static final String FAILED_HEAT_STAGE_COUNTS_NBT_KEY = "forgero_failedHeatStageCounts";
	private static final String STRIKE_QUALITY_COUNTS_NBT_KEY = "forgero_strikeQualityCounts";
	private static final String COOLING_STRIKE_QUALITY_COUNTS_NBT_KEY = "forgero_coolingStrikeQualityCounts";
	private static final String COOLING_STAGE_COUNTS_NBT_KEY = "forgero_coolingStageCounts";
	private static final String CURRENT_PERFECT_STREAK_NBT_KEY = "forgero_currentPerfectStrikeStreak";
	private static final String BEST_PERFECT_STREAK_NBT_KEY = "forgero_bestPerfectStrikeStreak";
	private static final String CURRENT_SKILLED_STREAK_NBT_KEY = "forgero_currentSkilledStrikeStreak";
	private static final String BEST_SKILLED_STREAK_NBT_KEY = "forgero_bestSkilledStrikeStreak";

	private final List<Vec2f> markerPositions = new ArrayList<>();
	private final List<Boolean> markerHits = new ArrayList<>();
	private final List<Integer> hitStageIndices = new ArrayList<>();
	private final List<Integer> coolingMarkerIndices = new ArrayList<>();
	private final int[] failedHeatStageCounts = new int[STAGE_COUNT];
	private final int[] strikeQualityCounts = new int[STRIKE_QUALITY_COUNT];
	private final int[] coolingStrikeQualityCounts = new int[STRIKE_QUALITY_COUNT];
	private final int[] coolingStageCounts = new int[STAGE_COUNT];
	private final Random random = new Random();

	private int coolingMarkerHits = 0;
	private int missMarkerHits = 0;
	private int currentPerfectStrikeStreak = 0;
	private int bestPerfectStrikeStreak = 0;
	private int currentSkilledStrikeStreak = 0;
	private int bestSkilledStrikeStreak = 0;

	@Setter
	private int markerAttempts = 0;

	@Setter
	private int markerHitsCount = 0;

	private int requiredHits = ONE_MATERIAL_REQUIRED_HITS;

	private int markerTimeout = 0;
	private int markerSpawnDelay;

	private enum MarkerOutcome {
		HIT,
		MISS,
		TIMEOUT
	}

	public enum StrikeQuality {
		POOR,
		GOOD,
		PERFECT
	}

	public interface MinigameCallback {
		void markDirty();

		void playHitEffect(Vec2f markerLocalPos, StrikeQuality quality);

		default void playHitEffect(Vec2f markerLocalPos) {
			playHitEffect(markerLocalPos, StrikeQuality.GOOD);
		}

		void playMissEffect();

		void spawnMarkerAppearanceEffect(Vec2f markerLocalPos, ItemStack itemStack);

		World getWorld();

		BlockPos getPos();

		BlockState getCachedState();

		ItemStack getCurrentStack();

		void replaceWithResult(ItemStack resultStack);
	}

	public MinigameLogic() {
		this.markerSpawnDelay = INITIAL_MARKER_DELAY_TICKS;
	}

	public static int getRequiredHits(ItemStack stack) {
		int materialCost = SchematicMaterialCost.DEFAULT_MATERIAL_COST;

		if (!stack.isEmpty() && stack.hasNbt()) {
			NbtCompound nbt = stack.getNbt();

			if (nbt != null && nbt.contains(SchematicMaterialCost.MATERIAL_COST_KEY)) {
				materialCost = nbt.getInt(SchematicMaterialCost.MATERIAL_COST_KEY);
			}
		}

		return getRequiredHitsForMaterialCost(materialCost);
	}

	public static int getRequiredHitsForMaterialCost(int materialCost) {
		if (materialCost <= 1) {
			return ONE_MATERIAL_REQUIRED_HITS;
		}

		if (materialCost == 2) {
			return TWO_MATERIAL_REQUIRED_HITS;
		}

		if (materialCost == 3) {
			return THREE_MATERIAL_REQUIRED_HITS;
		}

		return FOUR_MATERIAL_REQUIRED_HITS;
	}

	public void resetMarkerProgress(MinigameCallback callback) {
		ItemStack stack = callback.getCurrentStack();
		updateRequiredHits(stack);

		markerPositions.clear();
		markerHits.clear();

		markerAttempts = 0;
		markerHitsCount = 0;
		coolingMarkerHits = 0;
		missMarkerHits = 0;
		clearFailedHeatStageCounts();
		clearStrikeQualityCounts();
		clearCoolingStageCounts();
		clearStrikeStreaks();
		markerTimeout = 0;
		markerSpawnDelay = INITIAL_MARKER_DELAY_TICKS;

		coolingMarkerIndices.clear();

		if (!stack.isEmpty() && stack.getItem() instanceof MorphedItem) {
			NbtCompound nbt = stack.getOrCreateNbt();

			if (nbt.contains(HITS_NBT_KEY) || nbt.contains(ATTEMPTS_NBT_KEY)) {
				this.markerHitsCount = nbt.getInt(HITS_NBT_KEY);
				this.markerAttempts = this.markerHitsCount;
				this.coolingMarkerHits = nbt.getInt(COOLING_MARKER_HITS_NBT_KEY);
				this.missMarkerHits = nbt.contains(MISS_MARKER_NBT_KEY)
						? nbt.getInt(MISS_MARKER_NBT_KEY)
						: 0;

				hitStageIndices.clear();

				if (nbt.contains("hitStageIndices")) {
					int[] arr = nbt.getIntArray("hitStageIndices");

					for (int v : arr) {
						hitStageIndices.add(v);
					}
				}

				readFailedHeatStageCounts(nbt);
				readStrikeQualityCounts(nbt);
				readStrikeStreaks(nbt);

				if (nbt.contains("coolingMarkerIndices")) {
					coolingMarkerIndices.clear();

					int[] arr = nbt.getIntArray("coolingMarkerIndices");

					for (int idx : arr) {
						if (idx >= 0 && idx < requiredHits) {
							coolingMarkerIndices.add(idx);
						}
					}
				}

				readCoolingStageCounts(nbt);
			} else {
				this.coolingMarkerHits = 0;
				this.missMarkerHits = 0;
				clearFailedHeatStageCounts();
				clearStrikeQualityCounts();
				clearCoolingStageCounts();
				clearStrikeStreaks();
				hitStageIndices.clear();
			}
		} else {
			this.coolingMarkerHits = 0;
			this.missMarkerHits = 0;
			clearFailedHeatStageCounts();
			clearStrikeQualityCounts();
			clearCoolingStageCounts();
			clearStrikeStreaks();
			hitStageIndices.clear();
		}

		updateMorphProgressOnItem(stack);
		callback.markDirty();
	}

	public void clearMarkerProgress() {
		markerPositions.clear();
		markerHits.clear();

		markerAttempts = 0;
		markerHitsCount = 0;
		coolingMarkerHits = 0;
		missMarkerHits = 0;
		clearFailedHeatStageCounts();
		clearStrikeQualityCounts();
		clearCoolingStageCounts();
		clearStrikeStreaks();
		markerTimeout = 0;
		markerSpawnDelay = INITIAL_MARKER_DELAY_TICKS;

		coolingMarkerIndices.clear();
		hitStageIndices.clear();
		requiredHits = ONE_MATERIAL_REQUIRED_HITS;
	}

	public boolean processHit(Vec2f itemLocalHit, MinigameCallback callback) {
		if (markerPositions.size() != 1) {
			return false;
		}

		Vec2f marker = markerPositions.get(0);
		return marker.distanceSquared(itemLocalHit) < MARKER_HIT_RADIUS_SQ;
	}

	public void processMarkerAttempt(boolean hit, MinigameCallback callback) {
		processMarkerAttempt(hit, true, callback);
	}

	public void processMarkerAttempt(boolean hit, boolean wasPlayerAttempt, MinigameCallback callback) {
		MarkerOutcome outcome = hit
				? MarkerOutcome.HIT
				: wasPlayerAttempt ? MarkerOutcome.MISS : MarkerOutcome.TIMEOUT;

		processMarkerAttempt(outcome, callback);
	}

	private void processMarkerAttempt(MarkerOutcome outcome, MinigameCallback callback) {
		if (isComplete()) {
			return;
		}

		ItemStack stack = callback.getCurrentStack();

		if (stack.isEmpty()) {
			clearActiveMarker();
			callback.markDirty();
			return;
		}

		if (MorphedItem.isRuined(stack)) {
			clearActiveMarker();
			callback.markDirty();
			return;
		}

		if (MorphedItem.needsQuench(stack)) {
			clearActiveMarker();
			markerSpawnDelay = 0;
			callback.markDirty();
			return;
		}

		updateRequiredHits(stack);

		switch (outcome) {
			case HIT -> {
				int markerIndex = markerHitsCount;
				applySuccessfulMarkerHit(markerIndex, stack, callback);
			}
			case MISS -> applyFailedMarkerAttempt();
			case TIMEOUT -> applyMarkerTimeout(stack);
		}

		boolean ruined = ruinIfMissLimitReached(stack);

		clearActiveMarker();

		markerSpawnDelay = isComplete() || ruined
				? 0
				: SUBSEQUENT_MARKER_DELAY_TICKS;

		updateMorphProgressOnItem(stack);
		saveProgressToItem(stack);

		callback.markDirty();

		if (!ruined) {
			finishIfComplete(callback);
		}
	}

	private boolean ruinIfMissLimitReached(ItemStack stack) {
		if (isComplete() || MorphedItem.isRuined(stack) || !(stack.getItem() instanceof MorphedItem)) {
			return false;
		}

		if (missMarkerHits < MAX_MISSES_BEFORE_RUINED) {
			return false;
		}

		MorphedItem.markRuined(stack);
		return true;
	}

	private void applySuccessfulMarkerHit(
			int markerIndex,
			ItemStack stack,
			MinigameCallback callback
	) {
		int temperature = TemperatureState.currentTemperature(stack);
		TemperatureProfile profile = TemperatureProfile.from(stack);
		TemperatureRules.TemperatureStages stages = TemperatureRules.stages(profile);
		TemperatureStage stage = TemperatureRules.stage(temperature, stages);
		int stageIndex = stageIndexFor(stage);
		boolean coolingMarker = coolingMarkerIndices.contains(markerIndex);
		int baseTempChange = coolingMarker ? COOLING_MARKER_HEAT_CHANGE : NORMAL_MARKER_HEAT_CHANGE;

		if (!canShapeAtTemperature(stage)) {
			failedHeatStageCounts[stageIndex]++;
			missMarkerHits++;
			resetActiveStrikeStreaks();

			callback.playMissEffect();
			applyTemperatureChange(stack, profile, temperature, baseTempChange);
			return;
		}

		StrikeQuality quality = strikeQualityFor(coolingMarker);
		int tempChange = heatChangeFor(coolingMarker, quality, stage, temperature, stages);
		recordStrikeQuality(quality, coolingMarker);

		markerHitsCount++;

		/*
		 * markerAttempts now tracks successful marker progress only.
		 * Misses, timeouts, and bad-temperature blows do not advance this.
		 */
		markerAttempts = markerHitsCount;

		if (!markerPositions.isEmpty()) {
			markerHits.set(0, true);
			callback.playHitEffect(markerPositions.get(0), quality);
		}

		hitStageIndices.add(stageIndex);

		applyTemperatureChange(stack, profile, temperature, tempChange);

		if (coolingMarker) {
			coolingMarkerHits++;
			coolingStageCounts[stageIndex]++;
		}
	}

	private boolean canShapeAtTemperature(TemperatureStage stage) {
		return stage == TemperatureStage.HOT
				|| stage == TemperatureStage.WORKABLE
				|| stage == TemperatureStage.OVERHEATED;
	}

	private StrikeQuality strikeQualityFor(boolean coolingMarker) {
		int lifetime = markerLifetime(coolingMarker);

		if (lifetime <= 0) {
			return StrikeQuality.GOOD;
		}

		int elapsed = Math.max(0, Math.min(lifetime, lifetime - markerTimeout));
		double timing = elapsed / (double) lifetime;

		if (timing >= PERFECT_TIMING_START && timing <= PERFECT_TIMING_END) {
			return StrikeQuality.PERFECT;
		}

		if (timing >= GOOD_TIMING_START && timing <= GOOD_TIMING_END) {
			return StrikeQuality.GOOD;
		}

		return StrikeQuality.POOR;
	}

	private int markerLifetime(boolean coolingMarker) {
		return coolingMarker ? MARKER_LIFETIME_TICKS_COOLING : MARKER_LIFETIME_TICKS_NORMAL;
	}

	private void recordStrikeQuality(StrikeQuality quality, boolean coolingMarker) {
		int index = strikeQualityIndex(quality);
		strikeQualityCounts[index]++;

		if (coolingMarker) {
			coolingStrikeQualityCounts[index]++;
		}

		updateStrikeStreaks(quality);
	}

	private void updateStrikeStreaks(StrikeQuality quality) {
		if (quality == StrikeQuality.PERFECT) {
			currentPerfectStrikeStreak++;
		} else {
			currentPerfectStrikeStreak = 0;
		}

		if (quality == StrikeQuality.POOR) {
			currentSkilledStrikeStreak = 0;
		} else {
			currentSkilledStrikeStreak++;
		}

		bestPerfectStrikeStreak = Math.max(bestPerfectStrikeStreak, currentPerfectStrikeStreak);
		bestSkilledStrikeStreak = Math.max(bestSkilledStrikeStreak, currentSkilledStrikeStreak);
	}

	private void resetActiveStrikeStreaks() {
		currentPerfectStrikeStreak = 0;
		currentSkilledStrikeStreak = 0;
	}

	private int heatChangeFor(
			boolean coolingMarker,
			StrikeQuality quality,
			TemperatureStage stage,
			int temperature,
			TemperatureRules.TemperatureStages stages
	) {
		int baseHeatChange = switch (quality) {
			case PERFECT -> coolingMarker ? PERFECT_COOLING_MARKER_HEAT_CHANGE : PERFECT_NORMAL_MARKER_HEAT_CHANGE;
			case GOOD -> coolingMarker ? COOLING_MARKER_HEAT_CHANGE : NORMAL_MARKER_HEAT_CHANGE;
			case POOR -> coolingMarker ? POOR_COOLING_MARKER_HEAT_CHANGE : POOR_NORMAL_MARKER_HEAT_CHANGE;
		};

		if (!coolingMarker) {
			return baseHeatChange;
		}

		return baseHeatChange - coolingRewardBonus(stage, quality, temperature, stages);
	}

	private int coolingRewardBonus(
			TemperatureStage stage,
			StrikeQuality quality,
			int temperature,
			TemperatureRules.TemperatureStages stages
	) {
		if (stage == TemperatureStage.WORKABLE && heatPressureWithWorkableRange(temperature, stages) > 0.0d) {
			return switch (quality) {
				case PERFECT -> 12;
				case GOOD -> 8;
				case POOR -> 4;
			};
		}

		return switch (stage) {
			case OVERHEATED -> switch (quality) {
				case PERFECT -> 30;
				case GOOD -> 25;
				case POOR -> 15;
			};
			case HOT -> switch (quality) {
				case PERFECT -> 15;
				case GOOD -> 10;
				case POOR -> 5;
			};
			default -> 0;
		};
	}

	private boolean selectCoolingMarkerForNextMarker(ItemStack stack) {
		int markerIndex = markerHitsCount;

		if (!shouldOfferCoolingMarker(stack)) {
			coolingMarkerIndices.remove(Integer.valueOf(markerIndex));
			return false;
		}

		if (!coolingMarkerIndices.contains(markerIndex)) {
			coolingMarkerIndices.add(markerIndex);
		}

		return true;
	}

	private boolean shouldOfferCoolingMarker(ItemStack stack) {
		if (coolingMarkerIndices.size() >= maxCoolingMarkers()) {
			return false;
		}

		double pressure = heatPressure(stack);

		if (pressure <= 0.0d) {
			return false;
		}

		return pressure >= 1.0d || random.nextDouble() < pressure;
	}

	private int maxCoolingMarkers() {
		return Math.min(COOLING_MARKERS, Math.max(0, requiredHits - 1));
	}

	private double heatPressure(ItemStack stack) {
		if (stack.isEmpty() || !TemperatureRules.canTrackTemperature(stack)) {
			return 0.0d;
		}

		int temperature = TemperatureState.currentTemperature(stack);
		TemperatureRules.TemperatureStages stages = TemperatureRules.stages(stack);

		if (stages.workableStart > TemperatureState.DEFAULT_TEMPERATURE
				&& stages.workableEnd > stages.workableStart) {
			return heatPressureWithWorkableRange(temperature, stages);
		}

		return switch (TemperatureRules.stage(temperature, stages)) {
			case OVERHEATED -> 1.0d;
			case HOT -> 0.4d;
			default -> 0.0d;
		};
	}

	private double heatPressureWithWorkableRange(
			int temperature,
			TemperatureRules.TemperatureStages stages
	) {
		if (temperature < stages.workableStart) {
			return 0.0d;
		}

		if (temperature > stages.workableEnd
				|| temperature + NORMAL_MARKER_HEAT_CHANGE > stages.workableEnd) {
			return 1.0d;
		}

		double workableProgress = (temperature - stages.workableStart)
				/ (double) (stages.workableEnd - stages.workableStart);

		if (workableProgress < COOLING_MARKER_UPPER_WORKABLE_START) {
			return 0.0d;
		}

		double upperProgress = (workableProgress - COOLING_MARKER_UPPER_WORKABLE_START)
				/ (1.0d - COOLING_MARKER_UPPER_WORKABLE_START);

		return Math.min(COOLING_MARKER_MAX_CHANCE, 0.25d + upperProgress * 0.5d);
	}

	private void applyTemperatureChange(
			ItemStack stack,
			TemperatureProfile profile,
			int temperature,
			int tempChange
	) {
		TemperatureRules.setTemperature(
				stack,
				Math.max(0, Math.min(temperature + tempChange, profile.maxTemperature()))
		);
	}

	private void applyFailedMarkerAttempt() {
		missMarkerHits++;
		resetActiveStrikeStreaks();
	}

	private void applyMarkerTimeout(ItemStack stack) {
		resetActiveStrikeStreaks();

		int temperature = TemperatureState.currentTemperature(stack);

		if (temperature <= TemperatureState.DEFAULT_TEMPERATURE) {
			return;
		}

		TemperatureRules.setTemperature(
				stack,
				Math.max(TemperatureState.DEFAULT_TEMPERATURE, temperature - MARKER_TIMEOUT_COOLING)
		);
	}

	private void finishIfComplete(MinigameCallback callback) {
		if (!isComplete()) {
			return;
		}

		ItemStack stack = callback.getCurrentStack();

		if (!stack.isEmpty() && stack.getItem() instanceof MorphedItem) {
			setMorphProgress(1.0, callback);
		}
	}

	private int stageIndexFor(TemperatureStage stage) {
		return switch (stage) {
			case COLD -> 0;
			case WARM -> 1;
			case HOT -> 2;
			case WORKABLE -> 3;
			case OVERHEATED -> 4;
		};
	}

	public void setMarkerHit(int index, MinigameCallback callback) {
		World world = callback.getWorld();

		if (world != null && !world.isClient && index >= 0 && index < markerHits.size()) {
			markerHits.set(index, true);
			callback.playHitEffect(markerPositions.get(index));
			clearActiveMarker();
			callback.markDirty();
		}
	}

	public void tick(MinigameCallback callback) {
		ItemStack stackForMarker = callback.getCurrentStack();
		updateRequiredHits(stackForMarker);

		if (MorphedItem.isRuined(stackForMarker)) {
			boolean changed = !markerPositions.isEmpty()
					|| markerTimeout != 0
					|| markerSpawnDelay != 0;

			clearActiveMarker();
			markerSpawnDelay = 0;

			if (changed) {
				callback.markDirty();
			}

			return;
		}

		if (MorphedItem.needsQuench(stackForMarker)) {
			boolean changed = !markerPositions.isEmpty()
					|| markerTimeout != 0
					|| markerSpawnDelay != 0;

			clearActiveMarker();
			markerSpawnDelay = 0;

			if (changed) {
				callback.markDirty();
			}

			return;
		}

		boolean activeMorph = isMorphingActive(stackForMarker);

		if (!activeMorph || stackForMarker.isEmpty()) {
			if (!markerPositions.isEmpty() || markerSpawnDelay > 0) {
				clearMarkerProgress();
				callback.markDirty();
			}

			return;
		}

		if (isComplete()) {
			finishIfComplete(callback);
			return;
		}

		if (markerPositions.isEmpty()) {
			if (markerSpawnDelay > 0) {
				markerSpawnDelay--;
			}

			if (markerSpawnDelay == 0) {
				Vec2f marker = nextMarkerPosition(stackForMarker, callback);

				if (marker.equals(Vec2f.ZERO)) {
					markerSpawnDelay = SUBSEQUENT_MARKER_DELAY_TICKS;
					return;
				}

				boolean coolingMarker = selectCoolingMarkerForNextMarker(stackForMarker);

				markerPositions.add(marker);
				markerHits.add(false);

				markerTimeout = coolingMarker
						? MARKER_LIFETIME_TICKS_COOLING
						: MARKER_LIFETIME_TICKS_NORMAL;

				callback.markDirty();
				callback.spawnMarkerAppearanceEffect(marker, stackForMarker);
			}
		} else {
			markerTimeout--;

			if (markerTimeout <= 0) {
				processMarkerAttempt(false, false, callback);
			}
		}
	}

	public boolean isComplete() {
		return markerHitsCount >= requiredHits;
	}

	public void setMorphProgress(double progress, MinigameCallback callback) {
		ItemStack stack = callback.getCurrentStack();

		if (!stack.isEmpty() && stack.getItem() instanceof MorphedItem && !MorphedItem.isRuined(stack)) {
			if (progress >= 1.0) {
				MorphedItem.setMorphProgress(stack, 1.0);
				MorphedItem.markNeedsQuench(stack);
				updateRequiredHits(stack);
				markerHitsCount = Math.max(markerHitsCount, requiredHits);
				markerAttempts = markerHitsCount;
				saveProgressToItem(stack);
				clearActiveMarker();
				markerSpawnDelay = 0;
			} else if (!MorphedItem.needsQuench(stack)) {
				MorphedItem.setMorphProgress(stack, Math.max(0.0, progress));
			}
		}

		callback.markDirty();
	}

	public static ItemStack finalizeAfterQuenchIfReady(ItemStack stack, World world, BlockPos pos) {
		if (!isReadyForQuenchFinalization(stack)) {
			return stack;
		}

		MinigameLogic logic = new MinigameLogic();
		logic.restoreFromItemNbt(stack);

		ItemStack resultStack = createFinalResultStack(
				stack,
				logic.createMatchContext(world, pos, stack)
		);

		if (resultStack.isEmpty()) {
			return stack;
		}

		return resultStack;
	}

	private static boolean isReadyForQuenchFinalization(ItemStack stack) {
		if (!MorphedItem.needsQuench(stack)) {
			return false;
		}

		return TemperatureState.currentTemperature(stack) <= TemperatureState.DEFAULT_TEMPERATURE;
	}

	private static ItemStack createFinalResultStack(
			ItemStack stack,
			MatchContext context
	) {
		ItemStack storedResultStack = MorphedItem.getResultStack(stack);
		Item resultItem = storedResultStack.isEmpty() ? MorphedItem.getResultItem(stack) : storedResultStack.getItem();

		if (resultItem == null) {
			return ItemStack.EMPTY;
		}

		ItemStack resultStack = storedResultStack.isEmpty()
				? new ItemStack(resultItem, stack.getCount())
				: storedResultStack.copy();
		resultStack.setCount(stack.getCount());

		resultStack = applyCondition(resultStack, context);

		TemperatureProfile.from(stack).writeTo(resultStack);
		TemperatureState.copyFrom(stack, resultStack);

		return resultStack;
	}

	private static ItemStack applyCondition(
			ItemStack resultStack,
			MatchContext context
	) {
		var stateOpt = StateService.INSTANCE.convert(resultStack);

		if (stateOpt.isEmpty()
				|| !(stateOpt.get() instanceof com.sigmundgranaas.forgero.core.condition.Conditional<?> conditional)) {
			return resultStack;
		}

		var state = stateOpt.get();

		com.sigmundgranaas.forgero.core.condition.NamedCondition directCondition =
				PredicateConditionLootRegistry.getConditions(context).stream()
						.filter(condition -> condition.matches(state))
						.findFirst()
						.orElse(null);

		if (directCondition != null) {
			var conditioned = conditional.applyCondition(directCondition);
			var newStackOpt = StateService.INSTANCE.convert(
					(com.sigmundgranaas.forgero.core.state.State) conditioned
			);

			return newStackOpt.orElse(resultStack);
		}
		return resultStack;
	}

	private MatchContext createMatchContext(MinigameCallback callback, ItemStack stack) {
		return createMatchContext(callback.getWorld(), callback.getPos(), stack);
	}

	private MatchContext createMatchContext(World world, BlockPos pos, ItemStack stack) {
		MatchContext context = MatchContext.of();

		if (world != null) {
			context = context.put(MinecraftContextKeys.WORLD, world);
			context = context.put(MinecraftContextKeys.BLOCK_TARGET, pos);
		}

		context = context.put(MinecraftContextKeys.STACK, stack);
		int[] stageCounts = countStageHits();
		int totalStageHits = Arrays.stream(stageCounts).sum();

		context = context.put(MinecraftContextKeys.TOTAL_HITS, Math.max(markerHitsCount, totalStageHits));
		context = context.put(MinecraftContextKeys.MISS_HITS, missMarkerHits);
		context = context.put(MinecraftContextKeys.COOLING_MARKER_HITS, coolingMarkerHits);
		context = context.put(MinecraftContextKeys.QUENCH_COUNT, TemperatureState.quenchCount(stack));
		context = context.put(MinecraftContextKeys.REHEAT_COUNT, TemperatureState.reheatCount(stack));
		context = putStrikeQualityContext(context);
		context = putQuenchContext(context, stack);

		context = context.put(MinecraftContextKeys.COLD_STAGE_HITS, stageCounts[0]);
		context = context.put(MinecraftContextKeys.WARM_STAGE_HITS, stageCounts[1]);
		context = context.put(MinecraftContextKeys.HOT_STAGE_HITS, stageCounts[2]);
		context = context.put(MinecraftContextKeys.WORKABLE_STAGE_HITS, stageCounts[3]);
		context = context.put(MinecraftContextKeys.OVERHEATED_STAGE_HITS, stageCounts[4]);
		context = putCoolingStageContext(context);

		int stageFractionDenominator = Math.max(1, totalStageHits);
		context = context.put(MinecraftContextKeys.COLD_STAGE_FRACTION, stageCounts[0] / (double) stageFractionDenominator);
		context = context.put(MinecraftContextKeys.WARM_STAGE_FRACTION, stageCounts[1] / (double) stageFractionDenominator);
		context = context.put(MinecraftContextKeys.HOT_STAGE_FRACTION, stageCounts[2] / (double) stageFractionDenominator);
		context = context.put(MinecraftContextKeys.WORKABLE_STAGE_FRACTION, stageCounts[3] / (double) stageFractionDenominator);
		context = context.put(MinecraftContextKeys.OVERHEATED_STAGE_FRACTION, stageCounts[4] / (double) stageFractionDenominator);
		context = context.put(MinecraftContextKeys.STAGE_TRANSITION_MATRIX, buildStageTransitionMatrix());
		context = context.put(
				MinecraftContextKeys.STAGE_CHANGE_SEQUENCE,
				hitStageIndices.stream().mapToInt(Integer::intValue).toArray()
		);

		return context;
	}

	private MatchContext putStrikeQualityContext(MatchContext context) {
		int poor = strikeQualityCounts[strikeQualityIndex(StrikeQuality.POOR)];
		int good = strikeQualityCounts[strikeQualityIndex(StrikeQuality.GOOD)];
		int perfect = strikeQualityCounts[strikeQualityIndex(StrikeQuality.PERFECT)];
		int total = Math.max(1, poor + good + perfect);

		context = context.put(MinecraftContextKeys.POOR_STRIKES, poor);
		context = context.put(MinecraftContextKeys.GOOD_STRIKES, good);
		context = context.put(MinecraftContextKeys.PERFECT_STRIKES, perfect);
		context = context.put(MinecraftContextKeys.POOR_STRIKE_FRACTION, poor / (double) total);
		context = context.put(MinecraftContextKeys.GOOD_STRIKE_FRACTION, good / (double) total);
		context = context.put(MinecraftContextKeys.PERFECT_STRIKE_FRACTION, perfect / (double) total);
		context = context.put(MinecraftContextKeys.PERFECT_STRIKE_STREAK, bestPerfectStrikeStreak);
		context = context.put(MinecraftContextKeys.SKILLED_STRIKE_STREAK, bestSkilledStrikeStreak);

		context = context.put(
				MinecraftContextKeys.POOR_COOLING_STRIKES,
				coolingStrikeQualityCounts[strikeQualityIndex(StrikeQuality.POOR)]
		);
		context = context.put(
				MinecraftContextKeys.GOOD_COOLING_STRIKES,
				coolingStrikeQualityCounts[strikeQualityIndex(StrikeQuality.GOOD)]
		);
		context = context.put(
				MinecraftContextKeys.PERFECT_COOLING_STRIKES,
				coolingStrikeQualityCounts[strikeQualityIndex(StrikeQuality.PERFECT)]
		);

		return context;
	}

	private MatchContext putCoolingStageContext(MatchContext context) {
		context = context.put(MinecraftContextKeys.COOLING_COLD_STAGE_HITS, coolingStageCounts[0]);
		context = context.put(MinecraftContextKeys.COOLING_WARM_STAGE_HITS, coolingStageCounts[1]);
		context = context.put(MinecraftContextKeys.COOLING_HOT_STAGE_HITS, coolingStageCounts[2]);
		context = context.put(MinecraftContextKeys.COOLING_WORKABLE_STAGE_HITS, coolingStageCounts[3]);
		context = context.put(MinecraftContextKeys.COOLING_OVERHEATED_STAGE_HITS, coolingStageCounts[4]);
		context = context.put(MinecraftContextKeys.COOLING_STAGE_HIT_COUNTS, Arrays.copyOf(coolingStageCounts, coolingStageCounts.length));

		return context;
	}

	private MatchContext putQuenchContext(MatchContext context, ItemStack stack) {
		context = context.put(MinecraftContextKeys.TOTAL_QUENCH_SESSIONS, SmithingRewardData.totalQuenchSessions(stack));
		context = context.put(MinecraftContextKeys.IN_PROGRESS_QUENCH_SESSIONS, SmithingRewardData.inProgressQuenchSessions(stack));
		context = context.put(MinecraftContextKeys.FINAL_QUENCH_SESSIONS, SmithingRewardData.finalQuenchSessions(stack));
		context = context.put(MinecraftContextKeys.FINAL_QUENCH_START_TEMPERATURE, SmithingRewardData.finalQuenchStartTemperature(stack));
		context = context.put(MinecraftContextKeys.FINAL_QUENCH_START_STAGE, SmithingRewardData.finalQuenchStartStage(stack));
		context = context.put(MinecraftContextKeys.LAST_QUENCH_START_TEMPERATURE, SmithingRewardData.lastQuenchStartTemperature(stack));
		context = context.put(MinecraftContextKeys.LAST_QUENCH_START_STAGE, SmithingRewardData.lastQuenchStartStage(stack));
		context = context.put(MinecraftContextKeys.FINAL_QUENCH_COMPLETED_IN_ONE_GO, SmithingRewardData.finalQuenchCompletedInOneGo(stack));
		context = context.put(MinecraftContextKeys.QUENCHED_DURING_SMITHING, SmithingRewardData.quenchedDuringSmithing(stack));
		context = context.put(MinecraftContextKeys.QUENCH_START_TEMPERATURES, SmithingRewardData.quenchStartTemperatures(stack));
		context = context.put(MinecraftContextKeys.QUENCH_START_STAGES, SmithingRewardData.quenchStartStages(stack));
		context = context.put(MinecraftContextKeys.QUENCH_CONTEXT_SEQUENCE, SmithingRewardData.quenchContextSequence(stack));

		return context;
	}

	private int[] countStageHits() {
		int[] counts = new int[STAGE_COUNT];

		for (int stage : hitStageIndices) {
			if (stage >= 0 && stage < STAGE_COUNT) {
				counts[stage]++;
			}
		}

		for (int i = 0; i < STAGE_COUNT; i++) {
			counts[i] += failedHeatStageCounts[i];
		}

		return counts;
	}

	private int[] buildStageTransitionMatrix() {
		int[] matrix = new int[STAGE_COUNT * STAGE_COUNT];

		for (int i = 1; i < hitStageIndices.size(); i++) {
			int previous = hitStageIndices.get(i - 1);
			int current = hitStageIndices.get(i);

			if (previous >= 0 && previous < STAGE_COUNT && current >= 0 && current < STAGE_COUNT) {
				matrix[previous * STAGE_COUNT + current]++;
			}
		}

		return matrix;
	}

	public void saveProgressToItem(ItemStack stack) {
		if (stack.isEmpty()) {
			return;
		}

		if (!(stack.getItem() instanceof MorphedItem)) {
			return;
		}

		updateMorphProgressOnItem(stack);

		NbtCompound itemNbt = stack.getOrCreateNbt();

		itemNbt.putInt(HITS_NBT_KEY, markerHitsCount);
		itemNbt.putInt(ATTEMPTS_NBT_KEY, markerAttempts);
		itemNbt.putInt(MISS_MARKER_NBT_KEY, missMarkerHits);
		itemNbt.putInt(COOLING_MARKER_HITS_NBT_KEY, coolingMarkerHits);

		itemNbt.putIntArray(
				"coolingMarkerIndices",
				coolingMarkerIndices.stream().mapToInt(Integer::intValue).toArray()
		);

		itemNbt.putIntArray(
				"hitStageIndices",
				hitStageIndices.stream().mapToInt(Integer::intValue).toArray()
		);

		itemNbt.putIntArray(
				FAILED_HEAT_STAGE_COUNTS_NBT_KEY,
				failedHeatStageCounts
		);
		itemNbt.putIntArray(STRIKE_QUALITY_COUNTS_NBT_KEY, strikeQualityCounts);
		itemNbt.putIntArray(COOLING_STRIKE_QUALITY_COUNTS_NBT_KEY, coolingStrikeQualityCounts);
		itemNbt.putIntArray(COOLING_STAGE_COUNTS_NBT_KEY, coolingStageCounts);
		itemNbt.putInt(CURRENT_PERFECT_STREAK_NBT_KEY, currentPerfectStrikeStreak);
		itemNbt.putInt(BEST_PERFECT_STREAK_NBT_KEY, bestPerfectStrikeStreak);
		itemNbt.putInt(CURRENT_SKILLED_STREAK_NBT_KEY, currentSkilledStrikeStreak);
		itemNbt.putInt(BEST_SKILLED_STREAK_NBT_KEY, bestSkilledStrikeStreak);

	}
	
	public void writeNbt(NbtCompound nbt) {
		NbtCompound markersNbt = new NbtCompound();

		for (int i = 0; i < markerPositions.size(); i++) {
			Vec2f pos = markerPositions.get(i);
			NbtCompound markerNbt = new NbtCompound();

			markerNbt.putFloat("x", pos.x);
			markerNbt.putFloat("y", pos.y);
			markerNbt.putBoolean("hit", markerHits.size() > i && markerHits.get(i));

			markersNbt.put("marker_" + i, markerNbt);
		}

		nbt.put("markers", markersNbt);
		nbt.putInt(HITS_NBT_KEY, markerHitsCount);
		nbt.putInt(ATTEMPTS_NBT_KEY, markerAttempts);
		nbt.putInt(COOLING_MARKER_HITS_NBT_KEY, coolingMarkerHits);
		nbt.putInt(MISS_MARKER_NBT_KEY, missMarkerHits);
		nbt.putInt(REQUIRED_HITS_NBT_KEY, requiredHits);
		nbt.putIntArray(
				"hitStageIndices",
				hitStageIndices.stream().mapToInt(Integer::intValue).toArray()
		);
		nbt.putIntArray(
				"coolingMarkerIndices",
				coolingMarkerIndices.stream().mapToInt(Integer::intValue).toArray()
		);
		nbt.putIntArray(FAILED_HEAT_STAGE_COUNTS_NBT_KEY, failedHeatStageCounts);
		nbt.putIntArray(STRIKE_QUALITY_COUNTS_NBT_KEY, strikeQualityCounts);
		nbt.putIntArray(COOLING_STRIKE_QUALITY_COUNTS_NBT_KEY, coolingStrikeQualityCounts);
		nbt.putIntArray(COOLING_STAGE_COUNTS_NBT_KEY, coolingStageCounts);
		nbt.putInt(CURRENT_PERFECT_STREAK_NBT_KEY, currentPerfectStrikeStreak);
		nbt.putInt(BEST_PERFECT_STREAK_NBT_KEY, bestPerfectStrikeStreak);
		nbt.putInt(CURRENT_SKILLED_STREAK_NBT_KEY, currentSkilledStrikeStreak);
		nbt.putInt(BEST_SKILLED_STREAK_NBT_KEY, bestSkilledStrikeStreak);
		nbt.putDouble("morphProgress", getMorphProgress());
	}

	public void readNbt(NbtCompound nbt) {
		markerPositions.clear();
		markerHits.clear();

		if (nbt.contains("markers", NbtCompound.COMPOUND_TYPE)) {
			NbtCompound markersNbt = nbt.getCompound("markers");

			for (int i = 0; i < MAX_MARKERS; i++) {
				if (markersNbt.contains("marker_" + i)) {
					NbtCompound markerNbt = markersNbt.getCompound("marker_" + i);

					markerPositions.add(new Vec2f(
							markerNbt.getFloat("x"),
							markerNbt.getFloat("y")
					));
					markerHits.add(markerNbt.getBoolean("hit"));
				}
			}
		}

		markerHitsCount = nbt.contains(HITS_NBT_KEY)
				? nbt.getInt(HITS_NBT_KEY)
				: markerHitsCount;

		markerAttempts = markerHitsCount;

		requiredHits = nbt.contains(REQUIRED_HITS_NBT_KEY)
				? Math.max(ONE_MATERIAL_REQUIRED_HITS, Math.min(MAX_MARKERS, nbt.getInt(REQUIRED_HITS_NBT_KEY)))
				: requiredHits;

		coolingMarkerHits = nbt.contains(COOLING_MARKER_HITS_NBT_KEY)
				? nbt.getInt(COOLING_MARKER_HITS_NBT_KEY)
				: coolingMarkerHits;

		missMarkerHits = nbt.contains(MISS_MARKER_NBT_KEY)
				? nbt.getInt(MISS_MARKER_NBT_KEY)
				: nbt.contains("missHits") ? nbt.getInt("missHits") : 0;

		hitStageIndices.clear();

		if (nbt.contains("hitStageIndices")) {
			int[] arr = nbt.getIntArray("hitStageIndices");

			for (int v : arr) {
				hitStageIndices.add(v);
			}
		}

		readFailedHeatStageCounts(nbt);
		readStrikeQualityCounts(nbt);
		readStrikeStreaks(nbt);

		coolingMarkerIndices.clear();

		if (nbt.contains("coolingMarkerIndices")) {
			int[] arr = nbt.getIntArray("coolingMarkerIndices");

			for (int idx : arr) {
				if (idx >= 0 && idx < requiredHits) {
					coolingMarkerIndices.add(idx);
				}
			}
		}

		readCoolingStageCounts(nbt);

	}

	public void restoreFromItemNbt(ItemStack stack) {
		updateRequiredHits(stack);

		if (stack.isEmpty() || !(stack.getItem() instanceof MorphedItem)) {
			this.markerHitsCount = 0;
			this.markerAttempts = 0;
			this.coolingMarkerHits = 0;
			this.missMarkerHits = 0;
			this.requiredHits = ONE_MATERIAL_REQUIRED_HITS;
			this.hitStageIndices.clear();
			clearFailedHeatStageCounts();
			clearStrikeQualityCounts();
			clearCoolingStageCounts();
			clearStrikeStreaks();
			this.coolingMarkerIndices.clear();
			return;
		}

		if (!stack.hasNbt()) {
			this.markerHitsCount = 0;
			this.markerAttempts = 0;
			this.coolingMarkerHits = 0;
			this.missMarkerHits = 0;
			this.requiredHits = ONE_MATERIAL_REQUIRED_HITS;
			this.hitStageIndices.clear();
			clearFailedHeatStageCounts();
			clearStrikeQualityCounts();
			clearCoolingStageCounts();
			clearStrikeStreaks();
			this.coolingMarkerIndices.clear();
			return;
		}

		NbtCompound itemNbt = stack.getNbt();

		if (itemNbt == null) {
			return;
		}

		this.markerHitsCount = itemNbt.getInt(HITS_NBT_KEY);
		this.markerAttempts = this.markerHitsCount;

		this.missMarkerHits = itemNbt.contains(MISS_MARKER_NBT_KEY)
				? itemNbt.getInt(MISS_MARKER_NBT_KEY)
				: 0;

		hitStageIndices.clear();

		if (itemNbt.contains("hitStageIndices")) {
			int[] arr = itemNbt.getIntArray("hitStageIndices");

			for (int v : arr) {
				hitStageIndices.add(v);
			}
		}

		readFailedHeatStageCounts(itemNbt);
		readStrikeQualityCounts(itemNbt);
		readStrikeStreaks(itemNbt);

		this.coolingMarkerHits = itemNbt.contains(COOLING_MARKER_HITS_NBT_KEY)
				? itemNbt.getInt(COOLING_MARKER_HITS_NBT_KEY)
				: 0;

		coolingMarkerIndices.clear();

		if (itemNbt.contains("coolingMarkerIndices")) {
			int[] arr = itemNbt.getIntArray("coolingMarkerIndices");

			for (int idx : arr) {
				if (idx >= 0 && idx < requiredHits) {
					coolingMarkerIndices.add(idx);
				}
			}
		}

		readCoolingStageCounts(itemNbt);

	}

	public double getMorphProgress() {
		if (requiredHits <= 0) {
			return 0.0;
		}

		return Math.min(1.0, (double) markerHitsCount / requiredHits);
	}

	public double getMorphProgress(ItemStack stack) {
		int stackRequiredHits = getRequiredHits(stack);

		if (stackRequiredHits <= 0) {
			return 0.0;
		}

		return Math.min(1.0, (double) markerHitsCount / stackRequiredHits);
	}

	public void refreshRequiredHits(ItemStack stack) {
		updateRequiredHits(stack);
	}

	private void clearActiveMarker() {
		markerPositions.clear();
		markerHits.clear();
		markerTimeout = 0;
	}

	private boolean isMorphingActive(ItemStack stack) {
		return stack.getItem() instanceof MorphedItem
				&& MorphedItem.getMorphProgress(stack) < 1.0
				&& !MorphedItem.isRuined(stack)
				&& !MorphedItem.needsQuench(stack);
	}

	private Vec2f nextMarkerPosition(ItemStack stackForMarker, MinigameCallback callback) {
		if (stackForMarker.getItem() instanceof MorphedItem
				&& callback instanceof com.sigmundgranaas.forgero.smithing.block.entity.custom.SmithingAnvilBlockEntity anvilEntity) {
			Vec2f marker = MinigamePositioning.getRandomMarkerPositionMorphed(anvilEntity);

			if (!marker.equals(Vec2f.ZERO)) {
				return marker;
			}
		}

		return MinigamePositioning.getRandomMarkerPosition(
				stackForMarker,
				callback.getCachedState()
		);
	}

	private void updateMorphProgressOnItem(ItemStack stack) {
		if (stack.isEmpty()) {
			return;
		}

		if (stack.getItem() instanceof MorphedItem) {
			if (MorphedItem.isRuined(stack) || MorphedItem.needsQuench(stack)) {
				return;
			}

			updateRequiredHits(stack);
			stack.getOrCreateNbt().putDouble(MorphedItem.PROGRESS_KEY, getMorphProgress());
		}
	}

	private void updateRequiredHits(ItemStack stack) {
		requiredHits = getRequiredHits(stack);
	}

	private void clearFailedHeatStageCounts() {
		Arrays.fill(failedHeatStageCounts, 0);
	}

	private void clearStrikeQualityCounts() {
		Arrays.fill(strikeQualityCounts, 0);
		Arrays.fill(coolingStrikeQualityCounts, 0);
	}

	private void clearCoolingStageCounts() {
		Arrays.fill(coolingStageCounts, 0);
	}

	private void clearStrikeStreaks() {
		currentPerfectStrikeStreak = 0;
		bestPerfectStrikeStreak = 0;
		currentSkilledStrikeStreak = 0;
		bestSkilledStrikeStreak = 0;
	}

	private void readFailedHeatStageCounts(NbtCompound nbt) {
		clearFailedHeatStageCounts();

		if (!nbt.contains(FAILED_HEAT_STAGE_COUNTS_NBT_KEY)) {
			return;
		}

		int[] counts = nbt.getIntArray(FAILED_HEAT_STAGE_COUNTS_NBT_KEY);

		for (int i = 0; i < Math.min(counts.length, failedHeatStageCounts.length); i++) {
			failedHeatStageCounts[i] = Math.max(0, counts[i]);
		}
	}

	private void readStrikeQualityCounts(NbtCompound nbt) {
		clearStrikeQualityCounts();
		readIntCounts(nbt, STRIKE_QUALITY_COUNTS_NBT_KEY, strikeQualityCounts);
		readIntCounts(nbt, COOLING_STRIKE_QUALITY_COUNTS_NBT_KEY, coolingStrikeQualityCounts);
	}

	private void readCoolingStageCounts(NbtCompound nbt) {
		clearCoolingStageCounts();

		if (nbt.contains(COOLING_STAGE_COUNTS_NBT_KEY)) {
			readIntCounts(nbt, COOLING_STAGE_COUNTS_NBT_KEY, coolingStageCounts);
			return;
		}

		rebuildCoolingStageCountsFromHitHistory();
	}

	private void rebuildCoolingStageCountsFromHitHistory() {
		for (int hitIndex = 0; hitIndex < hitStageIndices.size(); hitIndex++) {
			if (!coolingMarkerIndices.contains(hitIndex)) {
				continue;
			}

			int stage = hitStageIndices.get(hitIndex);

			if (stage >= 0 && stage < STAGE_COUNT) {
				coolingStageCounts[stage]++;
			}
		}
	}

	private void readStrikeStreaks(NbtCompound nbt) {
		currentPerfectStrikeStreak = readNonNegativeInt(nbt, CURRENT_PERFECT_STREAK_NBT_KEY);
		bestPerfectStrikeStreak = Math.max(
				readNonNegativeInt(nbt, BEST_PERFECT_STREAK_NBT_KEY),
				currentPerfectStrikeStreak
		);
		currentSkilledStrikeStreak = readNonNegativeInt(nbt, CURRENT_SKILLED_STREAK_NBT_KEY);
		bestSkilledStrikeStreak = Math.max(
				readNonNegativeInt(nbt, BEST_SKILLED_STREAK_NBT_KEY),
				currentSkilledStrikeStreak
		);
	}

	private void readIntCounts(NbtCompound nbt, String key, int[] target) {
		if (!nbt.contains(key)) {
			return;
		}

		int[] counts = nbt.getIntArray(key);

		for (int i = 0; i < Math.min(counts.length, target.length); i++) {
			target[i] = Math.max(0, counts[i]);
		}
	}

	private int readNonNegativeInt(NbtCompound nbt, String key) {
		return nbt.contains(key) ? Math.max(0, nbt.getInt(key)) : 0;
	}

	private int strikeQualityIndex(StrikeQuality quality) {
		return Math.max(0, Math.min(STRIKE_QUALITY_COUNT - 1, quality.ordinal()));
	}

}
