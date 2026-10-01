package dev.linklecompanion.client.render;

import dev.linklecompanion.LinkleCompanion;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.resources.Identifier;

/**
 * Everything the model needs to draw one frame of Linkle. Extends the player's render state so the
 * vanilla player model and its layers (armor, held items, stuck arrows) work unchanged.
 */
public class LinkleRenderState extends AvatarRenderState {
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
