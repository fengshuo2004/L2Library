package dev.xkmc.l2library.block.mult;

import java.util.Random;

import dev.xkmc.l2library.block.type.MultipleBlockMethod;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

public interface RandomTickBlockMethod extends MultipleBlockMethod {

	void randomTick(BlockState state, ServerLevel world, BlockPos pos, Random random);

}
