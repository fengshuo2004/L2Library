package dev.xkmc.l2library.base.recipe;

import com.google.gson.JsonObject;
import dev.xkmc.l2library.serial.codec.JsonCodec;
import dev.xkmc.l2library.serial.codec.PacketCodec;
import dev.xkmc.l2library.util.code.Wrappers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class RecSerializer<R extends Recipe<I>, I extends Container> extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<R> {

	public final Class<R> cls;

	public RecSerializer(Class<R> cls) {
		this.cls = cls;
	}

	@Override
	public R fromJson(ResourceLocation id, JsonObject json) {
		return JsonCodec.from(json, cls,
				Wrappers.get(() -> cls.getConstructor(ResourceLocation.class).newInstance(id)));

	}

	@Override
	public R fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
		return PacketCodec.from(buf, cls,
				Wrappers.get(() -> cls.getConstructor(ResourceLocation.class).newInstance(id)));
	}

	@Override
	public void toNetwork(FriendlyByteBuf buf, R recipe) {
		PacketCodec.to(buf, recipe);
	}

	public R blank() {
		return Wrappers.get(() -> cls.getConstructor(ResourceLocation.class).newInstance(new ResourceLocation("dummy")));
	}

}
