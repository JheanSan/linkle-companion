package dev.linklecompanion.dialogue;

import dev.linklecompanion.config.LinkleConfig;
import dev.linklecompanion.entity.LinkleEntity;
import dev.linklecompanion.network.DialoguePayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.biome.Biome;

/**
 * Decides when Linkle speaks. One instance per Linkle, server side only.
 *
 * <p>Two layers of anti-spam: every topic has its own cooldown, and chatter topics also wait for a
 * global gap since her last line. Periodic checks (night, biome, idle) run every 2 seconds, not
 * every tick, and allocate nothing unless she actually speaks.
 */
public final class LinkleDialogue {
	private static final int CHECK_INTERVAL = 40;
	private static final int GLOBAL_GAP_TICKS = 300;
	private static final int FIGHT_QUIET_TICKS = 100;

	/** Biome tags checked in order; the first match wins. */
	@SuppressWarnings("unchecked")
	private static final TagKey<Biome>[] BIOME_TAGS = new TagKey[] {
		BiomeTags.IS_NETHER, BiomeTags.IS_END, ConventionalBiomeTags.IS_MUSHROOM, BiomeTags.IS_OCEAN,
		BiomeTags.IS_BEACH, ConventionalBiomeTags.IS_DESERT, BiomeTags.IS_BADLANDS, ConventionalBiomeTags.IS_SNOWY,
		BiomeTags.IS_JUNGLE, ConventionalBiomeTags.IS_SWAMP, BiomeTags.IS_MOUNTAIN, ConventionalBiomeTags.IS_CAVE,
		BiomeTags.IS_FOREST, ConventionalBiomeTags.IS_PLAINS
	};
	private static final Topic[] BIOME_TOPICS = {
		Topic.BIOME_NETHER, Topic.BIOME_END, Topic.BIOME_MUSHROOM, Topic.BIOME_OCEAN,
		Topic.BIOME_BEACH, Topic.BIOME_DESERT, Topic.BIOME_BADLANDS, Topic.BIOME_SNOWY,
		Topic.BIOME_JUNGLE, Topic.BIOME_SWAMP, Topic.BIOME_MOUNTAIN, Topic.BIOME_CAVE,
		Topic.BIOME_FOREST, Topic.BIOME_PLAINS
	};
	/** How many recently visited biome categories she remembers, so borders don't make her repeat herself. */
	private static final int BIOME_MEMORY = 4;

	private final LinkleEntity linkle;
	private final long[] nextAllowed = new long[Topic.values().length];
	private long lastLineTime = -GLOBAL_GAP_TICKS;
	private final Topic[] recentBiomes = new Topic[BIOME_MEMORY];
	private int recentBiomeIndex;
	private Holder<Biome> lastBiome;
	private boolean wasDark;
	private boolean darkKnown;

	// fight tracking for "big fight ending"
	private int fightTicks;
	private int fightKills;
	private int quietTicks;

	public LinkleDialogue(LinkleEntity linkle) {
		this.linkle = linkle;
	}

	/** Called every server tick; does real work only every {@value #CHECK_INTERVAL} ticks. */
	public void tick() {
		boolean fighting = linkle.getTarget() != null;
		if (fighting) {
			fightTicks++;
			quietTicks = 0;
		} else if (fightTicks > 0) {
			if (++quietTicks >= FIGHT_QUIET_TICKS) {
				if (fightKills >= 3 || fightTicks >= 400) {
					say(Topic.FIGHT_WON);
				}
				fightTicks = 0;
				fightKills = 0;
				quietTicks = 0;
			}
		}

		if ((linkle.tickCount + linkle.getId()) % CHECK_INTERVAL != 0 || linkle.isKnockedOut()) {
			return;
		}

		LivingEntity owner = linkle.getOwner();
		if (owner == null) {
			return;
		}

		boolean dark = linkle.level().isDarkOutside();
		if (darkKnown && dark && !wasDark) {
			say(Topic.NIGHT);
		}
		wasDark = dark;
		darkKnown = true;

		if (owner.getHealth() < owner.getMaxHealth() * 0.3F && owner.isAlive()) {
			say(Topic.OWNER_HURT);
		}

		checkBiome();

		if (!fighting && linkle.getRandom().nextInt(30) == 0) {
			say(Topic.IDLE);
		}
	}

	private void checkBiome() {
		Holder<Biome> biome = linkle.level().getBiome(linkle.blockPosition());
		if (biome == lastBiome) {
			return;
		}
		boolean firstCheck = lastBiome == null;
		lastBiome = biome;

		Topic topic = Topic.BIOME_GENERIC;
		for (int i = 0; i < BIOME_TAGS.length; i++) {
			if (biome.is(BIOME_TAGS[i])) {
				topic = BIOME_TOPICS[i];
				break;
			}
		}

		for (Topic recent : recentBiomes) {
			if (recent == topic) {
				return;
			}
		}
		recentBiomes[recentBiomeIndex] = topic;
		recentBiomeIndex = (recentBiomeIndex + 1) % BIOME_MEMORY;
		if (!firstCheck) {
			say(topic);
		}
	}

	public void onKill() {
		fightKills++;
	}

	/** Says a random line of this topic to the owner, if its cooldowns allow. Returns true if spoken. */
	public boolean say(Topic topic) {
		if (!(linkle.getOwner() instanceof ServerPlayer owner)) {
			return false;
		}
		String chattiness = LinkleConfig.get().chattiness;
		if (linkle.isPaused() || ("quiet".equals(chattiness) && !topic.important)) {
			return false;
		}
		// Chatty halves the waits, normal uses them as they are.
		int divisor = "chatty".equals(chattiness) ? 2 : 1;
		long now = linkle.level().getGameTime();
		if (now < nextAllowed[topic.ordinal()]) {
			return false;
		}
		if (!topic.important && now - lastLineTime < GLOBAL_GAP_TICKS / divisor) {
			return false;
		}
		if (owner.level() != linkle.level() || owner.distanceToSqr(linkle) > 64.0 * 64.0) {
			return false;
		}

		nextAllowed[topic.ordinal()] = now + topic.cooldownTicks / divisor;
		lastLineTime = now;
		String key = topic.key(linkle.getRandom().nextInt(topic.lineCount()));
		if (ServerPlayNetworking.canSend(owner, DialoguePayload.TYPE)) {
			ServerPlayNetworking.send(owner, new DialoguePayload(linkle.getVariant().id(), key));
		}
		return true;
	}
}
