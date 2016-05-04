package net.tangentmc.nmsUtils.entities;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import net.tangentmc.nmsUtils.NMSUtils;
import net.tangentmc.nmsUtils.utils.AnimatedMessage;
import net.tangentmc.nmsUtils.utils.ImageMessage;

public class HologramFactory {
	@Data
	@AllArgsConstructor
	public static class HologramObject {
		HologramType type;
		Object object;
	}

	public enum HologramType {
		TEXT,BLOCK,ITEM,HEAD,TouchPart
	}
	@Getter
	ArrayList<HologramObject> lines = new ArrayList<>();
	Location loc = null;
	public HologramFactory withLocation(Location loc) {
		this.loc = loc;
		return this;
	}
	public HologramFactory withLine(String s) {
		lines.add(new HologramObject(HologramType.TEXT,s));
		return this;
	}
	public HologramFactory withItem(ItemStack is) {
		lines.add(new HologramObject(HologramType.ITEM,is));
		return this;
	}
	public HologramFactory withBlock(ItemStack is) {
		lines.add(new HologramObject(HologramType.BLOCK,is));
		return this;
	}
	/**
	 * Hologram part with an item on a armorstand's head
	 * @param is
	 * @return
	 */
	public HologramFactory withHead(ItemStack is, double height) {
		lines.add(new HologramObject(HologramType.HEAD,new HeadItem(height,is)));
		return this;
	}
	public HologramFactory withHead(ItemStack is) {
		return withHead(is,0.4375);
	}
	public HologramFactory withLines(String[] lines) {
		for (String s: lines) {
			this.withLine(s);
		}
		return this;
	}
	public HologramFactory withImage(String imagePath, int height,char imgChar) throws IOException {
		String[] lines = new ImageMessage(ImageIO.read(new File(imagePath)), height, imgChar).getLines();
		for (String s: lines) {
			this.withLine(s);
		}
		return this;
	}
	public HologramFactory withImageUrl(String imageUrl, int height,char imgChar) throws IOException {
		String[] lines = new ImageMessage(ImageIO.read(new URL(imageUrl)), height, imgChar).getLines();
		for (String s: lines) {
			this.withLine(s);
		}
		return this;
	}
	/**
	 * 
	 * @param imagePath
	 * @param delay - how many ticks to wait on a frame
	 * @param height
	 * @param imgChar
	 * @return
	 * @throws IOException
	 */
	public HologramFactory withGif(String imagePath, int delay, int height,char imgChar) throws IOException {
		AnimatedMessage message = new AnimatedMessage(new File(imagePath), delay, height, imgChar);
		lines.add(new HologramObject(HologramType.TEXT, message));
		return this;
	}
	public HologramFactory withGifUrl(String imagePath, int delay, int height,char imgChar) throws IOException {
		AnimatedMessage message = new AnimatedMessage(new URL(imagePath), delay, height, imgChar);
		lines.add(new HologramObject(HologramType.TEXT, message));
		return this;
	}
	public NMSEntity build() {
		if (this.loc == null) {
			System.out.println("Unable to spawn holgoram without location!");
			Thread.dumpStack();
			return null;
		}
		if (this.lines.isEmpty()) {
			System.out.println("Unable to spawn holgoram without lines!");
			Thread.dumpStack();
			return null;
		}
		//NMSUtils.getInstance().getUtil().spawnHologram(loc, lines)
		return null;
	}
	@Getter
	@AllArgsConstructor
	public static class HeadItem {
		double height;
		ItemStack stack;
	}
}
