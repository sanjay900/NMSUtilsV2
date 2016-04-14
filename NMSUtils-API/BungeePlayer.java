package net.tangentmc.bungee.objects;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;

import lombok.SneakyThrows;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.chat.ComponentSerializer;
import net.tangentmc.TangentShared.SharedPlugin;
import net.tangentmc.TangentShared.objects.TangentLocation;
import net.tangentmc.TangentShared.player.SharedPlayer;
import net.tangentmc.TangentShared.player.TangentPlayer;
import net.tangentmc.TangentShared.util.HashGenerator;
import net.tangentmc.TangentShared.util.fanciful.FancyMessage;
import net.tangentmc.bungee.Main;
import net.tangentmc.bungee.packet.ResourcePackChangePacket;

public class BungeePlayer implements SharedPlayer {
	public BungeePlayer(ProxiedPlayer pl) {
		this.uuid = pl.getUniqueId();
		this.name = pl.getName();
	}
	UUID uuid;
	String name;
	@Override
	public UUID getUniqueID() {
		return uuid;
	}

	@Override
	public String getName() {
		return name;
	}
	TangentLocation location = null;
	@Override
	public TangentLocation getLocation() {
		SharedPlugin.getInstance().getSocketIOHandler().emit("getLocation", new Object[]{uuid}, args -> {
			location = (TangentLocation) args[0];
		});
		return location;
	}
	@Override
	public void teleport(TangentLocation to) {
		if (to.getServer() != getServer() && to.getServer() != null) {
			getPlayer().connect(ProxyServer.getInstance().getServerInfo(to.getServer()));
			ProxyServer.getInstance().getScheduler()
			.schedule(Main.getInstance(), 
					() -> getPlayer().chat(to.getTeleportString()), 100, TimeUnit.MILLISECONDS);
		} else {
			getPlayer().chat(to.getTeleportString());
		}
	}

	@Override
	public String getServer() {
		if (!isOnline()) return "";
		return getPlayer().getServer().getInfo().getName();
	}
	public ProxiedPlayer getPlayer() {
		return ProxyServer.getInstance().getPlayer(uuid);
	}
	String world = "";
	@Override
	@SneakyThrows
	public String getWorld() {
		JSONObject obj = new JSONObject();
		obj.put("uuid", uuid);
		obj.put("server", getServer());
		SharedPlugin.getInstance().getSocketIOHandler().emit("getWorld", new Object[]{obj}, args -> {
			world = (String) args[0];
		});
		return world;
	}

	@Override
	public boolean isOnline() {
		return getPlayer() != null;
	}

	@Override
	public void sendMessage(String message) {
		getPlayer().sendMessage(message);
	}

	@Override
	public List<String> getPermissions() {
		return Main.getInstance().getPlayerManager().getTangentPlayer(this).getPermissions();
	}

	@Override
	public void setTexture(String texture) {
		ResourcePackChangePacket p = new ResourcePackChangePacket();
		p.setURL(texture);
		p.setHash(HashGenerator.makeSHA1Hash(texture));
		getPlayer().unsafe().sendPacket(p);
	}

	@Override
	public void setDisplayName(TangentPlayer pl) {
		//Bungeecord only lets you set names of 16 characters.... Handle it in the tab manager instead
		getPlayer().setDisplayName(pl.getName());
	}

	@Override
	public void sendMessage(FancyMessage message) {
		BaseComponent[] components = ComponentSerializer.parse(message.toJSONString());
		getPlayer().sendMessage(components);
	}

}
