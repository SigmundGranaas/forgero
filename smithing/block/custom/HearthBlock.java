// ...existing code...
import com.sigmundgranaas.forgero.smithing.item.custom.CrucibleItem;
import com.sigmundgranaas.forgero.smithing.block.entity.HearthBlockEntity;
// ...existing code...

@Override
public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
	BlockEntity blockEntity = world.getBlockEntity(pos);
	if (blockEntity instanceof HearthBlockEntity hearth) {
		ItemStack held = player.getStackInHand(hand);
		ItemStack crucible = hearth.getCrucibleSlot().get(0);

		if (held.getItem() instanceof CrucibleItem) {
			// Try to place crucible if slot is empty
			if (!world.isClient && hearth.placeCrucible(held)) {
				return ActionResult.SUCCESS;
			}
			return ActionResult.CONSUME;
		} else if (crucible.getItem() instanceof CrucibleItem) {
			// Try to remove crucible if allowed
			ItemStack removed = hearth.removeCrucible();
			if (!world.isClient && !removed.isEmpty()) {
				if (!player.getInventory().insertStack(removed)) {
					player.dropItem(removed, false);
				}
				return ActionResult.SUCCESS;
			}
			return ActionResult.CONSUME;
		}
	}
	return ActionResult.PASS;
}
// ...existing code...

