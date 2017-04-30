package net.tangentmc.nmsUtils.utils;

import net.tangentmc.nmsUtils.NMSUtils;
import net.tangentmc.nmsUtils.entities.NMSEntity;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;

import java.lang.reflect.InvocationTargetException;

public class MetadataManager {
    @EventHandler
    public void entityAdd(Entity ent) {
        NMSEntity en = NMSEntity.wrap(ent);
        System.out.println(en.willSave());
        if (!en.willSave()) {
            ent.remove();
            return;
        }
        if (!en.hasEntityTag("hasMetadataSaver")) return;
        for (String tag: en.getTags()) {
            if (tag.startsWith("MetadataSaverClass:")) {
                for (String cName: tag.substring(tag.indexOf(":")).split(":")) {
                    try {
                        Class<?> c = Class.forName(cName);
                        c.getConstructor(Entity.class).newInstance(ent);
                    } catch (ClassNotFoundException e) {
                        System.out.println("Unable to initialize MetadataSaver for: "+cName);
                    } catch (IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
