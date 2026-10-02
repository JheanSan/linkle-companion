package dev.linklecompanion.client.screen;

import dev.linklecompanion.LinkleCompanion;
import dev.linklecompanion.config.LinkleConfig;
import dev.linklecompanion.entity.LinkleEntity;
import dev.linklecompanion.menu.LinkleMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/** Linkle's inventory: armor on the left, a live preview of her, her nine pockets and a status line. */
public class LinkleScreen extends AbstractContainerScreen<LinkleMenu> {
	private static final Identifier BACKGROUND = LinkleCompanion.id("textures/gui/linkle_inventory.png");
	private static final int LABEL_COLOR = 0xFF404040;

	private float xMouse;
	private float yMouse;

	public LinkleScreen(LinkleMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title, 176, LinkleMenu.IMAGE_HEIGHT);
		this.inventoryLabelY = this.imageHeight - 94;
	}

	@Override
	protected void init() {
		super.init();
		// Settings button in the top-right corner of the panel.
		this.addRenderableWidget(Button.builder(Component.literal("⚙"), button ->
				this.minecraft.gui.setScreen(new LinkleSettingsScreen(this)))
			.bounds(this.leftPos + this.imageWidth - 21, this.topPos + 3, 16, 13)
			.tooltip(Tooltip.create(Component.translatable("options.linkle_companion.title")))
			.build());
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractBackground(graphics, mouseX, mouseY, partialTick);
		graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, this.leftPos, this.topPos, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);
		LinkleEntity linkle = this.menu.getLinkle();
		if (linkle != null) {
			InventoryScreen.extractEntityInInventoryFollowsMouse(graphics, this.leftPos + 28, this.topPos + 18, this.leftPos + 92, this.topPos + 89,
				30, 0.0625F, this.xMouse, this.yMouse, linkle);
		}
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		this.xMouse = mouseX;
		this.yMouse = mouseY;
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
	}

	@Override
	protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
		super.extractLabels(graphics, mouseX, mouseY);
		LinkleEntity linkle = this.menu.getLinkle();
		if (linkle == null) {
			return;
		}
		int arrows = 0;
		for (int slot = 0; slot < linkle.getInventory().getContainerSize(); slot++) {
			ItemStack stack = this.menu.getSlot(4 + slot).getItem();
			if (stack.is(ItemTags.ARROWS)) {
				arrows += stack.getCount();
			}
		}
		boolean infinite = LinkleConfig.get().infiniteArrows;
		Component health = Component.translatable("gui.linkle_companion.health", Math.round(linkle.getHealth()));
		Component ammo = infinite ? Component.translatable("gui.linkle_companion.arrows_infinite") : Component.translatable("gui.linkle_companion.arrows", arrows);
		Component mode = Component.translatable("mode.linkle_companion." + linkle.getMode().id());
		graphics.text(this.font, mode, 98, 74, LABEL_COLOR, false);
		graphics.text(this.font, health, 98, 83, LABEL_COLOR, false);
		graphics.text(this.font, ammo, 98, this.inventoryLabelY, arrows == 0 && !infinite ? 0xFFAA2222 : LABEL_COLOR, false);
	}
}
