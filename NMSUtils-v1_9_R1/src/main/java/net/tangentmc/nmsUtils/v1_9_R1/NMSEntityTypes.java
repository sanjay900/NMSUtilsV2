package net.tangentmc.nmsUtils.v1_9_R1;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.server.v1_9_R1.*;
import net.minecraft.server.v1_9_R1.BiomeBase.BiomeMeta;
import net.tangentmc.nmsUtils.utils.ReflectionManager;
import net.tangentmc.nmsUtils.v1_9_R1.entities.LaserEntitiesGuardian;
import org.bukkit.entity.EntityType;

import java.util.*;
@Getter
@AllArgsConstructor
public enum NMSEntityTypes {
	//Lasers are now endercrystals
	//LASERORIGIN("LaserSource", 200, EntityType.ENDER_CRYSTAL, EntityEnderCrystal.class, LaserSourceEntity.class, false),
	LASERORIGIN("LaserSource", 68, EntityType.GUARDIAN, EntityGuardian.class, LaserEntitiesGuardian.CraftLaserSourceEntity.LaserSourceEntity.class, false);
	
	//TEXTHOLOGRAM("TextHologram", 30, EntityType.ARMOR_STAND, EntityArmorStand.class, TextHologramEntity.class, false),
	//BLOCKHOLOGRAM("BlockHologramT", 30, EntityType.ARMOR_STAND, EntityArmorStand.class, BlockHologramEntity.class, false),
	//ITEMHOLOGRAM("ItemHologramT", 30, EntityType.ARMOR_STAND, EntityArmorStand.class, ItemHologramEntity.class, false),
	
	//HOLOGRAMITEMENTITY("ItemHologramEntity", 1, EntityType.DROPPED_ITEM, EntityItem.class, HologramItem.class, false),
	//HOLOGRAMBLOCKENTITY("BlockHologramEntity", 21, EntityType.FALLING_BLOCK, EntityFallingBlock.class, HologramBlock.class, false);
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