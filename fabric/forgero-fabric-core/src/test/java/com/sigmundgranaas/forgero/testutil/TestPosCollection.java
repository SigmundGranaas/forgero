package com.sigmundgranaas.forgero.testutil;

import static com.sigmundgranaas.forgero.testutil.Utils.debugId;

import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

public record TestPosCollection(Set<TestPos> positions) {
	public static TestPosCollection of(Set<BlockPos> relative, TestContext ctx) {
		var positions = relative.stream().map(pos -> TestPos.of(pos, ctx)).collect(Collectors.toSet());
		return new TestPosCollection(positions);
	}

	public static TestPosCollection of(Set<TestPos> positions) {
		return new TestPosCollection(positions);
	}

	public Set<BlockPos> relative() {
		return positions.stream().map(TestPos::relative).collect(Collectors.toSet());
	}

	public Set<BlockPos> absolute() {
		return positions.stream().map(TestPos::absolute).collect(Collectors.toSet());
	}

	public boolean anyMatch(Predicate<TestPos> pos) {
		return positions.stream().anyMatch(pos);
	}

	public TestPosCollection apply(Predicate<TestPos> predicate) {
		return of(positions.stream().filter(predicate).collect(Collectors.toSet()));
	}

	public long count(Predicate<TestPos> predicate) {
		return positions.stream().filter(predicate).count();
	}

	@Override
	public String toString() {
		return "TestPosCollection{" +
				"positions=" + positions.toString() +
				'}';
	}

	public String toString(ContextSupplier ctx) {
		String elements = positions.stream()
				.map(pos -> String.format("{ Block: %s, pos: %s }", debugId(ctx.absolute(pos)), pos))
				.collect(Collectors.joining(", "));

		return "TestPosCollection{" +
				"elements=" + elements +
				'}';
	}
}
