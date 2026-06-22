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
	private static final int STAGE_COUNT = 5;

	private static final double MARKER_HIT_RADIUS_SQ = 0.0075d;

	private static final String HITS_NBT_KEY = "forgero_markerHitsCount";
	private static final String ATTEMPTS_NBT_KEY = "forgero_markerAttempts";
	private static final String COOLING_MARKER_HITS_NBT_KEY = "forgero_coolingMarkerHits";
	private static final String MISS_MARKER_NBT_KEY = "forgero_missMarkerHits";
	private static final String REQUIRED_HITS_NBT_KEY = "forgero_required_hits";
	private static final String FAILED_HEAT_STAGE_COUNTS_NBT_KEY = "forgero_failedHeatStageCounts";
	private static final Random RESULT_RANDOM = new Random();

	private final List<Vec2f> markerPositions = new ArrayList<>();
	private final List<Boolean> markerHits = new ArrayList<>();
	private final List<Integer> hitStageIndices = new ArrayList<>();
	private final List<Integer> coolingMarkerIndices = new ArrayList<>();
	private final int[] failedHeatStageCounts = new int[STAGE_COUNT];
	private final Random random = new Random();

	private int coolingMarkerHits = 0;
	private int missMarkerHits = 0;

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

	public interface MinigameCallback {
		void markDirty();

		void playHitEffect(Vec2f markerLocalPos);

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
		markerTimeout = 0;
		markerSpawnDelay = INITIAL_MARKER_DELAY_TICKS;

		coolingMarkerIndices.clear();

		randomizeCoolingMarkerIndices();

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

				if (nbt.contains("coolingMarkerIndices")) {
					coolingMarkerIndices.clear();

					int[] arr = nbt.getIntArray("coolingMarkerIndices");

					for (int idx : arr) {
						if (idx >= 0 && idx < requiredHits) {
							coolingMarkerIndices.add(idx);
						}
					}

					randomizeCoolingMarkerIndices();
				}
			} else {
				this.coolingMarkerHits = 0;
				this.missMarkerHits = 0;
				clearFailedHeatStageCounts();
				hitStageIndices.clear();
			}
		} else {
			this.coolingMarkerHits = 0;
			this.missMarkerHits = 0;
			clearFailedHeatStageCounts();
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
		TemperatureStage stage = TemperatureRules.stage(temperature, TemperatureRules.stages(profile));
		int stageIndex = stageIndexFor(stage);
		boolean coolingMarker = coolingMarkerIndices.contains(markerIndex);
		int tempChange = coolingMarker ? COOLING_MARKER_HEAT_CHANGE : NORMAL_MARKER_HEAT_CHANGE;

		if (!canShapeAtTemperature(stage)) {
			failedHeatStageCounts[stageIndex]++;
			missMarkerHits++;

			callback.playMissEffect();
			applyTemperatureChange(stack, profile, temperature, tempChange);
			return;
		}

		markerHitsCount++;

		/*
		 * markerAttempts now tracks successful marker progress only.
		 * Misses, timeouts, and bad-temperature blows do not advance this.
		 */
		markerAttempts = markerHitsCount;

		if (!markerPositions.isEmpty()) {
			markerHits.set(0, true);
			callback.playHitEffect(markerPositions.get(0));
		}

		hitStageIndices.add(stageIndex);

		applyTemperatureChange(stack, profile, temperature, tempChange);

		if (coolingMarker) {
			coolingMarkerHits++;
		}
	}

	private boolean canShapeAtTemperature(TemperatureStage stage) {
		return stage == TemperatureStage.HOT
				|| stage == TemperatureStage.WORKABLE
				|| stage == TemperatureStage.OVERHEATED;
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
	}

	private void applyMarkerTimeout(ItemStack stack) {
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

				markerPositions.add(marker);
				markerHits.add(false);

				markerTimeout = coolingMarkerIndices.contains(markerHitsCount)
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
				logic.createMatchContext(world, pos, stack),
				RESULT_RANDOM
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

		if (!TemperatureRules.canTrackTemperature(stack)) {
			return true;
		}

		TemperatureRules.TemperatureStages stages = TemperatureRules.stages(stack);
		int temperature = TemperatureState.currentTemperature(stack);

		return stages.workableStart <= TemperatureState.DEFAULT_TEMPERATURE
				|| temperature < stages.workableStart
				|| temperature <= TemperatureState.DEFAULT_TEMPERATURE;
	}

	private static ItemStack createFinalResultStack(
			ItemStack stack,
			MatchContext context,
			Random random
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

		resultStack = applyCondition(resultStack, context, random);

		TemperatureProfile.from(stack).writeTo(resultStack);
		TemperatureState.copyFrom(stack, resultStack);

		return resultStack;
	}

	private static ItemStack applyCondition(
			ItemStack resultStack,
			MatchContext context,
			Random random
	) {
		var stateOpt = StateService.INSTANCE.convert(resultStack);

		if (stateOpt.isEmpty()
				|| !(stateOpt.get() instanceof com.sigmundgranaas.forgero.core.condition.Conditional<?> conditional)) {
			return resultStack;
		}

		var state = stateOpt.get();

		com.sigmundgranaas.forgero.core.condition.NamedCondition directCondition =
				PredicateConditionLootRegistry.getCondition(context);

		if (directCondition != null && directCondition.matches(state)) {
			var conditioned = conditional.applyCondition(directCondition);
			var newStackOpt = StateService.INSTANCE.convert(
					(com.sigmundgranaas.forgero.core.state.State) conditioned
			);

			return newStackOpt.orElse(resultStack);
		}

		var lootTable = PredicateConditionLootRegistry.getLootTable(context);
		List<com.sigmundgranaas.forgero.core.condition.NamedCondition> applicableConditions =
				lootTable.stream()
						.filter(cond -> cond.matches(state))
						.toList();

		if (applicableConditions.isEmpty()) {
			return resultStack;
		}

		com.sigmundgranaas.forgero.core.condition.NamedCondition randomCondition =
				applicableConditions.get(random.nextInt(applicableConditions.size()));

		var conditioned = conditional.applyCondition(randomCondition);
		var newStackOpt = StateService.INSTANCE.convert(
				(com.sigmundgranaas.forgero.core.state.State) conditioned
		);

		return newStackOpt.orElse(resultStack);
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

		context = context.put(MinecraftContextKeys.COLD_STAGE_HITS, stageCounts[0]);
		context = context.put(MinecraftContextKeys.WARM_STAGE_HITS, stageCounts[1]);
		context = context.put(MinecraftContextKeys.HOT_STAGE_HITS, stageCounts[2]);
		context = context.put(MinecraftContextKeys.WORKABLE_STAGE_HITS, stageCounts[3]);
		context = context.put(MinecraftContextKeys.OVERHEATED_STAGE_HITS, stageCounts[4]);

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

		coolingMarkerIndices.clear();

		if (nbt.contains("coolingMarkerIndices")) {
			int[] arr = nbt.getIntArray("coolingMarkerIndices");

			for (int idx : arr) {
				if (idx >= 0 && idx < requiredHits) {
					coolingMarkerIndices.add(idx);
				}
			}
		}

		randomizeCoolingMarkerIndices();

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

		randomizeCoolingMarkerIndices();

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

	private void randomizeCoolingMarkerIndices() {
		int availableCoolingMarkerSlots = Math.max(0, requiredHits - 1);
		int coolingMarkerCount = Math.min(COOLING_MARKERS, availableCoolingMarkerSlots);

		while (coolingMarkerIndices.size() < coolingMarkerCount) {
			int idx = 1 + random.nextInt(availableCoolingMarkerSlots);

			if (!coolingMarkerIndices.contains(idx)) {
				coolingMarkerIndices.add(idx);
			}
		}
	}
}
