public VoxelShape makeShape(){
	VoxelShape shape = VoxelShapes.empty();
	shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.6875, 0, 0.375, 0.9375, 0.8125, 0.6875));
	shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.375, 1.7229223016368562, 0.27745873926376097, 0.625, 2.1604223016368564, 0.589958739263761));
	shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.1875, 0.625, 0.25, 0.8125, 1.9375, 0.8125));
	shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.0625, 0, 0.375, 0.3125, 0.8125, 0.6875));
	shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.8125, 1.0625, 0.375, 1, 1.875, 0.625));
	shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0, 1.0625, 0.375, 0.1875, 1.875, 0.625));
	shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.3125, 2.062881040900617, 0.21875, 0.6875, 2.375381040900617, 0.65625));
	shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.4375, 0.6875, 0.8125, 0.5625, 0.8125, 1.0625));
	shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.26875, 2.25, 0.46875, 0.33125, 2.5, 0.59375));
	shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.375, 2.125381040900617, -0.03125, 0.625, 2.312881040900617, 0.28125));
	shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.66875, 2.25, 0.46875, 0.7312500000000001, 2.5, 0.59375));
	shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.5, 1.8125, 0, 0.5, 2.1875, 0.3125));
	shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.546875, 2.375, 0.5625, 0.671875, 2.6875, 0.6875));
	shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.328125, 2.375, 0.5625, 0.453125, 2.6875, 0.6875));
	shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.328125, 2.4748705582505734, 0.7610572190051805, 0.453125, 2.7873705582505734, 0.8860572190051805));
	shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.546875, 2.4748705582505734, 0.7610572190051805, 0.671875, 2.7873705582505734, 0.8860572190051805));

	return shape;
}