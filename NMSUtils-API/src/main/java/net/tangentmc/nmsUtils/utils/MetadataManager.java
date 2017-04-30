package net.tangentmc.nmsUtils.utils;

import net.tangentmc.nmsUtils.NMSUtil;
import net.tangentmc.nmsUtils.NMSUtils;
import net.tangentmc.nmsUtils.entities.NMSEntity;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

public class MetadataManager implements Listener {
    public MetadataManager() {
        Bukkit.getPluginManager().registerEvents(this, NMSUtils.getInstance());
    }
    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        Arrays.stream(event.getChunk().getEntities()).forEach(this::entityAdd);
    }
    public void entityAdd(Entity ent) {
        NMSEntity en = NMSEntity.wrap(ent);
        if (!en.willSave()) {
            ent.remove();
            return;
        }
        if (!en.hasEntityTag("hasMetadataSaver")) return;
        for (String tag: en.getTags()) {
            if (tag.startsWith("MetadataSaverClass:")) {
                for (String cName: tag.substring(tag.indexOf(":")+1).split(":")) {
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
