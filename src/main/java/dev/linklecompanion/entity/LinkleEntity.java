package dev.linklecompanion.entity;

import dev.linklecompanion.LinkleCompanion;
import dev.linklecompanion.config.LinkleConfig;
import dev.linklecompanion.dialogue.LinkleDialogue;
import dev.linklecompanion.dialogue.Topic;
import dev.linklecompanion.entity.goal.GiveWayGoal;
import dev.linklecompanion.entity.goal.GuardPositionGoal;
import dev.linklecompanion.entity.goal.InspectCrossbowsGoal;
import dev.linklecompanion.entity.goal.KnockedOutGoal;
import dev.linklecompanion.entity.goal.LinkleCrossbowAttackGoal;
import dev.linklecompanion.entity.goal.LinkleFollowOwnerGoal;
import dev.linklecompanion.entity.goal.LinkleStrollGoal;
import dev.linklecompanion.entity.goal.LinkleThreatTargetGoal;
import dev.linklecompanion.entity.goal.LookAtOwnerGoal;
import dev.linklecompanion.entity.goal.VolleyGoal;
import dev.linklecompanion.menu.LinkleMenu;
import dev.linklecompanion.registry.ModAttachments;
import dev.linklecompanion.registry.ModTags;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Linkle, the dual-crossbow companion.
 *
 * <p>Built on vanilla {@link TamableAnimal}, so ownership, saving, sitting, team handling and the
 * vanilla "defend my owner" target goals all come for free, and other mods that respect tamed pets
 * (claim mods, pet mods) treat her like any other pet.
 *
 * <p>Performance notes: expensive checks (scanning for threats, picking up arrows, owner checks,
 * dialogue) are throttled to once every 1-2 seconds, and nothing allocates per tick.
 */
public class LinkleEntity extends TamableAnimal implements CrossbowAttackMob {
	// ------------------------------------------------------------------ synced data
	private static final EntityDataAccessor<Byte> DATA_MODE = SynchedEntityData.defineId(LinkleEntity.class, EntityDataSerializers.BYTE);
	private static final EntityDataAccessor<Byte> DATA_STATE = SynchedEntityData.defineId(LinkleEntity.class, EntityDataSerializers.BYTE);
	private static final EntityDataAccessor<String> DATA_VARIANT = SynchedEntityData.defineId(LinkleEntity.class, EntityDataSerializers.STRING);

	private static final int STATE_KNOCKED_OUT = 1;
	private static final int STATE_CHARGING = 2;
	private static final int STATE_AIMING = 4;
	private static final int STATE_INSPECTING = 8;
	private static final int STATE_VOLLEY = 16;

	public static final int INVENTORY_SIZE = 9;
	private static final EntityDimensions KNOCKED_OUT_DIMENSIONS = EntityDimensions.scalable(0.6F, 0.5F).withEyeHeight(0.3F);

	// ------------------------------------------------------------------ server state
	private final SimpleContainer inventory = new SimpleContainer(INVENTORY_SIZE);
	private final LinkleDialogue dialogue = new LinkleDialogue(this);
	private @Nullable BlockPos guardPos;
	private int knockoutTicks;
	private int generation;
	private int volleyCooldown;
	private int eatCooldown;
	private int regenTimer;
	private boolean warnedNoQuickCharge;
	/** Was the owner within 32 blocks at the last check? Used to follow through portals. */
	private boolean nearOwner;

	/** Client only: ticks since the current volley spin started (drives the spin animation). */
	public int volleyAnimTicks;

	public LinkleEntity(EntityType<? extends LinkleEntity> type, Level level) {
		super(type, level);
		this.setTame(false, false);
		if (this.getNavigation() instanceof GroundPathNavigation navigation) {
			navigation.setCanOpenDoors(true);
		}
		// Stay out of lava, fire and powder snow. Cliffs are avoided by the default max fall distance.
		this.setPathfindingMalus(PathType.LAVA, -1.0F);
		this.setPathfindingMalus(PathType.FIRE, -1.0F);
		this.setPathfindingMalus(PathType.FIRE_IN_NEIGHBOR, 16.0F);
		this.setPathfindingMalus(PathType.POWDER_SNOW, -1.0F);
		this.setPathfindingMalus(PathType.DAMAGING, -1.0F);
		this.setPathfindingMalus(PathType.DAMAGING_IN_NEIGHBOR, 8.0F);
		this.setCanPickUpLoot(false);
		this.setPersistenceRequired();
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Animal.createAnimalAttributes()
			.add(Attributes.MAX_HEALTH, 30.0)
			.add(Attributes.MOVEMENT_SPEED, 0.32)
			.add(Attributes.FOLLOW_RANGE, 32.0)
			.add(Attributes.ARMOR, 2.0)
			.add(Attributes.ATTACK_DAMAGE, 2.0);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(DATA_MODE, (byte) LinkleMode.FOLLOW.ordinal());
		builder.define(DATA_STATE, (byte) 0);
		builder.define(DATA_VARIANT, LinkleVariant.CLASSIC.id());
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(1, new KnockedOutGoal(this));
		this.goalSelector.addGoal(2, new VolleyGoal(this));
		this.goalSelector.addGoal(3, new LinkleCrossbowAttackGoal(this));
		this.goalSelector.addGoal(4, new SitWhenOrderedToGoal(this));
		this.goalSelector.addGoal(5, new GuardPositionGoal(this));
		this.goalSelector.addGoal(6, new LinkleFollowOwnerGoal(this));
		this.goalSelector.addGoal(7, new GiveWayGoal(this));
		this.goalSelector.addGoal(8, new OpenDoorGoal(this, true));
		this.goalSelector.addGoal(9, new LinkleStrollGoal(this));
		this.goalSelector.addGoal(10, new InspectCrossbowsGoal(this));
		this.goalSelector.addGoal(11, new LookAtOwnerGoal(this));
		this.goalSelector.addGoal(12, new RandomLookAroundGoal(this));

		this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
		this.targetSelector.addGoal(2, new HurtByTargetGoal(this));
		this.targetSelector.addGoal(3, new OwnerHurtTargetGoal(this));
		this.targetSelector.addGoal(4, new LinkleThreatTargetGoal(this));
	}

	// ------------------------------------------------------------------ setup

	/** Called once when a brand-new Linkle is created for a player. */
	public void setupNew(ServerPlayer owner, int generation) {
		makeOwned(owner);
		this.generation = generation;
		this.setVariant(LinkleVariant.byId(LinkleConfig.get().defaultVariant));
		this.giveCrossbows();
		int arrows = LinkleConfig.get().startingArrows;
		while (arrows > 0) {
			int count = Math.min(64, arrows);
			this.inventory.addItem(new ItemStack(Items.ARROW, count));
			arrows -= count;
		}
		this.setHealth(this.getMaxHealth());
		this.setMode(LinkleMode.FOLLOW);
	}

	/** Called when a wild Linkle (from /summon) joins a player. */
	public void setupBefriended(ServerPlayer owner, int generation) {
		makeOwned(owner);
		this.generation = generation;
		if (this.getMainHandItem().isEmpty() || this.getOffhandItem().isEmpty()) {
			this.giveCrossbows();
		}
		if (findArrows() == null) {
			int arrows = Math.min(64, LinkleConfig.get().startingArrows);
			if (arrows > 0) {
				this.inventory.addItem(new ItemStack(Items.ARROW, arrows));
			}
		}
		this.setMode(LinkleMode.FOLLOW);
	}

	@Override
	public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason, @Nullable SpawnGroupData groupData) {
		SpawnGroupData data = super.finalizeSpawn(level, difficulty, reason, groupData);
		// Always an adult, always with her crossbows, even when spawned by /summon.
		this.setAge(0);
		this.giveCrossbows();
		// A name given at spawn (e.g. /summon ... {CustomName:"Azure"}) picks the skin, like a name tag.
		LinkleVariant fromName = this.hasCustomName() ? LinkleVariant.fromName(this.getCustomName().getString()) : null;
		this.setVariant(fromName != null ? fromName : LinkleVariant.byId(LinkleConfig.get().defaultVariant));
		return data;
	}

	/**
	 * Like vanilla tame(), but without firing the vanilla "tame an animal" advancement trigger:
	 * a summoned companion shouldn't hand out vanilla advancements.
	 */
	private void makeOwned(ServerPlayer owner) {
		this.setTame(true, true);
		this.setOwner(owner);
	}

	/** Gives her the two signature crossbows (unbreakable, quick-charge, no glint). */
	public void giveCrossbows() {
		this.setItemSlot(EquipmentSlot.MAINHAND, makeCrossbow());
		this.setItemSlot(EquipmentSlot.OFFHAND, makeCrossbow());
		// Her crossbows are part of her, never loot.
		this.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
		this.setDropChance(EquipmentSlot.OFFHAND, 0.0F);
	}

	private ItemStack makeCrossbow() {
		ItemStack crossbow = new ItemStack(Items.CROSSBOW);
		crossbow.set(DataComponents.UNBREAKABLE, Unit.INSTANCE);
		crossbow.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, false);
		var quickCharge = this.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(Enchantments.QUICK_CHARGE);
		if (quickCharge.isPresent()) {
			crossbow.enchant(quickCharge.get(), 2);
		} else if (!warnedNoQuickCharge) {
			warnedNoQuickCharge = true;
			LinkleCompanion.LOGGER.warn("Quick Charge enchantment is missing (removed by a data pack?); Linkle will reload at normal speed.");
		}
		return crossbow;
	}

	@Override
	public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
		return null;
	}

	@Override
	public boolean isFood(ItemStack stack) {
		// Feeding is handled in mobInteract; returning false keeps vanilla breeding out of the way.
		return false;
	}

	@Override
	public boolean removeWhenFarAway(double distanceSqr) {
		return false;
	}

	// ------------------------------------------------------------------ synced getters/setters

	public LinkleMode getMode() {
		return LinkleMode.byOrdinal(this.entityData.get(DATA_MODE));
	}

	public void setMode(LinkleMode mode) {
		this.entityData.set(DATA_MODE, (byte) mode.ordinal());
		this.setOrderedToSit(mode == LinkleMode.STAY);
		this.guardPos = mode == LinkleMode.GUARD ? this.blockPosition() : null;
		this.getNavigation().stop();
		if (mode == LinkleMode.STAY) {
			this.setTarget(null);
		}
	}

	public @Nullable BlockPos getGuardPos() {
		return guardPos;
	}

	public LinkleVariant getVariant() {
		return LinkleVariant.byId(this.entityData.get(DATA_VARIANT));
	}

	public void setVariant(LinkleVariant variant) {
		this.entityData.set(DATA_VARIANT, variant.id());
	}

	private boolean getState(int flag) {
		return (this.entityData.get(DATA_STATE) & flag) != 0;
	}

	private void setState(int flag, boolean value) {
		byte current = this.entityData.get(DATA_STATE);
		byte updated = (byte) (value ? current | flag : current & ~flag);
		if (updated != current) {
			this.entityData.set(DATA_STATE, updated);
		}
	}

	/** True while the master switch in the settings is off: she sits and does nothing. */
	public boolean isPaused() {
		return !LinkleConfig.get().enabled;
	}

	@Override
	public boolean isOrderedToSit() {
		// Paused Linkles sit like in Stay mode, without changing their saved mode.
		return super.isOrderedToSit() || isPaused();
	}

	public boolean isKnockedOut() {
		return getState(STATE_KNOCKED_OUT);
	}

	public boolean isChargingCrossbow() {
		return getState(STATE_CHARGING);
	}

	public boolean isAiming() {
		return getState(STATE_AIMING);
	}

	public void setAiming(boolean aiming) {
		setState(STATE_AIMING, aiming);
	}

	public boolean isInspecting() {
		return getState(STATE_INSPECTING);
	}

	public void setInspecting(boolean inspecting) {
		setState(STATE_INSPECTING, inspecting);
	}

	public boolean isVolleying() {
		return getState(STATE_VOLLEY);
	}

	public void setVolleying(boolean volleying) {
		setState(STATE_VOLLEY, volleying);
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
		super.onSyncedDataUpdated(accessor);
		if (DATA_STATE.equals(accessor)) {
			this.refreshDimensions();
		}
	}

	@Override
	public EntityDimensions getDefaultDimensions(Pose pose) {
		return isKnockedOut() ? KNOCKED_OUT_DIMENSIONS : super.getDefaultDimensions(pose);
	}

	public SimpleContainer getInventory() {
		return inventory;
	}

	public LinkleDialogue dialogue() {
		return dialogue;
	}

	public int getGeneration() {
		return generation;
	}

	public int getVolleyCooldown() {
		return volleyCooldown;
	}

	public void setVolleyCooldown(int ticks) {
		this.volleyCooldown = ticks;
	}

	/** Ownership check by UUID; works even when the owner is in another dimension. */
	public boolean isOwnedByPlayer(Player player) {
		var owner = this.getOwnerReference();
		return owner != null && owner.getUUID().equals(player.getUUID());
	}

	public boolean wasNearOwner() {
		return nearOwner;
	}

	public int getKnockoutTicks() {
		return knockoutTicks;
	}

	// ------------------------------------------------------------------ name tag → skin

	@Override
	public void setCustomName(@Nullable Component name) {
		super.setCustomName(name);
		if (name != null && !this.level().isClientSide()) {
			LinkleVariant variant = LinkleVariant.fromName(name.getString());
			if (variant != null) {
				this.setVariant(variant);
			}
		}
	}

	// ------------------------------------------------------------------ ticking

	@Override
	public void tick() {
		super.tick();
		if (this.level().isClientSide()) {
			volleyAnimTicks = isVolleying() ? volleyAnimTicks + 1 : 0;
		}
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);

		if (volleyCooldown > 0) {
			volleyCooldown--;
		}
		if (eatCooldown > 0) {
			eatCooldown--;
		}

		if (isKnockedOut()) {
			tickKnockedOut(level);
			return;
		}
		if (isPaused()) {
			if (this.getTarget() != null) {
				this.setTarget(null);
			}
			return;
		}

		dialogue.tick();

		// Throttled housekeeping, staggered by entity id so many Linkles don't all work on the same tick.
		int phase = (this.tickCount + this.getId()) % 20;
		if (phase == 0) {
			LivingEntity owner = this.getOwner();
			nearOwner = owner != null && owner.level() == level && this.distanceToSqr(owner) < 32.0 * 32.0;
			checkOwnership(level);
			tryEatFood();
			slowRegen();
		} else if (phase == 10) {
			pickUpArrows(level);
		}
	}

	/** Leaves if a newer Linkle has replaced this one (one per player). */
	private void checkOwnership(ServerLevel level) {
		if (!LinkleConfig.get().onePerPlayer || !(this.getOwner() instanceof ServerPlayer owner)) {
			return;
		}
		ModAttachments.CompanionData data = owner.getAttached(ModAttachments.COMPANION);
		if (data != null && data.generation() > this.generation && !data.linkleId().equals(this.getUUID())) {
			leaveWorld(level);
		}
	}

	/** Drops her inventory and armor, then disappears in a puff of smoke. */
	public void leaveWorld(ServerLevel level) {
		dropInventoryAndArmor(level);
		level.sendParticles(ParticleTypes.POOF, this.getX(), this.getY() + 1.0, this.getZ(), 20, 0.3, 0.6, 0.3, 0.02);
		this.discard();
	}

	private void slowRegen() {
		// Out of combat she slowly heals (1 HP every 10 seconds) so she is never stuck at low health.
		if (this.getTarget() == null && this.getHealth() < this.getMaxHealth() && ++regenTimer >= 10) {
			regenTimer = 0;
			this.heal(1.0F);
		}
	}

	private void tryEatFood() {
		if (!LinkleConfig.get().autoEat || eatCooldown > 0 || this.getHealth() > this.getMaxHealth() * 0.6F) {
			return;
		}
		for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
			ItemStack stack = inventory.getItem(slot);
			if (isLinkleFood(stack)) {
				eatFromStack(stack, true);
				inventory.setChanged();
				dialogue.say(Topic.EAT);
				eatCooldown = 60;
				return;
			}
		}
	}

	/** Eats one item from the stack: heals, plays sound and crumbs. */
	private void eatFromStack(ItemStack stack, boolean shrink) {
		var food = stack.get(DataComponents.FOOD);
		float heal = food != null ? Math.max(2.0F, food.nutrition() * 1.5F) : 2.0F;
		if (this.level() instanceof ServerLevel serverLevel) {
			serverLevel.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, stack.getItem()),
				this.getX(), this.getEyeY() - 0.2, this.getZ(), 8, 0.15, 0.1, 0.15, 0.05);
		}
		this.playSound(SoundEvents.GENERIC_EAT.value(), 0.8F, 0.9F + this.random.nextFloat() * 0.2F);
		this.heal(heal);
		if (shrink) {
			stack.shrink(1);
		}
	}

	/** Food she will eat: anything with food stats that isn't known to make you sick. */
	public static boolean isLinkleFood(ItemStack stack) {
		return !stack.isEmpty() && stack.has(DataComponents.FOOD) && !stack.is(ConventionalItemTags.FOOD_POISONING_FOODS);
	}

	private void pickUpArrows(ServerLevel level) {
		if (!LinkleConfig.get().pickUpArrows || LinkleConfig.get().infiniteArrows || !level.getGameRules().get(GameRules.MOB_GRIEFING)) {
			return;
		}
		AABB area = this.getBoundingBox().inflate(1.5, 0.5, 1.5);
		List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, area,
			item -> !item.hasPickUpDelay() && item.getItem().is(ItemTags.ARROWS));
		for (ItemEntity item : items) {
			ItemStack stack = item.getItem();
			int before = stack.getCount();
			ItemStack rest = inventory.addItem(stack);
			if (rest.getCount() != before) {
				this.take(item, before - rest.getCount());
				if (rest.isEmpty()) {
					item.discard();
				} else {
					item.setItem(rest);
				}
			}
		}
	}

	// ------------------------------------------------------------------ interaction

	@Override
	public InteractionResult mobInteract(Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (this.level().isClientSide()) {
			// Predict success for the owner (or a wild Linkle) so the hand swings without lag.
			return (this.isOwnedBy(player) || !this.isTame()) ? InteractionResult.SUCCESS : InteractionResult.PASS;
		}
		if (!(player instanceof ServerPlayer serverPlayer)) {
			return InteractionResult.PASS;
		}

		if (!this.isTame()) {
			return LinkleSummoning.befriend(this, serverPlayer) ? InteractionResult.SUCCESS_SERVER : InteractionResult.CONSUME;
		}
		if (!this.isOwnedBy(player)) {
			serverPlayer.sendOverlayMessage(Component.translatable("message.linkle_companion.not_yours"));
			return InteractionResult.CONSUME;
		}

		if (isLinkleFood(stack)) {
			feed(serverPlayer, stack);
			return InteractionResult.SUCCESS_SERVER;
		}
		if (stack.is(ItemTags.ARROWS) && stack.getItem() instanceof ArrowItem) {
			giveArrows(serverPlayer, stack);
			return InteractionResult.SUCCESS_SERVER;
		}
		if (player.isSecondaryUseActive()) {
			openInventory(serverPlayer);
			return InteractionResult.SUCCESS_SERVER;
		}
		if (isKnockedOut()) {
			serverPlayer.sendOverlayMessage(Component.translatable("message.linkle_companion.knocked_out_hint"));
			return InteractionResult.CONSUME;
		}

		LinkleMode next = this.getMode().next();
		this.setMode(next);
		serverPlayer.sendOverlayMessage(Component.translatable("message.linkle_companion.mode." + next.id()));
		return InteractionResult.SUCCESS_SERVER;
	}

	private void feed(ServerPlayer player, ItemStack stack) {
		if (isKnockedOut()) {
			eatFromStack(stack, !player.hasInfiniteMaterials());
			wakeUp(true);
		} else if (this.getHealth() < this.getMaxHealth()) {
			eatFromStack(stack, !player.hasInfiniteMaterials());
			dialogue.say(Topic.GIFT);
		} else {
			ItemStack one = stack.copyWithCount(1);
			if (!inventory.canAddItem(one)) {
				player.sendOverlayMessage(Component.translatable("message.linkle_companion.inventory_full"));
				return;
			}
			inventory.addItem(one);
			if (!player.hasInfiniteMaterials()) {
				stack.shrink(1);
			}
			dialogue.say(Topic.GIFT_SAVED);
		}
		// Hearts like vanilla taming, so the gift feels good.
		if (this.level() instanceof ServerLevel serverLevel) {
			serverLevel.sendParticles(ParticleTypes.HEART, this.getX(), this.getEyeY() + 0.3, this.getZ(), 3, 0.3, 0.2, 0.3, 0.0);
		}
	}

	private void giveArrows(ServerPlayer player, ItemStack stack) {
		ItemStack rest = inventory.addItem(stack.copy());
		int given = stack.getCount() - rest.getCount();
		if (given <= 0) {
			player.sendOverlayMessage(Component.translatable("message.linkle_companion.inventory_full"));
			return;
		}
		if (!player.hasInfiniteMaterials()) {
			stack.shrink(given);
		}
		this.playSound(SoundEvents.ITEM_PICKUP, 0.5F, 1.2F);
		dialogue.say(Topic.ARROWS);
	}

	public void openInventory(ServerPlayer player) {
		player.openMenu(new ExtendedMenuProvider<Integer>() {
			@Override
			public Integer getScreenOpeningData(ServerPlayer serverPlayer) {
				return LinkleEntity.this.getId();
			}

			@Override
			public Component getDisplayName() {
				return LinkleEntity.this.getDisplayName();
			}

			@Override
			public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player menuPlayer) {
				return new LinkleMenu(containerId, playerInventory, LinkleEntity.this);
			}
		});
	}

	// ------------------------------------------------------------------ friends and foes

	/**
	 * True for anything Linkle must never hurt: her owner, allies and teammates, anyone's pets,
	 * villagers and everything in {@code #linkle_companion:never_target}, and players when PvP
	 * between them and her owner isn't allowed.
	 */
	public boolean isFriendlyTo(Entity entity) {
		if (entity == this) {
			return true;
		}
		LivingEntity owner = this.getOwner();
		if (entity == owner || this.isAlliedTo(entity)) {
			return true;
		}
		if (entity.is(ModTags.NEVER_TARGET) || entity instanceof AbstractVillager) {
			return true;
		}
		if (entity instanceof OwnableEntity ownable && ownable.getOwnerReference() != null) {
			return true;
		}
		if (entity instanceof Player player) {
			return !(owner instanceof Player ownerPlayer) || !ownerPlayer.canHarmPlayer(player);
		}
		return false;
	}

	@Override
	public boolean canAttack(LivingEntity target) {
		if (isKnockedOut() || isPaused() || isFriendlyTo(target) || target.is(ModTags.IGNORED_TARGETS)) {
			return false;
		}
		return super.canAttack(target);
	}

	@Override
	public boolean wantsToAttack(LivingEntity target, LivingEntity owner) {
		return !isFriendlyTo(target) && !target.is(ModTags.IGNORED_TARGETS);
	}

	@Override
	public void setTarget(@Nullable LivingEntity target) {
		if (target != null && isKnockedOut()) {
			return;
		}
		super.setTarget(target);
	}

	@Override
	public boolean canBeSeenAsEnemy() {
		// Monsters lose interest in a knocked-out Linkle.
		return !isKnockedOut() && super.canBeSeenAsEnemy();
	}

	@Override
	public boolean killedEntity(ServerLevel level, LivingEntity victim, DamageSource source) {
		dialogue.onKill();
		return super.killedEntity(level, victim, source);
	}

	// ------------------------------------------------------------------ damage and knockout

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
		if (isKnockedOut() && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
			return false;
		}
		boolean hurt = super.hurtServer(level, source, damage);
		if (hurt && !isKnockedOut() && this.isAlive() && this.getHealth() < this.getMaxHealth() * 0.3F) {
			dialogue.say(Topic.LOW_HEALTH);
		}
		return hurt;
	}

	@Override
	public void die(DamageSource source) {
		if (!this.level().isClientSide() && !LinkleConfig.get().realDeath && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
			knockOut();
			return;
		}
		if (this.level() instanceof ServerLevel serverLevel) {
			dropInventoryAndArmor(serverLevel);
		}
		super.die(source);
	}

	/** Instead of dying: falls over, stops fighting and waits to recover. */
	public void knockOut() {
		this.setHealth(1.0F);
		this.stopUsingItem();
		this.setAiming(false);
		this.setVolleying(false);
		this.setInspecting(false);
		setState(STATE_CHARGING, false);
		this.setTarget(null);
		this.getNavigation().stop();
		this.clearFire();
		this.knockoutTicks = LinkleConfig.get().knockoutSeconds * 20;
		setState(STATE_KNOCKED_OUT, true);
		this.playSound(SoundEvents.PLAYER_HURT, 1.0F, 1.3F);
		dialogue.say(Topic.KNOCKED_OUT);
	}

	private void tickKnockedOut(ServerLevel level) {
		if (--knockoutTicks <= 0) {
			wakeUp(false);
			return;
		}
		if (knockoutTicks % 30 == 0) {
			// Little "dazed" stars above her head (vanilla particles only).
			level.sendParticles(ParticleTypes.CRIT, this.getX(), this.getY() + 0.8, this.getZ(), 4, 0.25, 0.1, 0.25, 0.0);
		}
	}

	/** Gets back up. Feeding her gives a stronger recovery than waiting. */
	public void wakeUp(boolean fed) {
		setState(STATE_KNOCKED_OUT, false);
		this.knockoutTicks = 0;
		this.setHealth(Math.max(this.getHealth(), this.getMaxHealth() * (fed ? 0.5F : 0.3F)));
		if (this.level() instanceof ServerLevel serverLevel) {
			serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY() + 1.0, this.getZ(), 10, 0.3, 0.5, 0.3, 0.0);
		}
		this.playSound(SoundEvents.PLAYER_LEVELUP, 0.4F, 1.6F);
		dialogue.say(Topic.REVIVED);
	}

	private void dropInventoryAndArmor(ServerLevel level) {
		for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
			ItemStack stack = inventory.removeItemNoUpdate(slot);
			if (!stack.isEmpty()) {
				this.spawnAtLocation(level, stack);
			}
		}
		for (EquipmentSlot slot : new EquipmentSlot[] {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
			ItemStack stack = this.getItemBySlot(slot);
			if (!stack.isEmpty()) {
				this.spawnAtLocation(level, stack);
				this.setItemSlot(slot, ItemStack.EMPTY);
			}
		}
	}

	@Override
	protected @Nullable SoundEvent getHurtSound(DamageSource source) {
		return SoundEvents.PLAYER_HURT;
	}

	@Override
	protected @Nullable SoundEvent getDeathSound() {
		return SoundEvents.PLAYER_DEATH;
	}

	@Override
	protected @Nullable SoundEvent getAmbientSound() {
		return null;
	}

	// ------------------------------------------------------------------ crossbows and arrows

	/**
	 * Vanilla crossbows ask this when loading. Returning the real stack lets vanilla use up one
	 * arrow from her inventory; with infinite arrows a fresh arrow is made for each load.
	 */
	@Override
	public ItemStack getProjectile(ItemStack weapon) {
		if (LinkleConfig.get().infiniteArrows) {
			return new ItemStack(Items.ARROW);
		}
		ItemStack arrows = findArrows();
		return arrows == null ? ItemStack.EMPTY : arrows;
	}

	private @Nullable ItemStack findArrows() {
		for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
			ItemStack stack = inventory.getItem(slot);
			if (stack.is(ItemTags.ARROWS) && stack.getItem() instanceof ArrowItem) {
				return stack;
			}
		}
		return null;
	}

	/** Has at least one arrow to load (or infinite arrows). Cheap: no allocation. */
	public boolean hasAmmo() {
		return LinkleConfig.get().infiniteArrows || findArrows() != null;
	}

	@Override
	public void setChargingCrossbow(boolean charging) {
		setState(STATE_CHARGING, charging);
	}

	@Override
	public void onCrossbowAttackPerformed() {
		this.noActionTime = 0;
	}

	@Override
	public void performRangedAttack(LivingEntity target, float power) {
		// Not used: LinkleCrossbowAttackGoal fires each crossbow itself (see CombatHelper).
	}

	/**
	 * Applies the config damage multiplier and the right pickup rule to one of her bolts.
	 * Real arrows can be picked back up; infinite or special bolts can't.
	 */
	public void prepareBolt(AbstractArrow arrow, boolean realArrow) {
		arrow.setBaseDamage(2.0 * LinkleConfig.get().damageMultiplier);
		arrow.setSoundEvent(SoundEvents.CROSSBOW_HIT);
		arrow.pickup = realArrow && !LinkleConfig.get().infiniteArrows ? AbstractArrow.Pickup.ALLOWED : AbstractArrow.Pickup.DISALLOWED;
	}

	/**
	 * True if nothing friendly stands between her and the target. Runs only right before a shot,
	 * never every tick.
	 */
	public boolean isLineOfFireClear(LivingEntity target) {
		Vec3 from = this.getEyePosition();
		Vec3 to = target.getBoundingBox().getCenter();
		AABB path = new AABB(from, to).inflate(1.0);
		List<Entity> blockers = this.level().getEntities(this, path, entity -> entity != target && entity instanceof LivingEntity && isFriendlyTo(entity));
		for (Entity blocker : blockers) {
			if (blocker.getBoundingBox().inflate(0.4).clip(from, to).isPresent()) {
				return false;
			}
		}
		return true;
	}

	// ------------------------------------------------------------------ save / load

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.putString("Mode", this.getMode().id());
		output.putString("Variant", this.getVariant().id());
		output.putBoolean("KnockedOut", this.isKnockedOut());
		output.putInt("KnockoutTicks", this.knockoutTicks);
		output.putInt("Generation", this.generation);
		output.putInt("VolleyCooldown", this.volleyCooldown);
		output.storeNullable("GuardPos", BlockPos.CODEC, this.guardPos);
		this.inventory.storeAsItemList(output.list("Inventory", ItemStack.CODEC));
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		LinkleMode mode = LinkleMode.byId(input.getStringOr("Mode", "follow"));
		this.entityData.set(DATA_MODE, (byte) (mode == null ? LinkleMode.FOLLOW : mode).ordinal());
		this.setOrderedToSit(this.getMode() == LinkleMode.STAY);
		this.setVariant(LinkleVariant.byId(input.getStringOr("Variant", LinkleVariant.CLASSIC.id())));
		setState(STATE_KNOCKED_OUT, input.getBooleanOr("KnockedOut", false));
		this.knockoutTicks = input.getIntOr("KnockoutTicks", 0);
		this.generation = input.getIntOr("Generation", 0);
		this.volleyCooldown = input.getIntOr("VolleyCooldown", 0);
		this.guardPos = input.read("GuardPos", BlockPos.CODEC).orElse(null);
		if (this.getMode() == LinkleMode.GUARD && this.guardPos == null) {
			this.guardPos = this.blockPosition();
		}
		this.inventory.clearContent();
		input.list("Inventory", ItemStack.CODEC).ifPresent(this.inventory::fromItemList);
		if (this.getMainHandItem().isEmpty() || this.getOffhandItem().isEmpty()) {
			this.giveCrossbows();
		}
	}
}
