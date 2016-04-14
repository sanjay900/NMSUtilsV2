package net.tangentmc.nmsUtils.imagemap;

import java.awt.Image;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import lombok.Getter;

public class CustomMapRenderer extends MapRenderer {

	private MapUtils util;
	private Image image;

	public CustomMapRenderer(MapUtils mapUtils, Image image) {
		this.util = mapUtils;
		this.image = image;
	}
	@Getter
	List<UUID> rendered = new ArrayList<>();
	@Override
	public void render(MapView arg0, MapCanvas canvas, Player arg2) {

        if (image != null && !rendered.contains(arg2.getUniqueId()))
        {
            canvas.drawImage(0, 0, image);
            rendered.add(arg2.getUniqueId());
        }
	}

}
