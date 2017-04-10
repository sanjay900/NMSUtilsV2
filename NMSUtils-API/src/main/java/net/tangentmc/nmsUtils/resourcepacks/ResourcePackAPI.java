package net.tangentmc.nmsUtils.resourcepacks;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.tangentmc.nmsUtils.NMSUtils;
import net.tangentmc.nmsUtils.resourcepacks.handlers.Dropbox;
import net.tangentmc.nmsUtils.resourcepacks.handlers.FTP;
import net.tangentmc.nmsUtils.resourcepacks.handlers.Local;
import net.tangentmc.nmsUtils.resourcepacks.handlers.SFTP;
import net.tangentmc.nmsUtils.utils.MCException;
import org.apache.commons.io.IOUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.json.JSONObject;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;

public class ResourcePackAPI {
    private static JSONObject blockDisplay = new JSONObject(
            "{"+
            "        \"head\": { "+
            "            \"rotation\": [ -30, 0, 0 ], " +
            "            \"translation\": [ 0, -30.75, -7.25 ], " +
            "            \"scale\": [ 3.0125, 3.0125, 3.0125 ]" +
            "        },"+
            "        \"gui\": {"+
            "            \"rotation\": [ 30, 225, 0 ],"+
            "            \"translation\": [ 0, 0, 0],"+
            "            \"scale\":[ 0.625, 0.625, 0.625 ]"+
            "        },"+
            "        \"ground\": {"+
            "            \"rotation\": [ 0, 0, 0 ],"+
            "            \"translation\": [ 0, 3, 0],"+
            "            \"scale\":[ 0.25, 0.25, 0.25 ]"+
            "        },"+
            "        \"fixed\": {"+
            "            \"rotation\": [ 0, 0, 0 ],"+
            "            \"translation\": [ 0, 0, 0],"+
            "            \"scale\":[ 0.5, 0.5, 0.5 ]"+
            "        },"+
            "        \"thirdperson_righthand\": {"+
            "            \"rotation\": [ 75, 45, 0 ],"+
            "            \"translation\": [ 0, 2.5, 0],"+
            "            \"scale\": [ 0.375, 0.375, 0.375 ]"+
            "        },"+
            "        \"firstperson_righthand\": {"+
            "            \"rotation\": [ 0, 45, 0 ],"+
            "            \"translation\": [ 0, 0, 0 ],"+
            "            \"scale\": [ 0.40, 0.40, 0.40 ]"+
            "        },"+
            "        \"firstperson_lefthand\": {"+
            "            \"rotation\": [ 0, 225, 0 ],"+
            "            \"translation\": [ 0, 0, 0 ],"+
            "            \"scale\": [ 0.40, 0.40, 0.40 ]"+
            "        }"+
            "}");
    private BiMap<String,Short> mapping = HashBiMap.create();
    private List<ResourcePackHandler> handlerList = new ArrayList<>();
    //Start at key 1, damage 0 is dedicated to the default hoe.
    private short nextKey = 1;
    private File mappingFile;
    public ResourcePackAPI() {
        FileConfiguration config = NMSUtils.getInstance().getConfig();
        ConfigurationSection section = config.getConfigurationSection("resourcepackapi");
        if (section.getBoolean("enable_dropbox")) {
            handlerList.add(new Dropbox(section));
        }
        if (section.getBoolean("enable_local")) {
            handlerList.add(new Local(section));
        }
        if (section.getBoolean("enable_ftp")) {
            handlerList.add(new FTP(section));
        }
        if (section.getBoolean("enable_sftp")) {
            handlerList.add(new SFTP(section));
        }
        mappingFile = new File(NMSUtils.getInstance().getDataFolder(),"mapping.json");
        try {
            mappingFile.createNewFile();
            String text = String.join("\n",Files.readAllLines(mappingFile.toPath()));
            if (text.isEmpty()) {
                return;
            }
            JSONObject mapping = new JSONObject(text);
            mapping.keys().forEachRemaining(key -> {
                ResourcePackAPI.this.mapping.put(key, (short) mapping.getInt(key));
                if (nextKey == mapping.getInt(key)) nextKey++;
            });
        } catch (IOException ignored) {}
    }
    public void updatePacks() {
        for (Player pl: Bukkit.getOnlinePlayers()) {
            updatePacks(pl);
        }
    }
    public void updatePacks(Player pl) {
        try {
            pl.setResourcePack(handlerList.get(0).getUrl());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void setBlock(Location l, String block) {
        if (!mapping.containsKey(block)) {
            throw new InvalidBlockException(block);
        }
        short id = mapping.get(block);
        Block b = l.getBlock();
        b.setType(Material.MOB_SPAWNER);
        try {
            NMSUtils.getInstance().getUtil().updateBlockNBT(b,String.format("{RequiredPlayerRange:0s," +
                    "SpawnData:{id:\"minecraft:armor_stand\",Invisible:1,Marker:1," +
                    "ArmorItems:[0:{},1:{},2:{},3:{id:\"minecraft:diamond_hoe\",Count:1b,Damage:%ds,tag:{Unbreakable:1}}]}}",id));
        } catch (MCException e) {
            e.printStackTrace();
        }
    }
    public ItemStack getItemStack(String item) {
        if (!mapping.containsKey(item)) {
            throw new InvalidItemException(item);
        }
        int id = mapping.get(item);
        ItemStack it = new ItemStack(Material.DIAMOND_HOE,1,(short)id);
        ItemMeta meta = it.getItemMeta();
        meta.spigot().setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        it.setItemMeta(meta);
        return it;
    }
    public void uploadZIP() throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); ZipOutputStream zos = new ZipOutputStream(baos)) {
            //Load the pack on its own
            compressWithFilter(Paths.get(NMSUtils.getInstance().getDataFolder().toString(),"pack"),null,zos,(path,fileName) -> IOUtils.copy(new FileInputStream(path.toFile()), zos),this::filterFiles);
            //Parse custom items
            compressWithFilter(Paths.get(NMSUtils.getInstance().getDataFolder().toString(),"customItems"),MODEL_PREFIX+"item",zos,(path,fileName) -> processItem(getJSON(path),fileName.replace(MODEL_PREFIX,""),zos),this::filterFiles);
            //Parse custom blocks, add in block metadata
            compressWithFilter(Paths.get(NMSUtils.getInstance().getDataFolder().toString(),"customBlocks"),MODEL_PREFIX+"block",zos,(path,fileName) -> processBlock(getJSON(path),fileName.replace(MODEL_PREFIX,""),zos),this::filterFiles);
            Files.write(mappingFile.toPath(),new JSONObject(mapping).toString(4).getBytes());
            zos.putNextEntry(new ZipEntry("assets/minecraft/models/item/diamond_hoe.json"));
            String hoeJSON = convertMappingToHoe(mapping);
            zos.write(hoeJSON.getBytes());
            zos.closeEntry();
            zos.close();
            for (ResourcePackHandler resourcePackHandler : handlerList) {
                resourcePackHandler.uploadZip(baos.toByteArray());
                System.out.println(resourcePackHandler.getUrl());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private String convertMappingToHoe(BiMap<String, Short> mapping) {
        Map<String,Object> hoeData = new HashMap<>();
        hoeData.put("parent","item/handheld");
        Map<String,Object> textures = new HashMap<>();
        textures.put("layer0","items/diamond_hoe");
        hoeData.put("textures",textures);
        List<Override> overrides = new ArrayList<>();
        overrides.add(new Override(new Predicate(0,0),"item/diamond_hoe"));
        //sort as minecraft predicates are done in order
        TreeMap<Short,String> inv = new TreeMap<>(mapping.inverse());
        for (Short id : inv.keySet()) {
            String model = inv.get(id);
            model = model.replace(".json","");
            double realId = (double)id/DIAMOND_HOE_MAX;
            overrides.add(new Override(new Predicate(0,realId),model));
        }
        overrides.add(new Override(new Predicate(1,0),"item/diamond_hoe"));
        hoeData.put("overrides",overrides);
        Gson gson = new GsonBuilder().enableComplexMapKeySerialization()
                .setPrettyPrinting().create();
        return gson.toJson(hoeData);
    }
    private void predicateToMap(String json) {
        List<Map> overrides = (List<Map>) new Gson().fromJson(json,Map.class).get("overrides");
        overrides.forEach(override -> {
            Map<String,Double> predicate = (Map<String, Double>) override.get("predicate");
            Short realDamage = (short) (Math.round(predicate.get("damage")*DIAMOND_HOE_MAX));
            this.nextKey = (short) Math.max(this.nextKey,realDamage+1);
            if (mapping.containsValue(realDamage)) {
                System.out.printf("Error importing predicate map as id %d already exists\n",realDamage);
            } else {
                mapping.put((String) override.get("model"),realDamage);
            }
        });
    }
    //We need to deal with mapping + the diamond hoe here.
    private void processBlock(JSONObject json, String name, OutputStream os) throws IOException {
        //Apply model for placing inside mob spawner
        if (!json.has("display")) {
            json.put("display",blockDisplay);
        }
        IOUtils.write(json.toString(),os);
        if (!mapping.containsKey(name)) {
            mapping.put(name,findNextKey());
        }
    }

    private short findNextKey() {
        while (mapping.containsValue(++nextKey));
        return nextKey;
    }


    private void processItem(JSONObject json, String name, OutputStream os) throws IOException {
        IOUtils.write(json.toString(),os);
        if (!mapping.containsKey(name)) {
            mapping.put(name,findNextKey());
        }
    }

    private JSONObject getJSON(Path p) throws IOException {
        return new JSONObject(String.join("\n",Files.readAllLines(p)));
    }
    private boolean filterFiles(Path path, String fileName) {
        if (path.endsWith("diamond_hoe.json")) {
            try {
                predicateToMap(String.join("\n",Files.readAllLines(path)));
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("File diamond_hoe imported. To avoid errors, delete this file from your pack as it has now been converted.");
            return false;
        }
        return true;
    }
    private void compressWithFilter(Path path, String subDir, ZipOutputStream zos, PathConsumer consumer, FilterConsumer filter) throws IOException {
        Files.find(path,999,(b,bfa) -> true).forEach(file -> {
            try {

                //Substring away the pathName
                String fileName = file.toString().substring(path.toString().length());
                if (subDir != null) {
                    fileName = subDir+fileName;
                }
                if (fileName.startsWith(File.separator)) fileName = fileName.substring(1);
                if (file.toFile().isDirectory()) fileName+="/";
                if (fileName.length()==1) return;
                if (!filter.shouldCopy(file, fileName)) return;
                fileName = fileName.replace("\\","/");
                ZipEntry entry = new ZipEntry(fileName);
                zos.putNextEntry(entry);
                if (!file.toFile().isDirectory()) {
                    consumer.accept(file, fileName.replace(".json",""));
                }
                zos.closeEntry();
            } catch (IOException e) {
                if (e instanceof ZipException) return;
                e.printStackTrace();
            }
        });
    }

    public String findItemFromStack(ItemStack item) {
        if (item == null) return null;
        if (item.getType() != Material.DIAMOND_HOE) return null;
        if (!item.hasItemMeta()) return null;
        if (!item.getItemMeta().spigot().isUnbreakable()) return null;
        if (!mapping.containsValue(item.getDurability())) return null;
        return mapping.inverse().get(item.getDurability());
    }

    private interface FilterConsumer {
        boolean shouldCopy(Path path, String filename) throws IOException;
    }
    private interface PathConsumer {
        void accept(Path path, String filename) throws IOException;
    }
    private static final String MODEL_PREFIX = "assets/minecraft/models/";
    private static final int DIAMOND_HOE_MAX = 1561;
}
