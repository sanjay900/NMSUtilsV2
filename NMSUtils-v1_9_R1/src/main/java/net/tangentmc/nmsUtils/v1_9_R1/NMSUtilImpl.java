package net.tangentmc.nmsUtils.v1_9_R1;
import static org.mockito.Mockito.*;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import lombok.Getter;
import lombok.SneakyThrows;
import net.minecraft.server.v1_9_R1.*;
import net.sf.cglib.proxy.Enhancer;
import net.tangentmc.nmsUtils.NMSUtil;
import net.tangentmc.nmsUtils.NMSUtils;
import net.tangentmc.nmsUtils.entities.*;
import net.tangentmc.nmsUtils.events.EntityMoveEvent;
import net.tangentmc.nmsUtils.jinglenote.JingleNoteManager;
import net.tangentmc.nmsUtils.jinglenote.MidiJingleSequencer;
import net.tangentmc.nmsUtils.utils.Validator;
import net.tangentmc.nmsUtils.v1_9_R1.entities.CraftHologramEntity;
import net.tangentmc.nmsUtils.v1_9_R1.entities.LaserEntitiesGuardian;
import net.tangentmc.nmsUtils.v1_9_R1.entities.NPC;
import net.tangentmc.nmsUtils.v1_9_R1.entities.effects.Collideable;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_9_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftPlayer;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.util.MockUtil;

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
    private static Table<Integer,Integer,List<NMSEntity>> customEntities = HashBasedTable.create();
    NPCManager npcmanager;
    @SneakyThrows
    public NMSUtilImpl() {
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketListener(this));
        validateEntityMethod = net.minecraft.server.v1_9_R1.World.class.getDeclaredMethod("b", net.minecraft.server.v1_9_R1.Entity.class);
        validateEntityMethod.setAccessible(true);
        NMSEntityTypes.registerEntities();
        npcmanager = new NPCManager();
        Bukkit.getPluginManager().registerEvents(this, NMSUtils.getInstance());
        Bukkit.getWorlds().forEach(this::trackWorldEntities);
        Bukkit.getScheduler().runTaskTimer(NMSUtils.getInstance(),this,10L,1L);
    }
    WeakHashMap<UUID,WorldManager> managers = new WeakHashMap<>();
    @Override
    public void trackWorldEntities(World w) {
        managers.put(w.getUID(), new WorldManager(w));
    }

    @Override
    public void untrackWorldEntities(World w) {
        managers.get(w.getUID()).remove();
    }
    public net.minecraft.server.v1_9_R1.World getWorld(World w) {
        return ((CraftWorld)w).getHandle();
    }


    public List<Integer> getEntityRemoveQueue(Player entity) {
        return ((CraftPlayer)entity).getHandle().removeQueue;
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
    public boolean teleportFast(Entity entity, Location location) {
        net.minecraft.server.v1_9_R1.Entity en = ((CraftEntity)entity).getHandle();
        if (en.getVehicle() != null) {
            en = en.getVehicle();
        }
        Entity passenger = null;
        if (entity.getPassenger() != null) {
            passenger = entity.getPassenger();
            entity.eject();
        }

        ((WorldServer) getWorld(entity.getWorld())).getTracker().untrackEntity(en);
        for (Player bukkitPlayer : entity.getWorld().getPlayers()) {
            flushEntityRemoveQueue(bukkitPlayer);
        }
        en.world = ((CraftWorld) location.getWorld()).getHandle();
        en.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        ((WorldServer) getWorld(entity.getWorld())).getTracker().track(en);
        if (passenger != null) {
            entity.setPassenger(passenger);
        }
        return true;
    }
    public static NBTTagCompound getTag(net.minecraft.server.v1_9_R1.Entity en) {
        NBTTagCompound tag = new NBTTagCompound();
        en.c(tag);
        return tag;
    }
    @Override
    public void stealPlayerControls(Location loc, Player who) {
        ArmorStand as = (ArmorStand) loc.getWorld().spawnEntity(loc.add(0, -0.9875, 0), EntityType.ARMOR_STAND);
        as.setVisible(false);
        as.setSmall(true);
        as.setPassenger(who);
        this.riding.add(who.getUniqueId());
    }

    @Override
    public NMSLaser spawnLaser(Location init) {
        LaserEntitiesGuardian.CraftLaserEntity.LaserEntity laser = new LaserEntitiesGuardian.CraftLaserEntity.LaserEntity(((CraftWorld)init.getWorld()).getHandle(), init);
        return laser.getBukkitEntity();
    }

	@Override
	public NMSHologram spawnHologram(Location loc, ArrayList<HologramFactory.HologramObject> lines) {
		CraftHologramEntity.HologramEntity holo = new CraftHologramEntity.HologramEntity(loc,lines);
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
    private static Method validateEntityMethod;
    public static boolean addEntityToWorld(net.minecraft.server.v1_9_R1.World world, net.minecraft.server.v1_9_R1.Entity nmsEntity) {
        Validator.isTrue(Bukkit.isPrimaryThread(), "Async entity add");

        if (validateEntityMethod == null) {
            return world.addEntity(nmsEntity, SpawnReason.CUSTOM);
        }
        final int chunkX = MathHelper.floor(nmsEntity.locX / 16.0);
        final int chunkZ = MathHelper.floor(nmsEntity.locZ / 16.0);
        //This function can be called to add entities that arent in loaded chunks. When that happenes,
        //queue them for later.
        if (!((WorldServer)world).getChunkProviderServer().isChunkLoaded(chunkX, chunkZ)) {
            if (nmsEntity.getBukkitEntity() instanceof NMSEntity) {
                if (!customEntities.contains(chunkX, chunkZ)) customEntities.put(chunkX, chunkZ, new ArrayList<>());
                customEntities.get(chunkX, chunkZ).add((NMSEntity)nmsEntity.getBukkitEntity());
            }
            return false;
        }
        if (((WorldServer)world).getEntity(nmsEntity.getUniqueID()) != null) return false;
        if (((WorldServer)world).tracker.trackedEntities.b(nmsEntity.getId())) {
            return false;
        }
        if (Arrays.asList(world.getChunkAt(chunkX, chunkZ).getEntitySlices()).contains(nmsEntity)) {
            return false;
        }
        world.getChunkAt(chunkX, chunkZ).a(nmsEntity);
        world.entityList.add(nmsEntity);
        try {
            validateEntityMethod.invoke(world, nmsEntity);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
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
    @EventHandler
    public void unloadChunk(ChunkUnloadEvent evt) {
        Chunk c = evt.getChunk();
        customEntities.put(c.getX(),c.getZ(),Arrays.stream(c.getEntities()).filter(en ->en instanceof NMSEntity).filter(en -> !en.isDead()).map(en -> (NMSEntity)en).filter(nms -> !nms.willSave()).collect(Collectors.toList()));
    }
    @Override
    public void loadChunk(Chunk c) {
        if (!customEntities.contains(c.getX(), c.getZ())) return;
        customEntities.get(c.getX(),c.getZ()).forEach(NMSEntity::spawn);
        customEntities.remove(c.getX(), c.getZ());
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
        return spawnNPC(name,location,"","");
    }

    @Override
    public NMSEntity getNMSEntity(Entity en2) {
        if (en2 instanceof CraftHologramEntity) {
            return new NMSHologram() {
                Entity en = en2;
                @Override
                public List<Entity> getLines() {
                    return ((CraftHologramEntity) en).getLines();
                }

                @Override
                public void setLines(HologramFactory.HologramObject... lines) {
                    ((CraftHologramEntity) en).setLines(lines);
                }

                @Override
                public void setLine(int i, String line) {
                    ((CraftHologramEntity) en).setLine(i, line);
                }

                @Override
                public void addLine(String line) {
                    ((CraftHologramEntity) en).addLine(line);
                }

                @Override
                public void addItem(ItemStack stack) {
                    ((CraftHologramEntity) en).addItem(stack);
                }

                @Override
                public void addBlock(ItemStack stack) {
                    ((CraftHologramEntity) en).addBlock(stack);
                }

                @Override
                public void removeLine(int idx) {
                    ((CraftHologramEntity) en).removeLine(idx);
                }

                @Override
                public void remove() {
                    en.remove();
                }

                @Override
                public void setFrozen(boolean b) {
                    if (b) {
                        en.setMetadata(NMSEntity.FROZEN_TAG, new FixedMetadataValue(NMSUtils.getInstance(), true));
                    } else {
                        if (en.hasMetadata(NMSEntity.FROZEN_TAG))
                            en.removeMetadata(NMSEntity.FROZEN_TAG,NMSUtils.getInstance());
                    }
                }

                @Override
                public void setCollides(boolean b) {
                    if (b) {
                        en.setMetadata(NMSEntity.COLLIDE_TAG, new FixedMetadataValue(NMSUtils.getInstance(), true));
                    } else {
                        if (en.hasMetadata(NMSEntity.COLLIDE_TAG))
                            en.removeMetadata(NMSEntity.COLLIDE_TAG,NMSUtils.getInstance());
                    }
                }

                @Override
                public void setWillSave(boolean b) {
                    if (b) {
                        en.setMetadata(NMSEntity.SAVE_TAG, new FixedMetadataValue(NMSUtils.getInstance(), true));
                    } else {
                        if (en.hasMetadata(NMSEntity.SAVE_TAG))
                            en.removeMetadata(NMSEntity.SAVE_TAG,NMSUtils.getInstance());
                    }
                }

                @Override
                public void spawn() {
                    if (!this.willSave()) {
                        NMSUtilImpl.addEntityToWorld(((CraftEntity)en).getHandle().world, ((CraftEntity)en).getHandle());
                    }
                }

                @Override
                public boolean willSave() {
                    return en.hasMetadata(NMSEntity.SAVE_TAG);
                }

                @Override
                public Entity getEntity() {
                    return en;
                }

                @Override
                public boolean isFrozen() {
                    return en.hasMetadata(NMSEntity.FROZEN_TAG);
                }

                @Override
                public boolean willCollide() {
                    return en.hasMetadata(NMSEntity.COLLIDE_TAG);
                }
            };
        } else  if (en2.getType() == EntityType.ARMOR_STAND) {
            return new NMSArmorStand() {
                Entity en = en2;
                public void lock() {
                    NBTTagCompound tag = NMSUtilImpl.getTag(((CraftEntity)en).getHandle());
                    tag.setInt("DisabledSlots", 2039583);
                    ((EntityArmorStand)((CraftEntity)en).getHandle()).a(tag);
                }
                public void unlock() {
                    NBTTagCompound tag = NMSUtilImpl.getTag(((CraftEntity)en).getHandle());
                    tag.setInt("DisabledSlots", 0);
                    ((EntityArmorStand)((CraftEntity)en).getHandle()).a(tag);
                }

                @Override
                public void setFrozen(boolean b) {
                    if (b) {
                        en.setMetadata(NMSEntity.FROZEN_TAG, new FixedMetadataValue(NMSUtils.getInstance(), true));
                    } else {
                        if (en.hasMetadata(NMSEntity.FROZEN_TAG))
                            en.removeMetadata(NMSEntity.FROZEN_TAG,NMSUtils.getInstance());
                    }
                }

                @Override
                public void setCollides(boolean b) {
                    if (b) {
                        en.setMetadata(NMSEntity.COLLIDE_TAG, new FixedMetadataValue(NMSUtils.getInstance(), true));
                    } else {
                        if (en.hasMetadata(NMSEntity.COLLIDE_TAG))
                            en.removeMetadata(NMSEntity.COLLIDE_TAG,NMSUtils.getInstance());
                    }
                }

                @Override
                public void setWillSave(boolean b) {
                    if (b) {
                        en.setMetadata(NMSEntity.SAVE_TAG, new FixedMetadataValue(NMSUtils.getInstance(), true));
                    } else {
                        if (en.hasMetadata(NMSEntity.SAVE_TAG))
                            en.removeMetadata(NMSEntity.SAVE_TAG,NMSUtils.getInstance());
                    }
                }

                @Override
                public void spawn() {
                    if (!this.willSave()) {
                        NMSUtilImpl.addEntityToWorld(((CraftEntity)en).getHandle().world, ((CraftEntity)en).getHandle());
                    }
                }

                @Override
                public boolean willSave() {
                    return en.hasMetadata(NMSEntity.SAVE_TAG);
                }

                @Override
                public boolean isFrozen() {
                    return en.hasMetadata(NMSEntity.FROZEN_TAG);
                }

                @Override
                public boolean willCollide() {
                    return en.hasMetadata(NMSEntity.COLLIDE_TAG);
                }
                @Override
                public ArmorStand getEntity() {
                    return (ArmorStand) en;
                }
            };
        } else {
            return new NMSEntity() {
                Entity en = en2;
                @Override
                public void setFrozen(boolean b) {
                    if (b) {
                        en.setMetadata(NMSEntity.FROZEN_TAG, new FixedMetadataValue(NMSUtils.getInstance(), true));
                    } else {
                        if (en.hasMetadata(NMSEntity.FROZEN_TAG))
                            en.removeMetadata(NMSEntity.FROZEN_TAG,NMSUtils.getInstance());
                    }
                }

                @Override
                public void setCollides(boolean b) {
                    if (b) {
                        if (!new MockUtil().isMock(((CraftEntity) en).getHandle())) {
                            net.minecraft.server.v1_9_R1.Entity en3 = spy(((CraftEntity) en).getHandle());
                            doAnswer(Collideable.callback).when(en3).m();
                            en3.getWorld().removeEntity(((CraftEntity) en).getHandle());
                            NMSUtilImpl.addEntityToWorld(en3.getWorld(), en3);
                            en = en3.getBukkitEntity();
                        }
                        en.setMetadata(NMSEntity.COLLIDE_TAG, new FixedMetadataValue(NMSUtils.getInstance(), true));
                    } else {
                        if (en.hasMetadata(NMSEntity.COLLIDE_TAG))
                            en.removeMetadata(NMSEntity.COLLIDE_TAG,NMSUtils.getInstance());
                    }
                }

                @Override
                public void setWillSave(boolean b) {
                    if (b) {
                        en.setMetadata(NMSEntity.SAVE_TAG, new FixedMetadataValue(NMSUtils.getInstance(), true));
                    } else {
                        if (en.hasMetadata(NMSEntity.SAVE_TAG))
                            en.removeMetadata(NMSEntity.SAVE_TAG,NMSUtils.getInstance());
                    }
                }

                @Override
                public void spawn() {
                    if (!this.willSave()) {
                        NMSUtilImpl.addEntityToWorld(((CraftEntity)en).getHandle().world, ((CraftEntity)en).getHandle());
                    }
                }

                @Override
                public boolean willSave() {
                    return en.hasMetadata(NMSEntity.SAVE_TAG);
                }

                @Override
                public boolean isFrozen() {
                    return en.hasMetadata(NMSEntity.FROZEN_TAG);
                }

                @Override
                public boolean willCollide() {
                    return en.hasMetadata(NMSEntity.COLLIDE_TAG);
                }
                @Override
                public Entity getEntity() {
                    return en;
                }
            };
        }
    }

    @Override
    public boolean isNMSEntity(Entity en) {
        net.minecraft.server.v1_9_R1.Entity added = ((CraftEntity)en).getHandle();
        return en.getType() != EntityType.PLAYER && !Enhancer.isEnhanced(added.getClass());
    }

    @Override
    public void run() {
        for (World w: Bukkit.getWorlds()) {
            for (Entity en: w.getEntities()) {
                net.minecraft.server.v1_9_R1.Entity added = ((CraftEntity)en).getHandle();
                if (added.lastX != added.locX || added.lastY != added.locY || added.lastZ != added.locZ || added.lastPitch != added.pitch || added.lastYaw != added.yaw) {
                    Bukkit.getPluginManager().callEvent(new EntityMoveEvent(en,added.lastX,added.lastY,added.lastZ,added.locX,added.locY,added.locZ,added.pitch,added.lastPitch,added.yaw,added.lastYaw));
                }
            }
        }
    }
}
