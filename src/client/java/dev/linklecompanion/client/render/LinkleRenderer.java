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
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ArrowLayer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.phys.Vec3;

import java.util.EnumMap;
import java.util.Map;

/**
 * Draws Linkle with the vanilla slim player model (plus twin tails), vanilla armor, held items and
 * stuck-arrow layers. Uses standard render types only, so shader packs treat her like any player.
 */
public class LinkleRenderer extends MobRenderer<LinkleEntity, AvatarRenderState, PlayerModel> {
	public static final ModelLayerLocation LAYER = new ModelLayerLocation(LinkleCompanion.id("linkle"), "main");
	/** Hitbox is 1.75 tall vs the player's 1.8, so draw the player model a little smaller. */
	private static final float MODEL_SCALE = 0.9375F * (1.75F / 1.8F);
	private static final Vec3 SITTING_OFFSET = new Vec3(0.0, -0.56, 0.0);

	/** One texture per skin, created once (no per-frame allocation). Resource packs can replace any of them. */
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
			ArmorModelSet.bake(ModelLayers.PLAYER_SLIM_ARMOR, context.getModelSet(), part -> new PlayerModel(part, true)),
			context.getEquipmentRenderer()
		));
		this.addLayer(new ItemInHandLayer<>(this));
		this.addLayer(new ArrowLayer<>(this, context));
		this.addLayer(new CustomHeadLayer<>(this, context.getModelSet(), context.getPlayerSkinRenderCache()));
	}

	@Override
	public AvatarRenderState createRenderState() {
		return new LinkleRenderState();
	}

	@Override
	public void extractRenderState(LinkleEntity entity, AvatarRenderState state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		HumanoidMobRenderer.extractHumanoidRenderState(entity, state, partialTicks, this.itemModelResolver);
		state.rightArmPose = armPose(entity, HumanoidArm.RIGHT);
		state.leftArmPose = armPose(entity, HumanoidArm.LEFT);
		state.arrowCount = entity.getArrowCount();
		state.id = entity.getId();

		LinkleRenderState linkle = (LinkleRenderState) state;
		linkle.texture = TEXTURES.get(entity.getVariant());
		linkle.knockedOut = entity.isKnockedOut();
		linkle.aiming = entity.isAiming();
		linkle.charging = entity.isChargingCrossbow();
		linkle.inspecting = entity.isInspecting();
		linkle.volley = entity.isVolleying();
		linkle.volleyProgress = linkle.volley ? Mth.clamp((entity.volleyAnimTicks + partialTicks) / VolleyGoal.DURATION, 0.0F, 1.0F) : 0.0F;
		linkle.sitting = entity.isInSittingPose() && !linkle.knockedOut;
		if (linkle.sitting) {
			// The vanilla "riding" pose is exactly a sitting pose: legs forward.
			state.isPassenger = true;
		}
		LinkleConfig config = LinkleConfig.get();
		linkle.hairEnabled = config.hairEnabled;
		linkle.hairSway = config.hairSway;
	}

	/** Vanilla crossbow charge pose while loading; otherwise she simply holds her crossbows. */
	private static HumanoidModel.ArmPose armPose(LinkleEntity entity, HumanoidArm arm) {
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
	public Identifier getTextureLocation(AvatarRenderState state) {
		return ((LinkleRenderState) state).texture;
	}

	@Override
	protected void setupRotations(AvatarRenderState state, PoseStack poseStack, float bodyRot, float entityScale) {
		LinkleRenderState linkle = (LinkleRenderState) state;
		if (linkle.volley) {
			// Two full turns over the volley; smooth because progress includes partial ticks.
			bodyRot += linkle.volleyProgress * 720.0F;
		}
		super.setupRotations(state, poseStack, bodyRot, entityScale);
		if (linkle.knockedOut) {
			// Lies on her side, like the final frame of the vanilla death animation.
			poseStack.rotateDegrees(Axis.ZP, this.getFlipDegrees());
		}
	}

	@Override
	protected void scale(AvatarRenderState state, PoseStack poseStack) {
		poseStack.scale(MODEL_SCALE, MODEL_SCALE, MODEL_SCALE);
	}

	@Override
	public Vec3 getRenderOffset(AvatarRenderState state) {
		return ((LinkleRenderState) state).sitting ? SITTING_OFFSET : super.getRenderOffset(state);
	}
}
