package dev.xkmc.l2library.block.mult;

import java.util.Random;

import dev.xkmc.l2library.block.type.MultipleBlockMethod;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface AnimateTickBlockMethod extends MultipleBlockMethod {
	@OnlyIn(Dist.CLIENT)
	void animateTick(BlockState state, Level world, BlockPos pos, Random r);
}
