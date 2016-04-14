package net.tangentmc.nmsUtils.v1_8_R3;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.scheduler.BukkitRunnable;

import lombok.SneakyThrows;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.EntityHuman;
import net.minecraft.server.v1_8_R3.EntityMinecartAbstract;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.IWorldAccess;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.World;
import net.minecraft.server.v1_8_R3.WorldServer;
import net.tangentmc.nmsUtils.NMSUtils;
import net.tangentmc.nmsUtils.entities.NMSEntity;
import net.tangentmc.nmsUtils.events.EntityDespawnEvent;
import net.tangentmc.nmsUtils.events.EntitySpawnEvent;
import net.tangentmc.nmsUtils.utils.ReflectionManager;

public class WorldManager implements IWorldAccess {
	private HashSet<EntityHuman> players = new HashSet<EntityHuman>();
	private static Field f;
	private static Field f2;
	WorldServer world;
	@SuppressWarnings("unchecked")
	@SneakyThrows
	public WorldManager(org.bukkit.World world) {
		this.world = ((CraftWorld)world).getHandle();
		this.world.addIWorldAccess(this);
		//Entities will return based on the world handle, 
		//while bukkit methods are based on the Bukkit.getServers() list of worlds
		CraftWorld nmsWorld = new NMSCraftWorld((CraftWorld) world);
		ReflectionManager.setFinalStatic(f2, this.world, nmsWorld);
		Map<String, org.bukkit.World> worlds = (Map<String, org.bukkit.World>) WorldManager.worlds.get(Bukkit.getServer());
		worlds.put(world.getName(), nmsWorld);
		ReflectionManager.setFinalStatic(WorldManager.worlds, Bukkit.getServer(), worlds);
	}
	private static Field worlds;
	static {
		try {
			f = World.class.getDeclaredField("u");
			f2 = World.class.getDeclaredField("world");
			worlds = CraftServer.class.getDeclaredField("worlds");
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		worlds.setAccessible(true);
		f.setAccessible(true);
		f2.setAccessible(true);
	}
	@SneakyThrows
	public void remove() {
		@SuppressWarnings("unchecked")
		List<IWorldAccess> l = (List<IWorldAccess>)f.get(world);
		l.remove(this);
		f.set(world, l);
		this.players.clear();
	}
	@Override
	public final void a(Entity added) {
		if (added != null) {
			if (added instanceof EntityPlayer && !this.players.add((EntityPlayer) added)) {
				return;
			}
			if (!(added.getBukkitEntity() instanceof NMSEntity) && !(added instanceof EntityPlayer) && added.getClass().getName().contains("net.minecraft.server")) {
				
				new BukkitRunnable(){
					@Override
					public void run() {
						replaceEntity(added);
					}}.runTaskLater(NMSUtils.getInstance(),1l);
					return;
			}
			Bukkit.getPluginManager().callEvent(new EntitySpawnEvent(added.getBukkitEntity()));
		}
	}
	protected void replaceEntity(Entity added) {
		NBTTagCompound old = new NBTTagCompound();
		added.d(old);
		int tickslived = added.ticksLived;
		world.removeEntity(added);
		Entity tnew = a(old);
		if (tnew != null) {
			tnew.ticksLived = tickslived;
			NMSUtilImpl.addEntityToWorld((WorldServer) world, tnew);
		}
	}
	public Entity a(NBTTagCompound nbttagcompound) {
		Entity entity = null;

		if ("Minecart".equals(nbttagcompound.getString("id"))) {
			nbttagcompound.setString("id", EntityMinecartAbstract.EnumMinecartType.a(nbttagcompound.getInt("Type")).b());
			nbttagcompound.remove("Type");
		}

		try {
			Class<? extends Entity> oclass = NMSEntityTypes.getClassById(nbttagcompound.getString("id"));
			if (oclass != null) {
				entity = oclass.getConstructor(new Class[] { World.class}).newInstance(new Object[] { world});
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}

		if (entity != null) {
			entity.f(nbttagcompound);
		} else {
			Bukkit.getLogger().warning("Skipping Entity with id " + nbttagcompound.getString("id"));
		}

		return entity;
	}

	@Override
	public final void b(Entity removed) {
		if (removed != null) {
			if (removed instanceof EntityPlayer && !this.players.remove(removed)) {
				return;
			}
			Bukkit.getPluginManager().callEvent(new EntityDespawnEvent(removed.getBukkitEntity()));
		}
	}
	@Override
	public void a(int i, boolean flag, double d0, double d1, double d2, double d3, double d4, double d5, int... aint) {}
	@Override
	public void a(String s, double d0, double d1, double d2, float f, float f1) {}
	@Override
	public void a(EntityHuman entityhuman, String s, double d0, double d1, double d2, float f, float f1) {}
	@Override
	public void a(int i, int j, int k, int l, int i1, int j1) {}
	@Override
	public void a(BlockPosition blockposition) {}
	@Override
	public void b(BlockPosition blockposition) {}
	@Override
	public void a(String s, BlockPosition blockposition) {}
	@Override
	public void a(EntityHuman entityhuman, int i, BlockPosition blockposition, int j) {}
	@Override
	public void a(int i, BlockPosition blockposition, int j) {}
	@Override
	public void b(int i, BlockPosition blockposition, int j) {}
}
