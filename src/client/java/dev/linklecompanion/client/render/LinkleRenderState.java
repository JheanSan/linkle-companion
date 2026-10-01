package dev.linklecompanion.client.render;

import dev.linklecompanion.LinkleCompanion;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.Identifier;

/**
 * Everything the model needs to draw one frame of Linkle.
 *
 * <p>Note: this deliberately does NOT extend the player's AvatarRenderState. Minecraft sends every
 * AvatarRenderState to the player renderer (which would draw Steve), and skin mods hook that
 * renderer; keeping Linkle separate avoids both.
 */
public class LinkleRenderState extends HumanoidRenderState {
	public Identifier texture = LinkleCompanion.id("textures/entity/linkle/classic.png");
	public boolean knockedOut;
	public boolean aiming;
	public boolean charging;
	public boolean inspecting;
	public boolean volley;
	/** 0..1 through the volley spin. */
	public float volleyProgress;
	public boolean sitting;
	public boolean hairEnabled = true;
	public boolean hairSway = true;
}
