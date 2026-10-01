package dev.linklecompanion.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.linklecompanion.LinkleCompanion;
import dev.linklecompanion.config.LinkleConfig;
import dev.linklecompanion.entity.LinkleEntity;
import dev.linklecompanion.entity.LinkleVariant;
import dev.linklecompanion.entity.goal.VolleyGoal;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.phys.Vec3;

import java.util.EnumMap;
import java.util.Map;

/**
 * Draws Linkle: slim player mesh + twin tails, vanilla armor, held items, head items and elytra
 * (the last three come from HumanoidMobRenderer). Standard render types only, so shader packs
 * treat her like any other mob.
 */
public class LinkleRenderer extends HumanoidMobRenderer<LinkleEntity, LinkleRenderState, LinkleModel> {
	public static final ModelLayerLocation LAYER = new ModelLayerLocation(LinkleCompanion.id("linkle"), "main");
	/** Hitbox is 1.75 tall vs the player's 1.8, so draw the player-sized model a little smaller. */
	private static final float MODEL_SCALE = 0.9375F * (1.75F / 1.8F);
	private static final Vec3 SITTING_OFFSET = new Vec3(0.0, -0.56, 0.0);

	/** One texture id per skin, made once (no per-frame allocation). Resource packs can replace any of them. */
	private static final Map<LinkleVariant, Identifier> TEXTURES = new EnumMap<>(LinkleVariant.class);

	static {
		for (LinkleVariant variant : LinkleVariant.values()) {
			TEXTURES.put(variant, LinkleCompanion.id("textures/entity/linkle/" + variant.id() + ".png"));
		}
	}

	public LinkleRenderer(EntityRendererProvider.Context context) {
		super(context, new LinkleModel(context.bakeLayer(LAYER)), 0.45F);
		this.addLayer(new HumanoidArmorLayer<>(
			this,
			ArmorModelSet.bake(ModelLayers.PLAYER_SLIM_ARMOR, context.getModelSet(), HumanoidModel<LinkleRenderState>::new),
			context.getEquipmentRenderer()
		));
	}

	@Override
	public LinkleRenderState createRenderState() {
		return new LinkleRenderState();
	}

	@Override
	public void extractRenderState(LinkleEntity entity, LinkleRenderState state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		state.texture = TEXTURES.get(entity.getVariant());
		state.knockedOut = entity.isKnockedOut();
		state.aiming = entity.isAiming();
		state.charging = entity.isChargingCrossbow();
		state.inspecting = entity.isInspecting();
		state.volley = entity.isVolleying();
		state.volleyProgress = state.volley ? Mth.clamp((entity.volleyAnimTicks + partialTicks) / VolleyGoal.DURATION, 0.0F, 1.0F) : 0.0F;
		state.sitting = entity.isInSittingPose() && !state.knockedOut;
		if (state.sitting) {
			// The vanilla "riding" pose is exactly a sitting pose: legs forward.
			state.isPassenger = true;
		}
		LinkleConfig config = LinkleConfig.get();
		state.hairEnabled = config.hairEnabled;
		state.hairSway = config.hairSway;
	}

	/** Vanilla crossbow charge pose while loading; otherwise she simply holds her crossbows. */
	@Override
	protected HumanoidModel.ArmPose getArmPose(LinkleEntity entity, HumanoidArm arm) {
		ItemStack stack = entity.getItemHeldByArm(arm);
		if (stack.isEmpty()) {
			return HumanoidModel.ArmPose.EMPTY;
		}
		if (entity.isUsingItem() && entity.getUsedItemHand().asArm(entity.getMainArm()) == arm
			&& stack.getUseAnimation() == ItemUseAnimation.CROSSBOW) {
			return HumanoidModel.ArmPose.CROSSBOW_CHARGE;
		}
		return HumanoidModel.ArmPose.ITEM;
	}

	@Override
	public Identifier getTextureLocation(LinkleRenderState state) {
		return state.texture;
	}

	@Override
	protected void setupRotations(LinkleRenderState state, PoseStack poseStack, float bodyRot, float entityScale) {
		if (state.volley) {
			// Two full turns over the volley; smooth because progress includes partial ticks.
			bodyRot += state.volleyProgress * 720.0F;
		}
		super.setupRotations(state, poseStack, bodyRot, entityScale);
		if (state.knockedOut) {
			// Lies on her side, like the final frame of the vanilla death animation.
			poseStack.rotateDegrees(Axis.ZP, this.getFlipDegrees());
		}
	}

	@Override
	protected void scale(LinkleRenderState state, PoseStack poseStack) {
		poseStack.scale(MODEL_SCALE, MODEL_SCALE, MODEL_SCALE);
	}

	@Override
	public Vec3 getRenderOffset(LinkleRenderState state) {
		return state.sitting ? SITTING_OFFSET : super.getRenderOffset(state);
	}
}
