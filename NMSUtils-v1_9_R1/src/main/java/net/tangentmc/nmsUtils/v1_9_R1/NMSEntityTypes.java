package net.tangentmc.nmsUtils.v1_9_R1;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.server.v1_9_R1.*;
import net.minecraft.server.v1_9_R1.BiomeBase.BiomeMeta;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.tangentmc.nmsUtils.utils.ReflectionManager;
import net.tangentmc.nmsUtils.v1_9_R1.entities.CraftHologramEntity;
import net.tangentmc.nmsUtils.v1_9_R1.entities.LaserEntitiesGuardian;
import net.tangentmc.nmsUtils.v1_9_R1.entities.effects.Collideable;
import org.bukkit.entity.EntityType;

import java.lang.reflect.Constructor;
import java.util.*;
@Getter
@AllArgsConstructor
public enum NMSEntityTypes {
	//Lasers are now endercrystals
	//LASERORIGIN("LaserSource", 200, EntityType.ENDER_CRYSTAL, EntityEnderCrystal.class, LaserSourceEntity.class, false),
	LASERORIGIN("LaserSource", 68, EntityType.GUARDIAN, EntityGuardian.class, LaserEntitiesGuardian.CraftLaserSourceEntity.LaserSourceEntity.class),
    LASERDEST("LaserDest", 94, EntityType.SQUID, EntitySquid.class, LaserEntitiesGuardian.CraftLaserDestinationEntity.LaserDestinationEntity.class),
	TEXTHOLOGRAM("TextHologram", 30,EntityType.ARMOR_STAND, EntityArmorStand.class, CraftHologramEntity.CraftHologramPart.TextHologramEntity.class),
	BLOCKHOLOGRAM("BlockHologramT", 30,EntityType.ARMOR_STAND, EntityArmorStand.class, CraftHologramEntity.CraftHologramPart.BlockHologramEntity.class),
	ITEMHOLOGRAM("ItemHologramT", 30,EntityType.ARMOR_STAND, EntityArmorStand.class, CraftHologramEntity.CraftHologramPart.ItemHologramEntity.class),

	HOLOGRAMITEMENTITY("ItemHologramEntity", 1,EntityType.DROPPED_ITEM, EntityItem.class, CraftHologramEntity.CraftHologramPart.ItemHologramEntity.HologramItem.class),
	HOLOGRAMBLOCKENTITY("BlockHologramEntity", 21,EntityType.FALLING_BLOCK, EntityFallingBlock.class, CraftHologramEntity.CraftHologramPart.BlockHologramEntity.HologramBlock.class);
	private String name;
	private int id;
	private EntityType entityType;
	private Class<? extends net.minecraft.server.v1_9_R1.Entity> nmsClass;
	private Class<? extends net.minecraft.server.v1_9_R1.Entity> customClass;
	/**
	 * Register our entities.
	 */
	@SuppressWarnings("unchecked")
	public static void registerEntities() {
		for (NMSEntityTypes entity : values())
			a(entity.getCustomClass(), entity.getName(), entity.getId());
	}

	/*
	 * Since 1.7.2 added a check in their entity registration, simply bypass it and write to the maps ourself.
	 */

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static void a(Class<? extends Entity> paramClass, String paramString, int paramInt) {
		try {
			((Map) ReflectionManager.getPrivateStatic(EntityTypes.class, "d")).put(paramClass, paramString);
			((Map) ReflectionManager.getPrivateStatic(EntityTypes.class, "f")).put(paramClass, Integer.valueOf(paramInt));
			((Map) ReflectionManager.getPrivateStatic(EntityTypes.class, "g")).put(paramString, Integer.valueOf(paramInt));
		} catch (Exception exc) {
			// Unable to register the new class.
		}
	}
}