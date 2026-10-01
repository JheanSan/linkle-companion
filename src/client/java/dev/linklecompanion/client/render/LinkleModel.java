package dev.linklecompanion.client.render;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.util.Mth;

/**
 * The vanilla slim-arm player model plus two twin tails (four small cubes) on the head.
 *
 * <p>The tails read their texture from an area of the skin that vanilla never uses
 * ((24,0) to (40,8)), so any 64x64 skin works: if that area is empty, the tails are invisible.
 *
 * <p>Extra poses on top of vanilla: dual-crossbow aim, crossbow inspection, the volley spin pose
 * and the knocked-out slump. Charging uses the vanilla crossbow charge pose.
 */
public class LinkleModel extends PlayerModel {
	private static final float TAIL_REST_FLARE = 0.12F;

	private final ModelPart rightTail;
	private final ModelPart rightTailEnd;
	private final ModelPart leftTail;
	private final ModelPart leftTailEnd;

	public LinkleModel(ModelPart root) {
		super(root, true);
		this.rightTail = this.head.getChild("right_tail");
		this.rightTailEnd = this.rightTail.getChild("right_tail_end");
		this.leftTail = this.head.getChild("left_tail");
		this.leftTailEnd = this.leftTail.getChild("left_tail_end");
	}

	public static LayerDefinition createLayer() {
		MeshDefinition mesh = PlayerModel.createMesh(CubeDeformation.NONE, true);
		PartDefinition head = mesh.getRoot().getChild("head");
		// Each tail: an upper braid (2x5x2) hanging from just behind the ear, and a slightly thinner end piece.
		PartDefinition right = head.addOrReplaceChild("right_tail",
			CubeListBuilder.create().texOffs(24, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 5.0F, 2.0F),
			PartPose.offset(-4.6F, -4.5F, 1.2F));
		right.addOrReplaceChild("right_tail_end",
			CubeListBuilder.create().texOffs(32, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(-0.15F)),
			PartPose.offset(0.0F, 4.8F, 0.0F));
		PartDefinition left = head.addOrReplaceChild("left_tail",
			CubeListBuilder.create().texOffs(24, 0).mirror().addBox(-1.0F, 0.0F, -1.0F, 2.0F, 5.0F, 2.0F),
			PartPose.offset(4.6F, -4.5F, 1.2F));
		left.addOrReplaceChild("left_tail_end",
			CubeListBuilder.create().texOffs(32, 0).mirror().addBox(-1.0F, 0.0F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(-0.15F)),
			PartPose.offset(0.0F, 4.8F, 0.0F));
		return LayerDefinition.create(mesh, 64, 64);
	}

	@Override
	public void setupAnim(AvatarRenderState state) {
		super.setupAnim(state);
		if (!(state instanceof LinkleRenderState linkle)) {
			return;
		}
		poseArms(linkle);
		poseTails(linkle);
	}

	private void poseArms(LinkleRenderState state) {
		if (state.knockedOut) {
			rightArm.xRot = -0.2F;
			rightArm.yRot = 0.0F;
			rightArm.zRot = 0.35F;
			leftArm.xRot = 0.15F;
			leftArm.yRot = 0.0F;
			leftArm.zRot = -0.25F;
			head.xRot = 0.3F;
			head.yRot = 0.0F;
			return;
		}
		if (state.volley) {
			// Arms straight out to the sides while she spins.
			rightArm.xRot = -0.1F;
			rightArm.yRot = 0.0F;
			rightArm.zRot = Mth.HALF_PI * 0.95F;
			leftArm.xRot = -0.1F;
			leftArm.yRot = 0.0F;
			leftArm.zRot = -Mth.HALF_PI * 0.95F;
			return;
		}
		if (state.aiming && !state.isUsingItem) {
			// Both crossbows raised toward the target, converging slightly.
			rightArm.xRot = -Mth.HALF_PI + head.xRot;
			rightArm.yRot = head.yRot - 0.06F;
			rightArm.zRot = 0.0F;
			leftArm.xRot = -Mth.HALF_PI + head.xRot;
			leftArm.yRot = head.yRot + 0.06F;
			leftArm.zRot = 0.0F;
			return;
		}
		if (state.inspecting && !state.isUsingItem) {
			// Lifts the right crossbow in front of her face and looks it over.
			float wobble = Mth.sin(state.ageInTicks * 0.15F) * 0.08F;
			rightArm.xRot = -1.25F + wobble;
			rightArm.yRot = -0.45F;
			rightArm.zRot = 0.0F;
			head.xRot = 0.35F;
			head.yRot = -0.25F + wobble;
		}
	}

	private void poseTails(LinkleRenderState state) {
		boolean visible = state.hairEnabled;
		rightTail.visible = visible;
		leftTail.visible = visible;
		if (!visible) {
			return;
		}

		// Keep the tails hanging down even when she looks up or down.
		float hang = state.knockedOut ? 0.0F : -head.xRot;
		float trail = 0.0F;
		float bounce = 0.0F;
		float flare = TAIL_REST_FLARE;
		if (state.hairSway) {
			float speed = Math.min(1.0F, state.walkAnimationSpeed);
			trail = speed * 0.45F + Mth.sin(state.ageInTicks * 0.07F) * 0.035F;
			bounce = Mth.cos(state.walkAnimationPos * 0.6662F) * 0.14F * speed;
			if (state.volley) {
				flare += 0.9F;
			}
		}

		rightTail.xRot = hang + trail;
		rightTail.zRot = flare + bounce;
		leftTail.xRot = hang + trail;
		leftTail.zRot = -flare + bounce;
		rightTailEnd.xRot = trail * 0.6F;
		leftTailEnd.xRot = trail * 0.6F;
		rightTailEnd.zRot = bounce * 0.5F;
		leftTailEnd.zRot = bounce * 0.5F;
	}
}
