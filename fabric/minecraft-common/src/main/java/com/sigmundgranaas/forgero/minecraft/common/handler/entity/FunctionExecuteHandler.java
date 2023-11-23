package com.sigmundgranaas.forgero.minecraft.common.handler.entity;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.core.property.v2.feature.HandlerBuilder;
import com.sigmundgranaas.forgero.core.property.v2.feature.JsonBuilder;
import com.sigmundgranaas.forgero.minecraft.common.handler.targeted.onHitBlock.OnHitBlockHandler;
import com.sigmundgranaas.forgero.minecraft.common.handler.targeted.onHitEntity.OnHitHandler;
import com.sigmundgranaas.forgero.minecraft.common.handler.use.BlockUseHandler;
import com.sigmundgranaas.forgero.minecraft.common.handler.use.EntityUseHandler;
import com.sigmundgranaas.forgero.minecraft.common.handler.use.UseHandler;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.function.CommandFunction;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a handler that executes a given Minecraft function or a set of commands upon hitting a target.
 * The function can either be a predefined function from data packs or directly embedded commands.
 *
 * <p>Example JSON configuration for a function reference:
 * <pre>
 * {
 *   "type": "minecraft:function",
 *   "function": "mydatapack:some_function"
 * }
 * </pre>
 * </p>
 *
 * <p>Example JSON configuration for embedded commands:
 * <pre>
 * {
 *   "type": "minecraft:function",
 *   "function": [
 *     "say Hello, World!",
 *     "give @s minecraft:diamond 1"
 *   ]
 * }
 * </pre>
 * </p>
 */
@Getter
@Accessors(fluent = true)
public class FunctionExecuteHandler implements OnHitHandler, EntityHandler, OnHitBlockHandler, UseHandler, EntityUseHandler, BlockUseHandler {
	public static final String TYPE = "minecraft:function";
	public static final JsonBuilder<FunctionExecuteHandler> BUILDER = HandlerBuilder.fromObject(FunctionExecuteHandler.class, FunctionExecuteHandler::fromJson);
	private final List<SingleFunctionHandler> functions;

	private FunctionExecuteHandler(List<SingleFunctionHandler> functions) {
		this.functions = functions;
	}

	/**
	 * Constructs a {@link FunctionExecuteHandler} from a JSON object.
	 *
	 * @param json The JSON object.
	 * @return A new instance of {@link FunctionExecuteHandler}.
	 */
	public static FunctionExecuteHandler fromJson(JsonObject json) {
		List<SingleFunctionHandler> handlers = new ArrayList<>();
		JsonElement element = json.get("function");
		if (element.isJsonArray()) {
			for (JsonElement item : element.getAsJsonArray()) {
				handlers.add(SingleFunctionHandler.of(item.getAsString()));
			}
		} else if (element.isJsonPrimitive()) {
			handlers.add(SingleFunctionHandler.of(element.getAsString()));
		} else {
			throw new IllegalArgumentException("Invalid format for function");
		}

		return new FunctionExecuteHandler(handlers);
	}

	/**
	 * This method is triggered upon hitting an entity.
	 * Executes the configured function(s) or command(s).
	 *
	 * @param source       The source entity.
	 * @param world        The world where the event occurred.
	 * @param targetEntity The targeted entity.
	 */
	@Override
	public void onHit(Entity source, World world, Entity targetEntity) {
		if (world instanceof ServerWorld serverWorld) {
			execute(source, serverWorld, new Vec3d(targetEntity.getX(), targetEntity.getY(), targetEntity.getZ()));
		}
	}

	private void execute(Entity source, ServerWorld world, Vec3d pos) {
		ServerCommandSource commandSource = world.getServer()
				.getCommandSource()
				.withEntity(source)
				.withPosition(pos)
				.withSilent();

		for (SingleFunctionHandler function : functions) {
			function.execute(world, commandSource);
		}
	}

	@Override
	public void handle(Entity entity) {
		onHit(entity, entity.getWorld(), entity);
	}

	/**
	 * This method is triggered upon hitting aa block.
	 * Executes the configured function(s) or command(s).
	 *
	 * @param source The source entity.
	 * @param world  The world where the event occurred.
	 * @param pos    The targeted block position.
	 */
	@Override
	public void onHit(Entity source, World world, BlockPos pos) {
		if (world instanceof ServerWorld serverWorld) {
			execute(source, serverWorld, new Vec3d(pos.getX(), pos.getY(), pos.getZ()));
		}
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		onHit(user, world, user);
		return TypedActionResult.success(user.getStackInHand(hand));
	}

	@Override
	public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
		onHit(user, user.getWorld(), entity);
		return ActionResult.SUCCESS;
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext context) {
		if (context.getWorld() != null && context.getWorld() instanceof ServerWorld serverWorld) {
			execute(context.getPlayer(), serverWorld, new Vec3d(context.getHitPos().getX(), context.getHitPos().getY(), context.getHitPos().getZ()));
		}
		return ActionResult.SUCCESS;
	}

	private abstract static class SingleFunctionHandler {
		protected final String function;

		private SingleFunctionHandler(String function) {
			this.function = function;
		}

		private static SingleFunctionHandler of(String function) {
			if (function.contains(" ")) {
				return new EmbeddedCommandHandler(function);
			} else {
				return new FunctionReferenceHandler(function);
			}
		}

		abstract void execute(ServerWorld world, ServerCommandSource source);
	}

	private static class FunctionReferenceHandler extends SingleFunctionHandler {
		private FunctionReferenceHandler(String functionReference) {
			super(functionReference);
		}

		@Override
		protected void execute(ServerWorld world, ServerCommandSource source) {
			world.getServer().getCommandFunctionManager().getFunction(new Identifier(function)).ifPresent(
					func -> world.getServer().getCommandFunctionManager().execute(func, source)
			);
		}
	}

	private static class EmbeddedCommandHandler extends SingleFunctionHandler {
		private EmbeddedCommandHandler(String command) {
			super(command);
		}

		@Override
		protected void execute(ServerWorld world, ServerCommandSource source) {
			CommandFunction fn = CommandFunction.create(new Identifier("forgero:dynamic"), world.getServer().getCommandFunctionManager().getDispatcher(), source, List.of(function));
			world.getServer().getCommandFunctionManager().execute(fn, source);
		}
	}
}


