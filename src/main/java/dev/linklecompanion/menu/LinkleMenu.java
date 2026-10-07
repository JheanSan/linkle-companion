package dev.linklecompanion.menu;

import dev.linklecompanion.entity.LinkleEntity;
import dev.linklecompanion.network.LinkleActionPayload;
import dev.linklecompanion.registry.ModMenus;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

/**
 * Linkle's inventory screen (sneak + right-click): four armor slots and nine pocket slots.
 *
 * <p>Slot order: 0-3 armor (head, chest, legs, feet), 4-12 pockets, 13-48 the player's inventory.
 * Positions match {@code textures/gui/linkle_inventory.png}.
 */
public class LinkleMenu extends AbstractContainerMenu {
	public static final int ARMOR_X = 8;
	public static final int ARMOR_Y = 18;
	public static final int POCKET_X = 98;
	public static final int POCKET_Y = 18;
	public static final int PLAYER_INV_Y = 104;
	public static final int IMAGE_HEIGHT = 186;

	private static final EquipmentSlot[] ARMOR = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
	private static final Identifier[] ARMOR_ICONS = {
		InventoryMenu.EMPTY_ARMOR_SLOT_HELMET, InventoryMenu.EMPTY_ARMOR_SLOT_CHESTPLATE,
		InventoryMenu.EMPTY_ARMOR_SLOT_LEGGINGS, InventoryMenu.EMPTY_ARMOR_SLOT_BOOTS
	};
	private static final int LINKLE_SLOTS = ARMOR.length + LinkleEntity.INVENTORY_SIZE;

	private final @Nullable LinkleEntity linkle;
	private final Container pockets;

	public LinkleMenu(int containerId, Inventory playerInventory, @Nullable LinkleEntity linkle) {
		super(ModMenus.LINKLE, containerId);
		this.linkle = linkle;
		this.pockets = linkle != null ? linkle.getInventory() : new SimpleContainer(LinkleEntity.INVENTORY_SIZE);

		for (int i = 0; i < ARMOR.length; i++) {
			int y = ARMOR_Y + i * 18;
			if (linkle != null) {
				this.addSlot(new LinkleArmorSlot(linkle.createEquipmentSlotContainer(ARMOR[i]), linkle, ARMOR[i], 0, ARMOR_X, y, ARMOR_ICONS[i]));
			} else {
				// Fallback only if the client can't find her (should not happen).
				this.addSlot(new Slot(new SimpleContainer(1), 0, ARMOR_X, y));
			}
		}
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 3; col++) {
				this.addSlot(new Slot(pockets, col + row * 3, POCKET_X + col * 18, POCKET_Y + row * 18));
			}
		}
		this.addStandardInventorySlots(playerInventory, 8, PLAYER_INV_Y);
	}

	/** Client side: the server sends Linkle's entity id when opening the screen. */
	public static LinkleMenu fromNetwork(int containerId, Inventory playerInventory, Integer entityId) {
		Entity entity = playerInventory.player.level().getEntity(entityId);
		return new LinkleMenu(containerId, playerInventory, entity instanceof LinkleEntity linkle ? linkle : null);
	}

	public @Nullable LinkleEntity getLinkle() {
		return linkle;
	}

	@Override
	public boolean stillValid(Player player) {
		return linkle != null && linkle.isAlive() && !linkle.isRemoved()
			&& (player.level().isClientSide() || linkle.isOwnedBy(player))
			&& player.level() == linkle.level()
			&& player.distanceToSqr(linkle) <= LinkleActionPayload.INVENTORY_RANGE * LinkleActionPayload.INVENTORY_RANGE;
	}

	@Override
	public ItemStack quickMoveStack(Player player, int slotIndex) {
		Slot slot = this.slots.get(slotIndex);
		if (slot == null || !slot.hasItem()) {
			return ItemStack.EMPTY;
		}
		ItemStack stack = slot.getItem();
		ItemStack original = stack.copy();
		int playerStart = LINKLE_SLOTS;
		int playerEnd = this.slots.size();

		if (slotIndex < LINKLE_SLOTS) {
			// From Linkle to the player.
			if (!this.moveItemStackTo(stack, playerStart, playerEnd, true)) {
				return ItemStack.EMPTY;
			}
		} else {
			// From the player: armor goes to her matching armor slot, everything else to her pockets.
			boolean moved = false;
			for (int i = 0; i < ARMOR.length; i++) {
				Slot armorSlot = this.slots.get(i);
				if (!armorSlot.hasItem() && armorSlot.mayPlace(stack)) {
					moved = this.moveItemStackTo(stack, i, i + 1, false);
					break;
				}
			}
			if (!moved && !this.moveItemStackTo(stack, ARMOR.length, LINKLE_SLOTS, false)) {
				return ItemStack.EMPTY;
			}
		}

		if (stack.isEmpty()) {
			slot.setByPlayer(ItemStack.EMPTY);
		} else {
			slot.setChanged();
		}
		return original;
	}
}
