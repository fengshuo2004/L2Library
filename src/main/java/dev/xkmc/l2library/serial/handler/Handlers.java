package dev.xkmc.l2library.serial.handler;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import dev.xkmc.l2library.serial.generic.*;
import dev.xkmc.l2library.serial.nulldefer.NullDefer;
import dev.xkmc.l2library.serial.nulldefer.SimpleNullDefer;
import dev.xkmc.l2library.util.code.Wrappers;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

public class Handlers {

	public static final Map<Class<?>, JsonClassHandler<?>> JSON_MAP = new HashMap<>();
	public static final Map<Class<?>, NBTClassHandler<?, ?>> NBT_MAP = new HashMap<>();
	public static final Map<Class<?>, PacketClassHandler<?>> PACKET_MAP = new HashMap<>();

	public static final List<GenericCodec> LIST = new ArrayList<>();
	public static final Map<Class<?>, NullDefer<?>> MAP = new HashMap<>();

	// register handlers
	static {
		// primitives

		new ClassHandler<>(long.class, JsonPrimitive::new, JsonElement::getAsLong, FriendlyByteBuf::readLong, FriendlyByteBuf::writeLong, LongTag::getAsLong, LongTag::valueOf, Long.class);
		new ClassHandler<>(int.class, JsonPrimitive::new, JsonElement::getAsInt, FriendlyByteBuf::readInt, FriendlyByteBuf::writeInt, IntTag::getAsInt, IntTag::valueOf, Integer.class);
		new ClassHandler<ShortTag, Short>(short.class, JsonPrimitive::new, JsonElement::getAsShort, FriendlyByteBuf::readShort, FriendlyByteBuf::writeShort, ShortTag::getAsShort, ShortTag::valueOf, Short.class);
		new ClassHandler<ByteTag, Byte>(byte.class, JsonPrimitive::new, JsonElement::getAsByte, FriendlyByteBuf::readByte, FriendlyByteBuf::writeByte, ByteTag::getAsByte, ByteTag::valueOf, Byte.class);
		new ClassHandler<ByteTag, Boolean>(boolean.class, JsonPrimitive::new, JsonElement::getAsBoolean, FriendlyByteBuf::readBoolean, FriendlyByteBuf::writeBoolean, tag -> tag.getAsByte() != 0, ByteTag::valueOf, Boolean.class);
		new ClassHandler<ByteTag, Character>(char.class, JsonPrimitive::new, JsonElement::getAsCharacter, FriendlyByteBuf::readChar, FriendlyByteBuf::writeChar, t -> (char) t.getAsByte(), c -> ByteTag.valueOf((byte) (char) c), Character.class);
		new ClassHandler<>(double.class, JsonPrimitive::new, JsonElement::getAsDouble, FriendlyByteBuf::readDouble, FriendlyByteBuf::writeDouble, DoubleTag::getAsDouble, DoubleTag::valueOf, Double.class);
		new ClassHandler<>(float.class, JsonPrimitive::new, JsonElement::getAsFloat, FriendlyByteBuf::readFloat, FriendlyByteBuf::writeFloat, FloatTag::getAsFloat, FloatTag::valueOf, Float.class);

		new ClassHandler<>(String.class, JsonPrimitive::new, JsonElement::getAsString, FriendlyByteBuf::readUtf, FriendlyByteBuf::writeUtf, Tag::getAsString, StringTag::valueOf);

		// minecraft
		new ClassHandler<>(ItemStack.class, StackHelper::serializeItemStack, StackHelper::deserializeItemStack, FriendlyByteBuf::readItem, (p, is) -> p.writeItemStack(is, false), ItemStack::of, is -> is.save(new CompoundTag()));
		new ClassHandler<>(FluidStack.class, StackHelper::serializeFluidStack, StackHelper::deserializeFluidStack, FluidStack::readFromPacket, (p, f) -> f.writeToPacket(p), FluidStack::loadFluidStackFromNBT, f -> f.writeToNBT(new CompoundTag()));

		new StringClassHandler<>(ResourceLocation.class, ResourceLocation::new, ResourceLocation::toString);
		new StringClassHandler<>(UUID.class, UUID::fromString, UUID::toString);

		new RLClassHandler<>(Item.class, () -> ForgeRegistries.ITEMS);
		new RLClassHandler<>(Block.class, () -> ForgeRegistries.BLOCKS);
		new RLClassHandler<>(Potion.class, () -> ForgeRegistries.POTIONS);
		new RLClassHandler<>(Enchantment.class, () -> ForgeRegistries.ENCHANTMENTS);
		new RLClassHandler<>(MobEffect.class, () -> ForgeRegistries.MOB_EFFECTS);
		new RLClassHandler<>(Wrappers.cast(EntityType.class), () -> ForgeRegistries.ENTITIES);

		// partials

		// no NBT
		new ClassHandler<>(Ingredient.class, Ingredient::toJson,
				e -> e.isJsonArray() && e.getAsJsonArray().size() == 0 ? Ingredient.EMPTY : Ingredient.fromJson(e),
				Ingredient::fromNetwork, (p, o) -> o.toNetwork(p), null, null);

		// no JSON
		new ClassHandler<CompoundTag, CompoundTag>(CompoundTag.class, null, null, FriendlyByteBuf::readAnySizeNbt, FriendlyByteBuf::writeNbt, e -> e, e -> e);
		new ClassHandler<ListTag, ListTag>(ListTag.class, null, null, buf -> (ListTag) buf.readAnySizeNbt().get("warp"), (buf, tag) -> {
			CompoundTag comp = new CompoundTag();
			comp.put("warp", tag);
			buf.writeNbt(comp);
		}, e -> e, e -> e);

		new ClassHandler<>(long[].class, null, null, null, null, LongArrayTag::getAsLongArray, LongArrayTag::new);
		new ClassHandler<>(int[].class, null, null, null, null, IntArrayTag::getAsIntArray, IntArrayTag::new);
		new ClassHandler<>(byte[].class, null, null, null, null, ByteArrayTag::getAsByteArray, ByteArrayTag::new);
		new AutoPacketNBTHandler<>(BlockPos.class,
				tag -> new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z")),
				obj -> {
					CompoundTag tag = new CompoundTag();
					tag.putInt("x", obj.getX());
					tag.putInt("y", obj.getY());
					tag.putInt("z", obj.getZ());
					return tag;
				});
		new AutoPacketNBTHandler<>(Vec3.class,
				tag -> new Vec3(tag.getDouble("x"), tag.getDouble("y"), tag.getDouble("z")),
				obj -> {
					CompoundTag tag = new CompoundTag();
					tag.putDouble("x", obj.x());
					tag.putDouble("y", obj.y());
					tag.putDouble("z", obj.z());
					return tag;
				});
		new AutoPacketNBTHandler<>(MobEffectInstance.class,
				MobEffectInstance::load, e -> e.save(new CompoundTag()));
	}

	// register generic codec
	static {
		new RecordCodec();
		new EnumCodec();
		new ArrayCodec();
		new AliasCodec();
		new ListCodec();
		new SetCodec();
		new MapCodec();
	}

	// register null defer
	static {
		new SimpleNullDefer<>(ItemStack.class, ItemStack.EMPTY);
		new SimpleNullDefer<>(Ingredient.class, Ingredient.EMPTY);
	}

	public static void register() {

	}

}
