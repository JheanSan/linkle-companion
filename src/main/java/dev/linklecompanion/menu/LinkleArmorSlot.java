package dev.linklecompanion.menu;

import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.jspecify.annotations.Nullable;

/**
 * One of Linkle's armor slots: one item, only what fits that body slot, respects Curse of Binding,
 * shows the empty-slot icon. Same rules as vanilla's armor slot, which isn't public on every
 * supported Minecraft version.
 */
public class LinkleArmorSlot extends Slot {
	private final LivingEntity owner;
	private final EquipmentSlot slot;
	private final @Nullable Identifier emptyIcon;

	public LinkleArmorSlot(Container container, LivingEntity owner, EquipmentSlot slot, int index, int x, int y, @Nullable Identifier emptyIcon) {
		super(container, index, x, y);
		this.owner = owner;
		this.slot = slot;
		this.emptyIcon = emptyIcon;
	}

	@Override
	public void setByPlayer(ItemStack stack, ItemStack previous) {
		owner.onEquipItem(slot, previous, stack);
		super.setByPlayer(stack, previous);
	}

	@Override
	public int getMaxStackSize() {
		return 1;
	}

	@Override
	public boolean mayPlace(ItemStack stack) {
		return owner.isEquippableInSlot(stack, slot);
	}

	@Override
	public boolean isActive() {
		return owner.canUseSlot(slot);
	}

	@Override
	public boolean mayPickup(Player player) {
		ItemStack stack = getItem();
		if (!stack.isEmpty() && !player.isCreative() && EnchantmentHelper.has(stack, EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE)) {
			return false;
		}
		return super.mayPickup(player);
	}

	@Override
	public @Nullable Identifier getNoItemIcon() {
		return emptyIcon;
	}
}
