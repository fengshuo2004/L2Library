package dev.xkmc.l2library.menu.tabs.contents;

import net.minecraft.world.entity.ai.attributes.Attribute;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

public record AttributeEntry(Supplier<Attribute> sup, boolean usePercent, int order) {

	static final List<AttributeEntry> LIST = new ArrayList<>();

	public static void add(Supplier<Attribute> sup, boolean usePercent, int order) {
		LIST.add(new AttributeEntry(() -> sup.get().setSyncable(true), usePercent, order));
		LIST.sort(Comparator.comparingInt(AttributeEntry::order));
	}

}
