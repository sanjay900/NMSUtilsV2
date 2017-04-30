package net.tangentmc.nmsUtils.utils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import net.tangentmc.nmsUtils.NMSUtil;
import net.tangentmc.nmsUtils.NMSUtils;
import net.tangentmc.nmsUtils.entities.NMSEntity;
import net.tangentmc.nmsUtils.events.MetadataCreateEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.metadata.FixedMetadataValue;

/**
 * An class that's capable of saving itself inside the metadata of an entity
 * Any class implementing this entity needs to have the annotation {@link Metadata}
 * And requires a constructor with a single Entity argument.
 * @author sanjay
 *
 */
public abstract class MetadataSaver {
	@Getter
	private Entity wrappedEntity;
	@Getter
	private NMSEntity wrappedNMSEntity;
	protected void initMetadata(Entity en) {
		this.wrappedEntity = en;
		wrappedNMSEntity = NMSUtils.getInstance().getUtil().getNMSEntity(en);
		wrappedNMSEntity.addEntityTag("hasMetadataSaver");
		wrappedNMSEntity.addEntityTag(getMetadataName());
		String existingSavers = "";
		for (String tag:wrappedNMSEntity.getTags()) {
			if (tag.startsWith("MetadataSaverClass:")) {
				existingSavers = tag.substring(tag.indexOf(":"));
			}
		}
		if (existingSavers.isEmpty()) {
			wrappedNMSEntity.addEntityTag("MetadataSaverClass:"+this.getClass().getName());
		} else {
			if (!existingSavers.contains(this.getClass().getName()))
				wrappedNMSEntity.addEntityTag("MetadataSaverClass:"+existingSavers+":"+this.getClass().getName());
		}
		en.setMetadata("hasMetadataSaver", new FixedMetadataValue(NMSUtils.getInstance(),true));
		en.setMetadata(getMetadataName(), new FixedMetadataValue(NMSUtils.getInstance(),this));
		Bukkit.getPluginManager().callEvent(new MetadataCreateEvent(en,this));
	}
	public static boolean hasMetadata(Entity en) {
		return en.hasMetadata("hasMetadataSaver");
	}

	public String getMetadataName() {
		return getMetadataName(this.getClass());
	}
	private static String getMetadataName(Class<? extends MetadataSaver> clazz) {
		if (clazz.getAnnotation(Metadata.class)==null) return null;
		return clazz.getAnnotation(Metadata.class).metadataName();
	}
	public static boolean isInstance(Entity en,Class<? extends MetadataSaver> clazz) {
		return en.hasMetadata(getMetadataName(clazz));
	}
	public static List<MetadataSaver> getMetadataSavers(Entity en) {
		List<MetadataSaver> savers = new ArrayList<>();
		for (String tag : NMSUtils.getInstance().getUtil().getNMSEntity(en).getTags()) {
			if (tag.startsWith("MetadataSaverClass:")) {
				for (String saver: tag.substring(tag.indexOf(":")).split(":")) {
					try {
						Class<? extends MetadataSaver> clazz = (Class<? extends MetadataSaver>) Class.forName(saver);
						savers.add(get(en,clazz));
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return savers;
	}
	@SuppressWarnings("unchecked")
	public static <T extends MetadataSaver> T get(Entity en,Class<? extends T> clazz) {
		if (!isInstance(en,clazz)) return null;
		return (T) en.getMetadata(getMetadataName(clazz)).get(0).value();
	}
	@Retention(value = RetentionPolicy.RUNTIME)
	public @interface Metadata {
		String metadataName();
	}
}
