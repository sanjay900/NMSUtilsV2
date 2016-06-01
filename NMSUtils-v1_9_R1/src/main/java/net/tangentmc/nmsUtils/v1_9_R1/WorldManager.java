package net.tangentmc.nmsUtils.v1_9_R1;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.server.v1_9_R1.*;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.Proxy;
import net.tangentmc.nmsUtils.NMSUtil;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_9_R1.CraftServer;
import org.bukkit.craftbukkit.v1_9_R1.CraftWorld;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import lombok.SneakyThrows;
import net.tangentmc.nmsUtils.NMSUtils;
import net.tangentmc.nmsUtils.entities.NMSEntity;
import net.tangentmc.nmsUtils.events.EntityDespawnEvent;
import net.tangentmc.nmsUtils.events.EntitySpawnEvent;
import net.tangentmc.nmsUtils.utils.ReflectionManager;
import org.mockito.internal.util.MockUtil;

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
			//ignore the addition of a mocked entity
			if (!new MockUtil().isMock(added))
				Bukkit.getPluginManager().callEvent(new EntitySpawnEvent(added.getBukkitEntity()));
		}
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
	public void a(BlockPosition arg0) {}
	@Override
	public void a(SoundEffect arg0, BlockPosition arg1) {}
	@Override
	public void a(int arg0, BlockPosition arg1, int arg2) {}
	@Override
	public void a(EntityHuman arg0, int arg1, BlockPosition arg2, int arg3) {}
	@Override
	public void a(World arg0, BlockPosition arg1, IBlockData arg2, IBlockData arg3, int arg4) {}
	@Override
	public void a(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5) {}
	@Override
	public void a(EntityHuman arg0, SoundEffect arg1, SoundCategory arg2, double arg3, double arg4, double arg5,
				  float arg6, float arg7) {}
	@Override
	public void a(int arg0, boolean arg1, double arg2, double arg3, double arg4, double arg5, double arg6, double arg7,
				  int... arg8) {}
	@Override
	public void b(int arg0, BlockPosition arg1, int arg2) {}
}
