package net.tangentmc.nmsUtils.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.tangentmc.nmsUtils.utils.MetadataSaver;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
@Getter
@AllArgsConstructor
public class MetadataCreateEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    Entity en;
    MetadataSaver metadata;
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
