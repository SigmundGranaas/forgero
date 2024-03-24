package com.sigmundgranaas.forgero.testutil;

import java.util.UUID;
import java.util.function.Supplier;

import com.mojang.authlib.GameProfile;

import net.minecraft.item.ItemStack;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.GameMode;


public class PlayerFactory implements ContextSupplier {
	private Supplier<TestContext> ctxSupplier;

	private String playerName = "test-mock-player";
	private UUID uuid = UUID.randomUUID();
	private GameMode gameMode = GameMode.CREATIVE;
	private Supplier<ItemStack> stack = () -> ItemStack.EMPTY;
	private Hand stackHand = Hand.MAIN_HAND;
	private BlockPos pos = new BlockPos(0, 0, 0);
	private Direction direction = Direction.NORTH;

	public static PlayerBuilder builder(TestContext context) {
		return new PlayerBuilder().ctxSupplier(() -> context);
	}

	PlayerFactory(Supplier<TestContext> ctxSupplier, String playerName, UUID uuid, GameMode gameMode, Supplier<ItemStack> stack, Hand stackHand, BlockPos pos, Direction lookDirection) {
		this.ctxSupplier = ctxSupplier;
		this.playerName = playerName;
		this.uuid = uuid;
		this.gameMode = gameMode;
		this.stack = stack;
		this.stackHand = stackHand;
		this.pos = pos;
		this.direction = lookDirection;
	}

	private static String $default$playerName() {
		return "test-mock-player";
	}

	private static UUID $default$uuid() {
		return UUID.randomUUID();
	}

	private static GameMode $default$gameMode() {
		return GameMode.CREATIVE;
	}

	private static Supplier<ItemStack> $default$stack() {
		return () -> ItemStack.EMPTY;
	}

	private static Hand $default$stackHand() {
		return Hand.MAIN_HAND;
	}

	private static BlockPos $default$pos() {
		return new BlockPos(0, 0, 0);
	}

	private static Direction $default$direction() {
		return Direction.NORTH;
	}


	public static PlayerBuilder builder() {
		return new PlayerBuilder();
	}

	@Override
	public TestContext get() {
		return ctxSupplier.get();
	}

	public ServerPlayerEntity createPlayer() {
		TestContext context = ctxSupplier.get();
		context.getWorld().getServer().setDemo(false);
		ServerPlayerEntity entity = new ServerPlayerEntity(context.getWorld().getServer(), context.getWorld(), new GameProfile(uuid, playerName));
		entity.networkHandler = new ServerPlayNetworkHandler(context.getWorld().getServer(), new ClientConnection(NetworkSide.CLIENTBOUND), entity);
		entity.setPos(pos.getX(), pos.getY(), pos.getZ());
		entity.changeGameMode(gameMode);
		entity.setStackInHand(stackHand, stack.get());
		entity.setYaw(direction.asRotation());
		return entity;
	}

	public static class PlayerBuilder {
		private Supplier<TestContext> ctxSupplier;
		private String playerName$value;
		private boolean playerName$set;
		private UUID uuid$value;
		private boolean uuid$set;
		private GameMode gameMode$value;
		private boolean gameMode$set;
		private Supplier<ItemStack> stack$value;
		private boolean stack$set;
		private Hand stackHand$value;
		private boolean stackHand$set;
		private BlockPos pos$value;
		private boolean pos$set;
		private Direction direction$value;
		private boolean direction$set;

		PlayerBuilder() {
		}

		public PlayerBuilder ctxSupplier(Supplier<TestContext> ctxSupplier) {
			this.ctxSupplier = ctxSupplier;
			return this;
		}

		public PlayerBuilder playerName(String playerName) {
			this.playerName$value = playerName;
			this.playerName$set = true;
			return this;
		}

		public PlayerBuilder uuid(UUID uuid) {
			this.uuid$value = uuid;
			this.uuid$set = true;
			return this;
		}

		public PlayerBuilder gameMode(GameMode gameMode) {
			this.gameMode$value = gameMode;
			this.gameMode$set = true;
			return this;
		}

		public PlayerBuilder stack(Supplier<ItemStack> stack) {
			this.stack$value = stack;
			this.stack$set = true;
			return this;
		}

		public PlayerBuilder stackHand(Hand stackHand) {
			this.stackHand$value = stackHand;
			this.stackHand$set = true;
			return this;
		}

		public PlayerBuilder pos(BlockPos pos) {
			this.pos$value = pos;
			this.pos$set = true;
			return this;
		}

		public PlayerBuilder direction(Direction dir) {
			this.direction$value = dir;
			this.direction$set = true;
			return this;
		}

		public PlayerFactory build() {
			String playerName$value = this.playerName$value;
			if (!this.playerName$set) {
				playerName$value = PlayerFactory.$default$playerName();
			}
			UUID uuid$value = this.uuid$value;
			if (!this.uuid$set) {
				uuid$value = PlayerFactory.$default$uuid();
			}
			GameMode gameMode$value = this.gameMode$value;
			if (!this.gameMode$set) {
				gameMode$value = PlayerFactory.$default$gameMode();
			}
			Supplier<ItemStack> stack$value = this.stack$value;
			if (!this.stack$set) {
				stack$value = PlayerFactory.$default$stack();
			}
			Hand stackHand$value = this.stackHand$value;
			if (!this.stackHand$set) {
				stackHand$value = PlayerFactory.$default$stackHand();
			}
			BlockPos pos$value = this.pos$value;
			if (!this.pos$set) {
				pos$value = PlayerFactory.$default$pos();
			}
			Direction direction$value = this.direction$value;
			if (!this.direction$set) {
				direction$value = PlayerFactory.$default$direction();
			}
			return new PlayerFactory(this.ctxSupplier, playerName$value, uuid$value, gameMode$value, stack$value, stackHand$value, pos$value, direction$value);
		}

		public String toString() {
			return "PlayerFactory.PlayerBuilder(ctxSupplier=" + this.ctxSupplier + ", playerName$value=" + this.playerName$value + ", uuid$value=" + this.uuid$value + ", gameMode$value=" + this.gameMode$value + ", stack$value=" + this.stack$value + ", stackHand$value=" + this.stackHand$value + ", pos$value=" + this.pos$value + ")";
		}
	}
}
