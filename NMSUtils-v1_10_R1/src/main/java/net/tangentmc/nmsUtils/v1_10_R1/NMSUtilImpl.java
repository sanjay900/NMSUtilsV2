package net.tangentmc.nmsUtils.v1_10_R1;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import lombok.Getter;
import lombok.SneakyThrows;
import net.minecraft.server.v1_10_R1.*;
import net.tangentmc.nmsUtils.NMSUtil;
import net.tangentmc.nmsUtils.NMSUtils;
import net.tangentmc.nmsUtils.entities.*;
import net.tangentmc.nmsUtils.events.EntityMoveEvent;
import net.tangentmc.nmsUtils.jinglenote.JingleNoteManager;
import net.tangentmc.nmsUtils.jinglenote.MidiJingleSequencer;
import net.tangentmc.nmsUtils.utils.Validator;
import net.tangentmc.nmsUtils.v1_10_R1.entities.CraftHologramEntity;
import net.tangentmc.nmsUtils.v1_10_R1.entities.NPC;
import net.tangentmc.nmsUtils.v1_10_R1.entities.basic.BasicNMSArmorStand;
import net.tangentmc.nmsUtils.v1_10_R1.entities.basic.BasicNMSEntity;
import net.tangentmc.nmsUtils.v1_10_R1.entities.basic.NMSHologramWrapper;
import net.tangentmc.nmsUtils.v1_10_R1.entities.effects.Collideable;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_10_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftPlayer;
import org.bukkit.entity.*;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.world.ChunkUnloadEvent;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

@Getter
public class NMSUtilImpl implements NMSUtil, Listener, Runnable {

    private HashSet<UUID> riding = new HashSet<>();
    JingleNoteManager manager = new JingleNoteManager();
    NPCManager npcmanager;

    @SneakyThrows
    public NMSUtilImpl() {
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketListener(this));
        npcmanager = new NPCManager();
        Bukkit.getPluginManager().registerEvents(this, NMSUtils.getInstance());
        Bukkit.getWorlds().forEach(this::trackWorldEntities);
        Bukkit.getScheduler().runTaskTimer(NMSUtils.getInstance(), this, 10L, 1L);
    }

    WeakHashMap<UUID, WorldManager> managers = new WeakHashMap<>();

    @Override
    public void trackWorldEntities(World w) {
        managers.put(w.getUID(), new WorldManager(w));
        w.getEntities().stream().filter(e -> !NMSEntity.wrap(e).willSave()).forEach(Entity::remove);
    }

    @Override
    public void untrackWorldEntities(World w) {
        managers.get(w.getUID()).remove();
    }

    public net.minecraft.server.v1_10_R1.World getWorld(World w) {
        return ((CraftWorld) w).getHandle();
    }


    public List<Integer> getEntityRemoveQueue(Player entity) {
        return ((CraftPlayer) entity).getHandle().removeQueue;
    }

    public void flushEntityRemoveQueue(Player pl) {
        final List<Integer> ids = getEntityRemoveQueue(pl);
        if (ids.isEmpty()) {
            return;
        }
        while (ids.size() >= 128) {
            final int[] rawIds = new int[127];
            for (int i = 0; i < rawIds.length; i++) {
                rawIds[i] = ids.remove(0);
            }
            PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_DESTROY);
            packet.getIntegerArrays().write(0, rawIds);
            try {
                ProtocolLibrary.getProtocolManager().sendServerPacket(pl, packet);
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        final int[] rawIds = new int[ids.size()];
        for (int i = 0; i < rawIds.length; i++) {
            rawIds[i] = ids.remove(0);
        }
        PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_DESTROY);
        packet.getIntegerArrays().write(0, rawIds);
        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(pl, packet);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        ids.clear();
    }

    @Override
    public boolean teleportFast(Entity entity, Location location, org.bukkit.util.Vector velocity) {
        if (entity.isInsideVehicle()) {
            entity = entity.getVehicle();
        }
        net.minecraft.server.v1_10_R1.Entity en = ((CraftEntity) entity).getHandle();
        //Flush the removal queue so that it is done first then we teleport and set velocity
        entity.getWorld().getPlayers().forEach(this::flushEntityRemoveQueue);
        EntityTracker tracker = ((WorldServer) getWorld(entity.getWorld())).getTracker();
        if (!(entity instanceof Minecart)) {
            tracker.untrackEntity(en);
        }
        en.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        en.motX = velocity.getX();
        en.motY = velocity.getY();
        en.motZ = velocity.getZ();

        if (!(entity instanceof Minecart)) {
            tracker.track(en);
        } else {
            EntityTrackerEntry entry = tracker.trackedEntities.get(en.getId());
            //Forcibly update this entity this tick
            //Why 3? The tracker runs every three ticks, but you cant set a to 0 or it wont run at all
            //At least, it runs every three ticks for MineCarts anyway.
            entry.a = 3;
        }
        tracker.updatePlayers();
        return true;
    }
    public static NBTTagCompound getTag(net.minecraft.server.v1_10_R1.Entity en) {
        NBTTagCompound tag = new NBTTagCompound();
        en.c(tag);
        return tag;
    }

    @Override
    public void stealPlayerControls(Location loc, Player who) {
        if (loc != null) {
            ArmorStand as = (ArmorStand) loc.getWorld().spawnEntity(loc.add(0, -0.9875, 0), EntityType.ARMOR_STAND);
            as.setVisible(false);
            as.setSmall(true);
            as.setPassenger(who);
            as.setGravity(false);
        }
        this.riding.add(who.getUniqueId());
    }

    @Override
    public NMSHologram spawnHologram(Location loc, ArrayList<HologramFactory.HologramObject> lines) {
        CraftHologramEntity.HologramEntity holo = new CraftHologramEntity.HologramEntity(loc, lines);
        return NMSHologram.wrap(holo.getBukkitEntity());
    }

    @Override
    public void playMidi(Player to, boolean repeat, File midi)
            throws MidiUnavailableException, InvalidMidiDataException, IOException {
        MidiJingleSequencer seq = new MidiJingleSequencer(midi, repeat);
        manager.play(to.getName(), seq);
    }

    @Override
    public void playMidiNear(Location near, double area, boolean repeat, File midi)
            throws MidiUnavailableException, InvalidMidiDataException, IOException {
        MidiJingleSequencer seq = new MidiJingleSequencer(midi, repeat);
        manager.playNear(near, area, seq);
    }

    static Field b;
    static Field c;

    static {
        try {
            b = PathfinderGoalSelector.class.getDeclaredField("b");
            b.setAccessible(true);
            c = PathfinderGoalSelector.class.getDeclaredField("c");
            c.setAccessible(true);
        } catch (SecurityException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public static void clearSelectors(EntityInsentient entity) {
        try {
            b.set(entity.targetSelector, Sets.newLinkedHashSet());
            c.set(entity.targetSelector, Sets.newLinkedHashSet());
            b.set(entity.goalSelector, Sets.newLinkedHashSet());
            c.set(entity.goalSelector, Sets.newLinkedHashSet());

        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public net.tangentmc.nmsUtils.entities.NPC spawnNPC(String name, Location location, String value, String signature) {
        NPC npc = NPC.createNPC(NMSUtils.getInstance(), name, location, value, signature);
        this.npcmanager.addNPC(npc.getBukkitEntity());
        Bukkit.getOnlinePlayers().forEach(npc::spawn);
        return npc.getBukkitEntity();
    }

    @Override
    public net.tangentmc.nmsUtils.entities.NPC spawnNPC(String name, Location location) {
        return spawnNPC(name, location, "", "");
    }

    @Override
    public NMSEntity getNMSEntity(Entity en2) {
        if (en2 instanceof CraftHologramEntity) {
            return new NMSHologramWrapper(en2);
        } else if (en2.getType() == EntityType.ARMOR_STAND) {
            return new BasicNMSArmorStand(en2);
        } else {
            return new BasicNMSEntity(en2);
        }
    }

    @Override
    public void run() {
        for (World w : Bukkit.getWorlds()) {
            for (Entity en : w.getEntities()) {
                net.minecraft.server.v1_10_R1.Entity added = ((CraftEntity) en).getHandle();
                if (added.lastX != added.locX || added.lastY != added.locY || added.lastZ != added.locZ || added.lastPitch != added.pitch || added.lastYaw != added.yaw) {
                    Bukkit.getPluginManager().callEvent(new EntityMoveEvent(en, added.lastX, added.lastY, added.lastZ, added.locX, added.locY, added.locZ, added.pitch, added.lastPitch, added.yaw, added.lastYaw));
                }
                if (en.hasMetadata(NMSEntity.COLLIDE_TAG)) {
                    Collideable.testCollision(added);
                }
            }
        }
    }
    public static boolean addEntityToWorld(WorldServer nmsWorld, net.minecraft.server.v1_10_R1.Entity nmsEntity) {
        net.minecraft.server.v1_10_R1.Chunk nmsChunk = nmsWorld.getChunkAtWorldCoords(nmsEntity.getChunkCoordinates());

        if (nmsChunk != null) {
            Chunk chunk = nmsChunk.bukkitChunk;

            if (!chunk.isLoaded()) {
                chunk.load();
                NMSUtils.getInstance().getLogger().info("Loaded chunk (x:" + chunk.getX() + " z:" + chunk.getZ() + ") to spawn a Hologram");
            }
        }

        return nmsWorld.addEntity(nmsEntity, CreatureSpawnEvent.SpawnReason.CUSTOM);
    }
}
