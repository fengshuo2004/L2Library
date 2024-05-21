package dev.xkmc.l2library.init;

import dev.xkmc.l2library.base.effects.ClientEntityEffectRenderEvents;
import dev.xkmc.l2library.base.effects.EffectSyncEvents;
import dev.xkmc.l2library.base.effects.EffectToClient;
import dev.xkmc.l2library.base.ingredients.EnchantmentIngredient;
import dev.xkmc.l2library.base.ingredients.MobEffectIngredient;
import dev.xkmc.l2library.base.ingredients.PotionIngredient;
import dev.xkmc.l2library.base.tabs.contents.AttributeEntry;
import dev.xkmc.l2library.capability.player.PlayerCapToClient;
import dev.xkmc.l2library.capability.player.PlayerCapabilityEvents;
import dev.xkmc.l2library.capability.player.PlayerCapabilityHolder;
import dev.xkmc.l2library.idea.infmaze.worldgen.MazeChunkGenerator;
import dev.xkmc.l2library.init.events.attack.AttackEventHandler;
import dev.xkmc.l2library.init.events.GenericEventHandler;
import dev.xkmc.l2library.serial.handler.Handlers;
import dev.xkmc.l2library.serial.network.PacketHandler;
import dev.xkmc.l2library.serial.network.SyncPacket;
import dev.xkmc.l2library.util.raytrace.TargetSetPacket;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.event.RegistryEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mojang.serialization.Codec;

import static net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT;
import static net.minecraftforge.network.NetworkDirection.PLAY_TO_SERVER;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(L2Library.MODID)
public class L2Library {

	public static final String MODID = "l2library";
	public static final Logger LOGGER = LogManager.getLogger();

	public static final PacketHandler PACKET_HANDLER = new PacketHandler(new ResourceLocation(MODID, "main"), 1,
			e -> e.create(SyncPacket.class, PLAY_TO_CLIENT),
			e -> e.create(EffectToClient.class, PLAY_TO_CLIENT),
			e -> e.create(PlayerCapToClient.class, PLAY_TO_CLIENT),
			e -> e.create(TargetSetPacket.class, PLAY_TO_SERVER));

	private static final DeferredRegister<Codec<? extends ChunkGenerator>> CODEC_CHUNK_GENERATOR =
		DeferredRegister.create(Registry.CHUNK_GENERATOR_REGISTRY, MODID);

	public L2Library() {
		Handlers.register();
		FMLJavaModLoadingContext ctx = FMLJavaModLoadingContext.get();
		IEventBus bus = ctx.getModEventBus();
		CODEC_CHUNK_GENERATOR.register("maze_chunkgen", () -> MazeChunkGenerator.CODEC);
		MinecraftForge.EVENT_BUS.register(GenericEventHandler.class);
		MinecraftForge.EVENT_BUS.register(EffectSyncEvents.class);
		MinecraftForge.EVENT_BUS.register(PlayerCapabilityEvents.class);
		MinecraftForge.EVENT_BUS.register(AttackEventHandler.class);
		MinecraftForge.EVENT_BUS.register(ClientEntityEffectRenderEvents.class);
		bus.addListener(L2Library::registerCaps);
		bus.addListener(PacketHandler::setup);
		bus.addListener(L2Library::setup);
		bus.addGenericListener(RecipeSerializer.class, L2Library::registerRecipeSerializers);
		L2LibraryConfig.init();
		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> L2Client.onCtorClient(bus, MinecraftForge.EVENT_BUS));
	}

	public static void registerCaps(RegisterCapabilitiesEvent event) {
		for (PlayerCapabilityHolder<?> holder : PlayerCapabilityHolder.INTERNAL_MAP.values()) {
			event.register(holder.cls);
		}
	}

	public static void setup(FMLCommonSetupEvent event) {
		event.enqueueWork(() -> {
			AttributeEntry.add(() -> Attributes.MAX_HEALTH, false, 1000);
			AttributeEntry.add(() -> Attributes.ARMOR, false, 2000);
			AttributeEntry.add(() -> Attributes.ARMOR_TOUGHNESS, false, 3000);
			AttributeEntry.add(() -> Attributes.KNOCKBACK_RESISTANCE, false, 4000);
			AttributeEntry.add(() -> Attributes.MOVEMENT_SPEED, false, 5000);
			AttributeEntry.add(() -> Attributes.ATTACK_DAMAGE, false, 6000);
			AttributeEntry.add(() -> Attributes.ATTACK_SPEED, false, 7000);
			AttributeEntry.add(ForgeMod.REACH_DISTANCE, false, 8000);
			AttributeEntry.add(ForgeMod.ATTACK_RANGE, false, 9000);
			AttributeEntry.add(() -> Attributes.LUCK, false, 10000);
		});
	}

	public static void registerRecipeSerializers(RegistryEvent.Register<RecipeSerializer<?>> event) {
		CraftingHelper.register(EnchantmentIngredient.INSTANCE.id(), EnchantmentIngredient.INSTANCE);
		CraftingHelper.register(PotionIngredient.INSTANCE.id(), PotionIngredient.INSTANCE);
		CraftingHelper.register(MobEffectIngredient.INSTANCE.id(), MobEffectIngredient.INSTANCE);
	}

}
