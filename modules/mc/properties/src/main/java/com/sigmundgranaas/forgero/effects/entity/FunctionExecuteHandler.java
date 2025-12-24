package com.sigmundgranaas.forgero.effects.entity;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.function.CommandFunction;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Handler that executes Minecraft datapack functions or commands.
 *
 * <h3>JSON Example (Function Reference):</h3>
 * <pre>
 * {
 *   "type": "forgero:function",
 *   "function": "mydatapack:some_function"
 * }
 * </pre>
 *
 * <h3>JSON Example (Direct Commands):</h3>
 * <pre>
 * {
 *   "type": "forgero:function",
 *   "function": [
 *     "say Hello!",
 *     "give @s minecraft:diamond 1"
 *   ]
 * }
 * </pre>
 */
public record FunctionExecuteHandler(
		List<String> functions
) implements ContextualEffectHandler {

	public static final String TYPE = "forgero:function";
	private static final Logger LOGGER = LoggerFactory.getLogger(FunctionExecuteHandler.class);

	public static final Codec<FunctionExecuteHandler> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.either(
					Codec.STRING,
					Codec.STRING.listOf()
			).xmap(
					either -> either.map(List::of, list -> list),
					list -> list.size() == 1 ? Either.left(list.get(0)) : Either.right(list)
			).fieldOf("function").forGetter(FunctionExecuteHandler::functions)
	).apply(instance, FunctionExecuteHandler::new));

	@Override
	public void apply(Entity source, Entity target) {
		if (!(source.getWorld() instanceof ServerWorld serverWorld)) {
			return;
		}

		Vec3d pos = new Vec3d(target.getX(), target.getY(), target.getZ());
		execute(source, serverWorld, pos);
	}

	@Override
	public String type() {
		return TYPE;
	}

	private void execute(Entity source, ServerWorld world, Vec3d pos) {
		ServerCommandSource commandSource = world.getServer()
				.getCommandSource()
				.withEntity(source)
				.withPosition(pos)
				.withSilent();

		for (String function : functions) {
			executeSingleFunction(function, world, commandSource);
		}
	}

	private void executeSingleFunction(String function, ServerWorld world, ServerCommandSource source) {
		if (function.contains(" ")) {
			// This is a direct command
			executeCommand(function, world, source);
		} else {
			// This is a function reference
			executeFunctionReference(function, world, source);
		}
	}

	private void executeFunctionReference(String functionId, ServerWorld world, ServerCommandSource source) {
		try {
			Identifier id = new Identifier(functionId);
			world.getServer().getCommandFunctionManager().getFunction(id).ifPresentOrElse(
					func -> world.getServer().getCommandFunctionManager().execute(func, source),
					() -> LOGGER.warn("Function not found: {}", functionId)
			);
		} catch (Exception e) {
			LOGGER.error("Failed to execute function: {}", functionId, e);
		}
	}

	private void executeCommand(String command, ServerWorld world, ServerCommandSource source) {
		try {
			CommandFunction fn = CommandFunction.create(
					new Identifier("forgero:dynamic"),
					world.getServer().getCommandFunctionManager().getDispatcher(),
					source,
					List.of(command)
			);
			world.getServer().getCommandFunctionManager().execute(fn, source);
		} catch (Exception e) {
			LOGGER.error("Failed to execute command: {}", command, e);
		}
	}
}
