package net.tangentmc.nmsUtils.v1_9_R1;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.bukkit.entity.EntityType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.server.v1_9_R1.BiomeBase;
import net.minecraft.server.v1_9_R1.BiomeBase.BiomeMeta;
import net.minecraft.server.v1_9_R1.Entity;
import net.minecraft.server.v1_9_R1.EntityArmorStand;
import net.minecraft.server.v1_9_R1.EntityBat;
import net.minecraft.server.v1_9_R1.EntityBlaze;
import net.minecraft.server.v1_9_R1.EntityBoat;
import net.minecraft.server.v1_9_R1.EntityCaveSpider;
import net.minecraft.server.v1_9_R1.EntityChicken;
import net.minecraft.server.v1_9_R1.EntityCow;
import net.minecraft.server.v1_9_R1.EntityCreeper;
import net.minecraft.server.v1_9_R1.EntityEgg;
import net.minecraft.server.v1_9_R1.EntityEnderCrystal;
import net.minecraft.server.v1_9_R1.EntityEnderDragon;
import net.minecraft.server.v1_9_R1.EntityEnderPearl;
import net.minecraft.server.v1_9_R1.EntityEnderSignal;
import net.minecraft.server.v1_9_R1.EntityEnderman;
import net.minecraft.server.v1_9_R1.EntityEndermite;
import net.minecraft.server.v1_9_R1.EntityExperienceOrb;
import net.minecraft.server.v1_9_R1.EntityFallingBlock;
import net.minecraft.server.v1_9_R1.EntityFireworks;
import net.minecraft.server.v1_9_R1.EntityGhast;
import net.minecraft.server.v1_9_R1.EntityGiantZombie;
import net.minecraft.server.v1_9_R1.EntityGuardian;
import net.minecraft.server.v1_9_R1.EntityHorse;
import net.minecraft.server.v1_9_R1.EntityInsentient;
import net.minecraft.server.v1_9_R1.EntityIronGolem;
import net.minecraft.server.v1_9_R1.EntityItem;
import net.minecraft.server.v1_9_R1.EntityItemFrame;
import net.minecraft.server.v1_9_R1.EntityLargeFireball;
import net.minecraft.server.v1_9_R1.EntityLeash;
import net.minecraft.server.v1_9_R1.EntityMagmaCube;
import net.minecraft.server.v1_9_R1.EntityMinecartAbstract;
import net.minecraft.server.v1_9_R1.EntityMinecartChest;
import net.minecraft.server.v1_9_R1.EntityMinecartCommandBlock;
import net.minecraft.server.v1_9_R1.EntityMinecartFurnace;
import net.minecraft.server.v1_9_R1.EntityMinecartHopper;
import net.minecraft.server.v1_9_R1.EntityMinecartMobSpawner;
import net.minecraft.server.v1_9_R1.EntityMinecartRideable;
import net.minecraft.server.v1_9_R1.EntityMinecartTNT;
import net.minecraft.server.v1_9_R1.EntityMushroomCow;
import net.minecraft.server.v1_9_R1.EntityOcelot;
import net.minecraft.server.v1_9_R1.EntityPainting;
import net.minecraft.server.v1_9_R1.EntityPig;
import net.minecraft.server.v1_9_R1.EntityPigZombie;
import net.minecraft.server.v1_9_R1.EntityPotion;
import net.minecraft.server.v1_9_R1.EntityRabbit;
import net.minecraft.server.v1_9_R1.EntitySheep;
import net.minecraft.server.v1_9_R1.EntitySilverfish;
import net.minecraft.server.v1_9_R1.EntitySkeleton;
import net.minecraft.server.v1_9_R1.EntitySlime;
import net.minecraft.server.v1_9_R1.EntitySmallFireball;
import net.minecraft.server.v1_9_R1.EntitySnowball;
import net.minecraft.server.v1_9_R1.EntitySnowman;
import net.minecraft.server.v1_9_R1.EntitySpider;
import net.minecraft.server.v1_9_R1.EntitySquid;
import net.minecraft.server.v1_9_R1.EntityTNTPrimed;
import net.minecraft.server.v1_9_R1.EntityThrownExpBottle;
import net.minecraft.server.v1_9_R1.EntityTippedArrow;
import net.minecraft.server.v1_9_R1.EntityTypes;
import net.minecraft.server.v1_9_R1.EntityVillager;
import net.minecraft.server.v1_9_R1.EntityWitch;
import net.minecraft.server.v1_9_R1.EntityWither;
import net.minecraft.server.v1_9_R1.EntityWitherSkull;
import net.minecraft.server.v1_9_R1.EntityWolf;
import net.minecraft.server.v1_9_R1.EntityZombie;
import net.minecraft.server.v1_9_R1.EnumCreatureType;
import net.tangentmc.nmsUtils.utils.ReflectionManager;
import net.tangentmc.nmsUtils.v1_9_R1.entities.CraftHologramEntity.CraftHologramPart.BlockHologramEntity;
import net.tangentmc.nmsUtils.v1_9_R1.entities.CraftHologramEntity.CraftHologramPart.BlockHologramEntity.HologramBlock;
import net.tangentmc.nmsUtils.v1_9_R1.entities.CraftHologramEntity.CraftHologramPart.ItemHologramEntity;
import net.tangentmc.nmsUtils.v1_9_R1.entities.CraftHologramEntity.CraftHologramPart.ItemHologramEntity.HologramItem;
import net.tangentmc.nmsUtils.v1_9_R1.entities.CraftHologramEntity.CraftHologramPart.TextHologramEntity;
import net.tangentmc.nmsUtils.v1_9_R1.entities.LaserEntities.CraftLaserSourceEntity.LaserSourceEntity;
import net.tangentmc.nmsUtils.v1_9_R1.entities.basic.CraftArmorStandEntity.ArmorStandEntity;
import net.tangentmc.nmsUtils.v1_9_R1.entities.basic.CraftBatEntity.BatEntity;
import net.tangentmc.nmsUtils.v1_9_R1.entities.basic.CraftBlazeEntity.BlazeEntity;
import net.tangentmc.nmsUtils.v1_9_R1.entities.basic.CraftBoatEntity.BoatEntity;
import net.tangentmc.nmsUtils.v1_9_R1.entities.basic.CraftCaveSpiderEntity.CaveSpiderEntity;
import net.tangentmc.nmsUtils.v1_9_R1.entities.basic.CraftChickenEntity.ChickenEntity;
import net.tangentmc.nmsUtils.v1_9_R1.entities.basic.CraftCowEntity.CowEntity;
import net.tangentmc.nmsUtils.v1_9_R1.entities.basic.CraftCreeperEntity.CreeperEntity;
import net.tangentmc.nmsUtils.v1_9_R1.entities.basic.CraftEggEntity.EggEntity;
import net.tangentmc.nmsUtils.v1_9_R1.entities.basic.CraftEnderCrystalEntity.EnderCrystalEntity;
import net.tangentmc.nmsUtils.v1_9_R1.entities.basic.CraftEnderDragonEntity.EnderDragonEntity;
import net.tangentmc.nmsUtils.v1_9_R1.entities.basic.CraftEnderPearlEntity.EnderPearlEntity;
import net.tangentmc.nmsUtils.v1_9_R1.entities.basic.CraftEnderSignalEntity.EnderSignalEntity;
import net.tangentmc.nmsUtils.v1_9_R1.entities.basic.CraftEndermanEntity.EndermanEntity;
import net.tangentmc.nmsUtils.v1_9_R1.entities.basic.CraftEndermiteEntity.EndermiteEntity;
import net.tangentmc.nmsUtils.v1_9_R1.entities.basic.CraftExperienceOrbEntity.ExperienceOrbEntity;
import net.tangentmc.nmsUtils.v1_9_R1.entities.basic.CraftFallingBlockEntity.FallingBlockEntity;
import net.tangentmc.nmsUtils.v1_9_R1.entities.basic.CraftFireworksEntity.FireworksEntity;
import net.tangentmc.nmsUtils.v1_9_R1.entities.basic.CraftGhastEntity.GhastEntity;
import net.tangentmc.nmsUtils.v1_9_R1.entities.basic.CraftGiantZombieEntity.GiantZombieEntity;
import net.tangentmc.nmsUtils.v1_9_R1.entities.basic.CraftGuardianEntity.GuardianEntity;
import net.tangentmc.nmsUtils.v1_9_R1.entities.basic.CraftHorseEntity.HorseEntity;
import net.tangentmc.nmsUtils.v1_9_R1.entities.basic.CraftIronGolemEntity.IronGolemEntity;
import net.tangentmc.nmsUtils.v1_9_R1.entities.basic.CraftItemEntity.ItemEntity;
import net.tangentmc.nmsUtils.v1_9_R1.entities.basic.CraftItemFrameEntity.ItemFrameEntity;
import net.tangentmc.nmsUtils.v1_9_R1.entities.basic.CraftLargeFireballEntity.LargeFireballEntity;
import net.tangentmc.nmsUtils.v1_9_R1.entities.basic.CraftLeashEntity.LeashEntity;
import net.tangentmc.nmsUtils.v1_9_R1.entities.basic.CraftMagmaCubeEntity.MagmaCubeEntity;
import net.tangentmc.nmsUtils.v1_9_R1.entities.basic.CraftMinecartChestEntity.MinecartChestEntity;
import net.tangentmc.nmsUtils.v1_9_R1.entities.basic.CraftMinecartCommandBlockEntity.MinecartCommandBlockEntity;
import net.tangentmc.nmsUtils.v1_9_R1.entities.basic.CraftMinecartFurnaceEntity.MinecartFurnaceEntity;
import net.tangentmc.nmsUtils.v1_9_R1.entities.basic.CraftMinecartHopperEntity.MinecartHopperEntity;
import net.tangentmc.nmsUtils.v1_9_R1.entities.basic.CraftMinecartMobSpawnerEntity.MinecartMobSpawnerEntity;
import net.tangentmc.nmsUtils.v1_9_R1.entities.basic.CraftMinecartRideableEntity.MinecartRideableEntity;
import net.tangentmc.nmsUtils.v1_9_R1.entities.basic.CraftMinecartTNTEntity.MinecartTNTEntity;
import net.tangentmc.nmsUtils.v1_9_R1.entities.basic.CraftMushroomCowEntity.MushroomCowEntity;
import net.tangentmc.nmsUtils.v1_9_R1.entities.basic.CraftOcelotEntity.OcelotEntity;
import net.tangentmc.nmsUtils.v1_9_R1.entities.basic.CraftPaintingEntity.PaintingEntity;
import net.tangentmc.nmsUtils.v1_9_R1.entities.basic.CraftPigEntity.PigEntity;
import net.tangentmc.nmsUtils.v1_9_R1.entities.basic.CraftPigZombieEntity.PigZombieEntity;
import net.tangentmc.nmsUtils.v1_9_R1.entities.basic.CraftRabbitEntity.RabbitEntity;
import net.tangentmc.nmsUtils.v1_9_R1.entities.basic.CraftSheepEntity.SheepEntity;
import net.tangentmc.nmsUtils.v1_9_R1.entities.basic.CraftSilverfishEntity.SilverfishEntity;
import net.tangentmc.nmsUtils.v1_9_R1.entities.basic.CraftSkeletonEntity.SkeletonEntity;
import net.tangentmc.nmsUtils.v1_9_R1.entities.basic.CraftSlimeEntity.SlimeEntity;
import net.tangentmc.nmsUtils.v1_9_R1.entities.basic.CraftSmallFireballEntity.SmallFireballEntity;
import net.tangentmc.nmsUtils.v1_9_R1.entities.basic.CraftSnowballEntity.SnowballEntity;
import net.tangentmc.nmsUtils.v1_9_R1.entities.basic.CraftSnowmanEntity.SnowmanEntity;
import net.tangentmc.nmsUtils.v1_9_R1.entities.basic.CraftSpiderEntity.SpiderEntity;
import net.tangentmc.nmsUtils.v1_9_R1.entities.basic.CraftSplashPotionEntity.PotionEntity;
import net.tangentmc.nmsUtils.v1_9_R1.entities.basic.CraftSquidEntity.SquidEntity;
import net.tangentmc.nmsUtils.v1_9_R1.entities.basic.CraftTNTPrimedEntity.TNTPrimedEntity;
import net.tangentmc.nmsUtils.v1_9_R1.entities.basic.CraftThrownExpBottleEntity.ThrownExpBottleEntity;
import net.tangentmc.nmsUtils.v1_9_R1.entities.basic.CraftTippedArrowEntity.TippedArrowEntity;
import net.tangentmc.nmsUtils.v1_9_R1.entities.basic.CraftVillagerEntity.VillagerEntity;
import net.tangentmc.nmsUtils.v1_9_R1.entities.basic.CraftWitchEntity.WitchEntity;
import net.tangentmc.nmsUtils.v1_9_R1.entities.basic.CraftWitherEntity.WitherEntity;
import net.tangentmc.nmsUtils.v1_9_R1.entities.basic.CraftWitherSkullEntity.WitherSkullEntity;
import net.tangentmc.nmsUtils.v1_9_R1.entities.basic.CraftWolfEntity.WolfEntity;
import net.tangentmc.nmsUtils.v1_9_R1.entities.basic.CraftZombieEntity.ZombieEntity;
@Getter
@AllArgsConstructor
public enum NMSEntityTypes {

	ITEM("Item", 1, EntityType.DROPPED_ITEM, EntityItem.class, ItemEntity.class),
	EXPERIENCEORB("XPOrb", 2, EntityType.EXPERIENCE_ORB, EntityExperienceOrb.class, ExperienceOrbEntity.class),
	EGG("ThrownEgg", 7, EntityType.EGG, EntityEgg.class, EggEntity.class),
	LEASH("LeashKnot", 8, EntityType.LEASH_HITCH, EntityLeash.class, LeashEntity.class),
	PAINTING("Painting", 9, EntityType.PAINTING, EntityPainting.class, PaintingEntity.class),
	ARROW("Arrow", 10, EntityType.ARROW, EntityTippedArrow.class, TippedArrowEntity.class),
	SNOWBALL("Snowball", 11, EntityType.SNOWBALL, EntitySnowball.class, SnowballEntity.class),
	LARGEFIREBALL("Fireball", 12, EntityType.FIREBALL, EntityLargeFireball.class, LargeFireballEntity.class),
	SMALLFIREBALL("SmallFireball", 13, EntityType.SMALL_FIREBALL, EntitySmallFireball.class, SmallFireballEntity.class),
	ENDERPEARL("ThrownEnderpearl", 14, EntityType.ENDER_PEARL, EntityEnderPearl.class, EnderPearlEntity.class),
	ENDERSIGNAL("EyeOfEnderSignal", 15, EntityType.ENDER_SIGNAL, EntityEnderSignal.class, EnderSignalEntity.class),
	POTION("ThrownPotion", 16, EntityType.SPLASH_POTION, EntityPotion.class, PotionEntity.class),
	THROWNEXPBOTTLE("ThrownExpBottle", 17, EntityType.THROWN_EXP_BOTTLE, EntityThrownExpBottle.class, ThrownExpBottleEntity.class),
	ITEMFRAME("ItemFrame", 18, EntityType.ITEM_FRAME, EntityItemFrame.class, ItemFrameEntity.class),
	WITHERSKULL("WitherSkull", 19, EntityType.WITHER_SKULL, EntityWitherSkull.class, WitherSkullEntity.class),
	TNTPRIMED("PrimedTnt", 20, EntityType.PRIMED_TNT, EntityTNTPrimed.class, TNTPrimedEntity.class),
	FALLINGBLOCK("FallingSand", 21, EntityType.FALLING_BLOCK, EntityFallingBlock.class, FallingBlockEntity.class),
	FIREWORKS("FireworksRocketEntity", 22, EntityType.FIREWORK, EntityFireworks.class, FireworksEntity.class),
	ARMORSTAND("ArmorStand", 30, EntityType.ARMOR_STAND, EntityArmorStand.class, ArmorStandEntity.class),
	BOAT("Boat", 41, EntityType.BOAT, EntityBoat.class, BoatEntity.class),
	MINECARTRIDEABLE(EntityMinecartAbstract.EnumMinecartType.RIDEABLE.b(), 42, EntityType.MINECART, EntityMinecartRideable.class, MinecartRideableEntity.class),
	MINECARTCHEST(EntityMinecartAbstract.EnumMinecartType.CHEST.b(), 43, EntityType.MINECART_CHEST, EntityMinecartChest.class, MinecartChestEntity.class),
	MINECARTFURNACE(EntityMinecartAbstract.EnumMinecartType.FURNACE.b(), 44, EntityType.MINECART_FURNACE, EntityMinecartFurnace.class, MinecartFurnaceEntity.class),
	MINECARTTNT(EntityMinecartAbstract.EnumMinecartType.TNT.b(), 45, EntityType.MINECART_TNT, EntityMinecartTNT.class, MinecartTNTEntity.class),
	MINECARTHOPPER(EntityMinecartAbstract.EnumMinecartType.HOPPER.b(), 46, EntityType.MINECART_HOPPER, EntityMinecartHopper.class, MinecartHopperEntity.class),
	MINECARTMOBSPAWNER(EntityMinecartAbstract.EnumMinecartType.SPAWNER.b(), 47, EntityType.MINECART_MOB_SPAWNER, EntityMinecartMobSpawner.class, MinecartMobSpawnerEntity.class),
	MINECARTCOMMANDBLOCK(EntityMinecartAbstract.EnumMinecartType.COMMAND_BLOCK.b(), 40, EntityType.MINECART_COMMAND, EntityMinecartCommandBlock.class, MinecartCommandBlockEntity.class),
	CREEPER("Creeper", 50, EntityType.CREEPER, EntityCreeper.class, CreeperEntity.class),
	SKELETON("Skeleton", 51, EntityType.SKELETON, EntitySkeleton.class, SkeletonEntity.class),
	SPIDER("Spider", 52, EntityType.SPIDER, EntitySpider.class, SpiderEntity.class),
	GIANTZOMBIE("Giant", 53, EntityType.GIANT, EntityGiantZombie.class, GiantZombieEntity.class),
	ZOMBIE("Zombie", 54, EntityType.ZOMBIE, EntityZombie.class, ZombieEntity.class),
	SLIME("Slime", 55, EntityType.SLIME, EntitySlime.class, SlimeEntity.class),
	GHAST("Ghast", 56, EntityType.GHAST, EntityGhast.class, GhastEntity.class),
	PIGZOMBIE("PigZombie", 57, EntityType.PIG_ZOMBIE, EntityPigZombie.class, PigZombieEntity.class),
	ENDERMAN("Enderman", 58, EntityType.ENDERMAN, EntityEnderman.class, EndermanEntity.class),
	CAVESPIDER("CaveSpider", 59, EntityType.CAVE_SPIDER, EntityCaveSpider.class, CaveSpiderEntity.class),
	SILVERFISH("Silverfish", 60, EntityType.SILVERFISH, EntitySilverfish.class, SilverfishEntity.class),
	BLAZE("Blaze", 61, EntityType.BLAZE, EntityBlaze.class, BlazeEntity.class),
	MAGMACUBE("LavaSlime", 62, EntityType.MAGMA_CUBE, EntityMagmaCube.class, MagmaCubeEntity.class),
	ENDERDRAGON("EnderDragon", 63, EntityType.ENDER_DRAGON, EntityEnderDragon.class, EnderDragonEntity.class),
	WITHER("WitherBoss", 64, EntityType.WITHER, EntityWither.class, WitherEntity.class),
	BAT("Bat", 65, EntityType.BAT, EntityBat.class, BatEntity.class),
	WITCH("Witch", 66, EntityType.WITCH, EntityWitch.class, WitchEntity.class),
	ENDERMITE("Endermite", 67, EntityType.ENDERMITE, EntityEndermite.class, EndermiteEntity.class),
	GUARDIAN("Guardian", 68, EntityType.GUARDIAN, EntityGuardian.class, GuardianEntity.class),
	PIG("Pig", 90, EntityType.PIG, EntityPig.class, PigEntity.class),
	SHEEP("Sheep", 91, EntityType.SHEEP, EntitySheep.class, SheepEntity.class),
	COW("Cow", 92, EntityType.COW, EntityCow.class, CowEntity.class),
	CHICKEN("Chicken", 93, EntityType.CHICKEN, EntityChicken.class, ChickenEntity.class),
	SQUID("Squid", 94, EntityType.SQUID, EntitySquid.class, SquidEntity.class),
	WOLF("Wolf", 95, EntityType.WOLF, EntityWolf.class, WolfEntity.class),
	MUSHROOMCOW("MushroomCow", 96, EntityType.MUSHROOM_COW, EntityMushroomCow.class, MushroomCowEntity.class),
	SNOWMAN("SnowMan", 97, EntityType.SNOWMAN, EntitySnowman.class, SnowmanEntity.class),
	OCELOT("Ozelot", 98, EntityType.OCELOT, EntityOcelot.class, OcelotEntity.class),
	IRONGOLEM("VillagerGolem", 99, EntityType.IRON_GOLEM, EntityIronGolem.class, IronGolemEntity.class),
	HORSE("EntityHorse", 100, EntityType.HORSE, EntityHorse.class, HorseEntity.class),
	RABBIT("Rabbit", 101, EntityType.RABBIT, EntityRabbit.class, RabbitEntity.class),
	VILLAGER("Villager", 120, EntityType.VILLAGER, EntityVillager.class, VillagerEntity.class),
	ENDERCRYSTAL("EnderCrystal", 200, EntityType.ENDER_CRYSTAL, EntityEnderCrystal.class, EnderCrystalEntity.class),
	//Lasers are now endercrystals
	LASERORIGIN("LaserSource", 200, EntityType.ENDER_CRYSTAL, EntityEnderCrystal.class, LaserSourceEntity.class, false),
	//LASERORIGIN("LaserSource", 68, EntityType.GUARDIAN, EntityGuardian.class, LaserSourceEntity.class, false),
	
	TEXTHOLOGRAM("TextHologram", 30, EntityType.ARMOR_STAND, EntityArmorStand.class, TextHologramEntity.class, false),
	BLOCKHOLOGRAM("BlockHologramT", 30, EntityType.ARMOR_STAND, EntityArmorStand.class, BlockHologramEntity.class, false),
	ITEMHOLOGRAM("ItemHologramT", 30, EntityType.ARMOR_STAND, EntityArmorStand.class, ItemHologramEntity.class, false),
	
	HOLOGRAMITEMENTITY("ItemHologramEntity", 1, EntityType.DROPPED_ITEM, EntityItem.class, HologramItem.class, false),
	HOLOGRAMBLOCKENTITY("BlockHologramEntity", 21, EntityType.FALLING_BLOCK, EntityFallingBlock.class, HologramBlock.class, false);
	private String name;
	private int id;
	private EntityType entityType;
	private Class<? extends net.minecraft.server.v1_9_R1.Entity> nmsClass;
	private Class<? extends net.minecraft.server.v1_9_R1.Entity> customClass;
	boolean override;
	NMSEntityTypes(String name, int id, EntityType entityType, Class<? extends net.minecraft.server.v1_9_R1.Entity> nmsClass,
				   Class<? extends net.minecraft.server.v1_9_R1.Entity> customClass) {
		this.name = name;
		this.id = id;
		this.entityType = entityType;
		this.nmsClass = nmsClass;
		this.customClass = customClass;
		this.override = true;
	}
	public static Class<? extends net.minecraft.server.v1_9_R1.Entity> getClassById(String id) {
		Optional<NMSEntityTypes> found = Arrays.stream(values()).filter(t->t.getName().equals(id)).findFirst();
		if (!found.isPresent()) return null;
		return found.get().customClass;
	}
	/**
	 * Register our entities.
	 */
	@SuppressWarnings("unchecked")
	public static void registerEntities() {
		for (NMSEntityTypes entity : values())
			a(entity.getCustomClass(), entity.getName(), entity.getId(),entity.override);

		Set<BiomeBase> biomes = BiomeBase.i;
		for (BiomeBase biomeBase : biomes) {
			if (biomeBase == null)
				break;
			for (EnumCreatureType t : EnumCreatureType.values()) {

				List<BiomeMeta> mobList = biomeBase.getMobs(t);
				// Write in our custom class.
				for (BiomeMeta meta : mobList)
					for (NMSEntityTypes entity : values())
						if (entity.getNmsClass().equals(meta.b) && entity.override && entity.getCustomClass().isAssignableFrom(EntityInsentient.class))
							meta.b = (Class<? extends EntityInsentient>) entity.getCustomClass();
			}
		}
	}

	/**
	 * Unregister our entities to prevent memory leaks. Call on disable.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void unregisterEntities() {
		for (NMSEntityTypes entity : values()) {
			// Remove our class references.
			try {
				((Map) ReflectionManager.getPrivateStatic(EntityTypes.class, "d")).remove(entity.getCustomClass());
			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				((Map) ReflectionManager.getPrivateStatic(EntityTypes.class, "f")).remove(entity.getCustomClass());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		for (NMSEntityTypes entity : values())
			try {
				// Unregister each entity by writing the NMS back in place of the custom class.
				a(entity.getNmsClass(), entity.getName(), entity.getId(), entity.override);
			} catch (Exception e) {
				e.printStackTrace();
			}
		Set<BiomeBase> biomes = BiomeBase.i;
		for (BiomeBase biomeBase : biomes) {
			if (biomeBase == null)
				break;
			for (EnumCreatureType t : EnumCreatureType.values()) {
				List<BiomeMeta> mobList = biomeBase.getMobs(t);
				// Write in our custom class.
				for (BiomeMeta meta : mobList)
					for (NMSEntityTypes entity : values())
						if (entity.getCustomClass().equals(meta.b) && entity.override)
							meta.b = (Class<? extends EntityInsentient>) entity.getNmsClass();
			}
		}
	}

	/*
	 * Since 1.7.2 added a check in their entity registration, simply bypass it and write to the maps ourself.
	 */
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static void a(Class<? extends Entity> paramClass, String paramString, int paramInt, boolean override) {
		try {
			if (override) {
				((Map) ReflectionManager.getPrivateStatic(EntityTypes.class, "c")).put(paramString, paramClass);
				((Map) ReflectionManager.getPrivateStatic(EntityTypes.class, "e")).put(Integer.valueOf(paramInt), paramClass);
			}
			((Map) ReflectionManager.getPrivateStatic(EntityTypes.class, "d")).put(paramClass, paramString);
			((Map) ReflectionManager.getPrivateStatic(EntityTypes.class, "f")).put(paramClass, Integer.valueOf(paramInt));
			((Map) ReflectionManager.getPrivateStatic(EntityTypes.class, "g")).put(paramString, Integer.valueOf(paramInt));
		} catch (Exception exc) {
			// Unable to register the new class.
		}
	}
}