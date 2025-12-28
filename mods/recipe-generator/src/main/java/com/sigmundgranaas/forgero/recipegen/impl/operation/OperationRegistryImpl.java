package com.sigmundgranaas.forgero.recipegen.impl.operation;

import com.sigmundgranaas.forgero.recipegen.api.operation.OperationRegistry;
import com.sigmundgranaas.forgero.recipegen.api.operation.VariableOperation;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe implementation of {@link OperationRegistry}.
 *
 * <p>Operations are organized in a two-level structure: group -> operation name -> handler.
 * Global fallback operations are also supported.</p>
 */
public class OperationRegistryImpl implements OperationRegistry {

	// group -> (operation -> handler)
	private final Map<String, Map<String, VariableOperation>> groups = new ConcurrentHashMap<>();

	// Fallback operations that work for any group
	private final Map<String, VariableOperation> globalOperations = new ConcurrentHashMap<>();

	@Override
	public OperationReference register(String group, String operation, VariableOperation handler) {
		groups.computeIfAbsent(group, k -> new ConcurrentHashMap<>())
				.put(operation, handler);
		return new OperationReferenceImpl(group, operation, handler);
	}

	@Override
	public OperationReference registerGlobal(String operation, VariableOperation handler) {
		globalOperations.put(operation, handler);
		return new OperationReferenceImpl("*", operation, handler);
	}

	@Override
	public Optional<VariableOperation> get(String group, String operation) {
		Map<String, VariableOperation> groupOps = groups.get(group);
		if (groupOps != null && groupOps.containsKey(operation)) {
			return Optional.of(groupOps.get(operation));
		}
		return Optional.ofNullable(globalOperations.get(operation));
	}

	@Override
	public String apply(String group, String operation, Object value) {
		return get(group, operation)
				.filter(op -> op.matches(value))
				.map(op -> op.apply(value))
				.orElseThrow(() -> new IllegalArgumentException(
						String.format("No operation '%s' found in group '%s' for value type %s",
								operation, group, value.getClass().getName())));
	}

	@Override
	public String apply(String operation, Object value) {
		// Try to find operation in any group that matches the value
		for (Map<String, VariableOperation> groupOps : groups.values()) {
			VariableOperation op = groupOps.get(operation);
			if (op != null && op.matches(value)) {
				return op.apply(value);
			}
		}

		// Try global fallback
		VariableOperation global = globalOperations.get(operation);
		if (global != null && global.matches(value)) {
			return global.apply(value);
		}

		// Default to toString
		return value.toString();
	}

	private record OperationReferenceImpl(String group, String operation, VariableOperation handler)
			implements OperationReference {
	}
}
