package net.tangentmc.nmsUtils.v1_8_R3;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.stream.Collectors;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.util.UnsafeList;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.world.ChunkUnloadEvent;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import lombok.Getter;
import lombok.SneakyThrows;
import net.minecraft.server.v1_8_R3.EntityInsentient;
import net.minecraft.server.v1_8_R3.MathHelper;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.PathfinderGoalSelector;
import net.minecraft.server.v1_8_R3.WorldServer;
import net.tangentmc.nmsUtils.NMSUtil;
import net.tangentmc.nmsUtils.NMSUtils;
import net.tangentmc.nmsUtils.entities.HologramFactory.HologramObject;
import net.tangentmc.nmsUtils.entities.NMSEntity;
import net.tangentmc.nmsUtils.entities.NMSHologram;
import net.tangentmc.nmsUtils.entities.NMSLaser;
import net.tangentmc.nmsUtils.entities.NPCManager;
import net.tangentmc.nmsUtils.jinglenote.JingleNoteManager;
import net.tangentmc.nmsUtils.jinglenote.MidiJingleSequencer;
import net.tangentmc.nmsUtils.utils.Validator;
import net.tangentmc.nmsUtils.v1_8_R3.entities.CraftHologramEntity.HologramEntity;
import net.tangentmc.nmsUtils.v1_8_R3.entities.LaserEntities.CraftLaserEntity.LaserEntity;
import net.tangentmc.nmsUtils.v1_8_R3.entities.NPC;
@Getter
public class NMSUtilImpl implements NMSUtil, Listener {
	private HashSet<UUID> riding = new HashSet<>();
	JingleNoteManager manager = new JingleNoteManager();
	private static Table<Integer,Integer,List<NMSEntity>> customEntities = HashBasedTable.create();
	NPCManager npcmanager;
	@SneakyThrows
	public NMSUtilImpl() {
		ProtocolLibrary.getProtocolManager().addPacketListener(new PacketListener(this));
		validateEntityMethod = net.minecraft.server.v1_8_R3.World.class.getDeclaredMethod("a", net.minecraft.server.v1_8_R3.Entity.class);
		validateEntityMethod.setAccessible(true);
		NMSEntityTypes.registerEntities();
		npcmanager = new NPCManager();
		Bukkit.getPluginManager().registerEvents(this, NMSUtils.getInstance());
		Bukkit.getWorlds().forEach(this::trackWorldEntities);
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
	public net.minecraft.server.v1_8_R3.World getWorld(World w) {
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
				rawIds[i] = ids.remove(0).intValue();
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
			rawIds[i] = ids.remove(0).intValue();
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
		net.minecraft.server.v1_8_R3.Entity en = ((CraftEntity)entity).getHandle();
		if (en.vehicle != null) {
			en = en.vehicle;
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
		net.minecraft.server.v1_8_R3.Entity ent = ((CraftEntity)entity).getHandle();
		ent.mount(null);

		ent.world = ((CraftWorld) location.getWorld()).getHandle();
		ent.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
		((WorldServer) getWorld(entity.getWorld())).getTracker().track(en);
		if (passenger != null) {
			entity.setPassenger(passenger);
		}
		return true;
	}
	public static NBTTagCompound getTag(net.minecraft.server.v1_8_R3.Entity en) {
		NBTTagCompound tag = en.getNBTTag();
		if(tag == null) {
			tag = new NBTTagCompound();
		}
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
		LaserEntity laser = new LaserEntity(((CraftWorld)init.getWorld()).getHandle(), init);
		return laser.getBukkitEntity();
	}

	@Override
	public NMSHologram spawnHologram(Location loc, ArrayList<HologramObject> lines) {
		HologramEntity holo = new HologramEntity(loc,lines);
		return holo.getBukkitEntity();
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
	public static boolean addEntityToWorld(net.minecraft.server.v1_8_R3.World world, net.minecraft.server.v1_8_R3.Entity nmsEntity) {
		Validator.isTrue(Bukkit.isPrimaryThread(), "Async entity add");

		if (validateEntityMethod == null) {
			return world.addEntity(nmsEntity, SpawnReason.CUSTOM);
		}
		final int chunkX = MathHelper.floor(nmsEntity.locX / 16.0);
		final int chunkZ = MathHelper.floor(nmsEntity.locZ / 16.0);
		//This function can be called to add entities that arent in loaded chunks. When that happenes,
		//queue them for later.
		if (!((WorldServer)world).chunkProviderServer.isChunkLoaded(chunkX, chunkZ)) {
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
			b.set(entity.targetSelector, new UnsafeList<>());
			c.set(entity.targetSelector, new UnsafeList<>());
			b.set(entity.goalSelector, new UnsafeList<>());
			c.set(entity.goalSelector, new UnsafeList<>());

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
}
