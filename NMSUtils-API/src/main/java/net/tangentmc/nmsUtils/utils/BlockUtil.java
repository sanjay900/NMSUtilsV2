package net.tangentmc.nmsUtils.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Directional;
import org.bukkit.material.MaterialData;

public class BlockUtil {
	public static ItemStack getStackFromString(String bstr) {
		final int bid = bstr.split(":").length > 1 ? Integer.parseInt(bstr.split(":")[0]) : Integer.parseInt(bstr);
		final int bdata = bstr.split(":").length > 1 ? Integer.parseInt(bstr.split(":")[1]) : 0;
		return new ItemStack(bid,1,(short)bdata);
	}
	public static boolean compareBlockToString(V10Block block, String blockData)
	{
	  return compareBlockToString(block.getHandle().getBlock(), blockData);
	}
	
	@SuppressWarnings("deprecation")
	public static boolean compareBlockToString(Block block, String blockData) {
		String[] blockArr = blockData.split(":");
		if (blockArr.length > 1)
			return (block.getTypeId() == Integer.parseInt(blockArr[0]) && block.getData() == Integer.parseInt(blockArr[1]));
		else
			return block.getTypeId() == Integer.parseInt(blockArr[0]);
	}
	
	public static void setBlockData(V10Block block, String blockData) {
	  setBlockData(block.getHandle().getBlock(), blockData);
	}

	@SuppressWarnings("deprecation")
	public static void setBlockData(Block block, String blockData) {
		String[] blockArr = blockData.split(":");
		block.setTypeId(Integer.parseInt(blockArr[0]));
		if (blockArr.length > 1)
			block.setData((byte) Integer.parseInt(blockArr[1]));
	}

	@SuppressWarnings("deprecation")
	public static String getBlockData(Block block) {
		if (block.getData() != 0)
			return block.getTypeId() + ":" + block.getData();
		return Integer.toString(block.getTypeId());
	}
	public static List<Block> getNearbyBlocks(Location location, int Radius) {
        List<Block> Blocks = new ArrayList<Block>();

        for (int X = location.getBlockX() - Radius; X <= location.getBlockX()
                + Radius; X++) {
            for (int Y = location.getBlockY() - Radius; Y <= location
                    .getBlockY() + Radius; Y++) {
                for (int Z = location.getBlockZ() - Radius; Z <= location
                        .getBlockZ() + Radius; Z++) {
                    Block block = location.getWorld().getBlockAt(X, Y, Z);
                    if (!block.isEmpty()) {
                        Blocks.add(block);
                    }
                }
            }
        }

        return Blocks;
    }
	public static BlockFace getFaceOfMaterial(Block block, BlockFace[] faces, String material, HashMap<BlockFace, Block> faceMap) {
		Block block2;
		for (BlockFace face : faces)
		{
			if(faceMap.containsKey(face))
				block2 = faceMap.get(face);
			else
			{
				block2 = block.getRelative(face);
				faceMap.put(face, block2);
			}
			if (compareBlockToString(block2, material))
				return face;
		}
		return null;
	}
	
	@SuppressWarnings("deprecation")
	public static Byte rotateBlock(Material block, Byte bdata, BlockFace origin, BlockFace newOrientation)
	{
		if (block.getNewData(bdata) instanceof Directional)
		{
			Directional directional = (Directional) block.getNewData(bdata);
			
			int diff = newOrientation.ordinal() - origin.ordinal();
			
			if (directional.getFacing().ordinal() + diff < 0) diff += 4;
			if (directional.getFacing().ordinal() + diff > 3) diff -= 4;
			
			directional.setFacingDirection(BlockFace.values()[directional.getFacing().ordinal() + diff]);
			return ((MaterialData) directional).getData();
		}
		else
			return bdata;
		
	}

}
