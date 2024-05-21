package dev.xkmc.l2library.base;

import dev.xkmc.l2library.util.code.Wrappers;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistryEntry;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class NamedEntry<T extends NamedEntry<T>> implements IForgeRegistryEntry<T> {

	private final L2Registrate.RegistryInstance<T> registry;

	private String desc = null;

	public NamedEntry(L2Registrate.RegistryInstance<T> registry) {
		this.registry = registry;
	}

	public @NotNull String getDescriptionId() {
		if (desc != null)
			return desc;
		ResourceLocation rl = getRegistryName();
		ResourceLocation reg = registry.get().getRegistryName();
		desc = reg.getPath() + "." + rl.getNamespace() + "." + rl.getPath();
		return desc;
	}

	public MutableComponent getDesc() {
		return new TranslatableComponent(getDescriptionId());
	}

	public ResourceLocation getRegistryName() {
		return Objects.requireNonNull(registry.get().getKey(getThis()));
	}

	public String getID() {
		return getRegistryName().toString();
	}

	public T getThis() {
		return Wrappers.cast(this);
	}

	public T setRegistryName(ResourceLocation name){
		// we should never get here
		throw new IllegalStateException("Attempted to set registry name on NamedEntry!");
	}

	public Class<T> getRegistryType(){
		// implementation is required :-(
		return registry.get().getRegistrySuperType();
	}
}
