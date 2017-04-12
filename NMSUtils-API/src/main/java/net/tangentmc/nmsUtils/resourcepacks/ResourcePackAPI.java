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
import net.tangentmc.nmsUtils.resourcepacks.predicates.*;
import net.tangentmc.nmsUtils.utils.MCException;
import net.tangentmc.nmsUtils.utils.Utils;
import org.apache.commons.io.IOUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
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
    static {
        ConfigurationSerialization.registerClass(ModelInfo.class, "ModelInfo");
    }
    private static JSONObject bowDisplay = new JSONObject("{" +
            "\"display\": {\n" +
            "        \"thirdperson_righthand\": {\n" +
            "            \"rotation\": [ -80, 260, -40 ],\n" +
            "            \"translation\": [ -1, -2, 2.5 ],\n" +
            "            \"scale\": [ 0.9, 0.9, 0.9 ]\n" +
            "        },\n" +
            "        \"thirdperson_lefthand\": {\n" +
            "            \"rotation\": [ -80, -280, 40 ],\n" +
            "            \"translation\": [ -1, -2, 2.5 ],\n" +
            "            \"scale\": [ 0.9, 0.9, 0.9 ]\n" +
            "        },\n" +
            "        \"firstperson_righthand\": {\n" +
            "            \"rotation\": [ 0, -90, 25 ],\n" +
            "            \"translation\": [ 1.13, 3.2, 1.13],\n" +
            "            \"scale\": [ 0.68, 0.68, 0.68 ]\n" +
            "        },\n" +
            "        \"firstperson_lefthand\": {\n" +
            "            \"rotation\": [ 0, 90, -25 ],\n" +
            "            \"translation\": [ 1.13, 3.2, 1.13],\n" +
            "            \"scale\": [ 0.68, 0.68, 0.68 ]\n" +
            "        }\n" +
            "    }" +
            "}");
    private static JSONObject shieldDisplay = new JSONObject("{" +
            "\"display\": {\n" +
            "        \"thirdperson_righthand\": {\n" +
            "            \"rotation\": [ 0, 90, 0 ],\n" +
            "            \"translation\": [ 10.51, 6, -4 ],\n" +
            "            \"scale\": [ 1, 1, 1 ]\n" +
            "        },\n" +
            "        \"thirdperson_lefthand\": {\n" +
            "            \"rotation\": [ 0, 90, 0 ],\n" +
            "            \"translation\": [ 10.51, 6, 12 ],\n" +
            "            \"scale\": [ 1, 1, 1 ]\n" +
            "        },\n" +
            "        \"firstperson_righthand\": {\n" +
            "            \"rotation\": [ 0, 180, 5 ],\n" +
            "            \"translation\": [ -10, 2, -10 ],\n" +
            "            \"scale\": [ 1.25, 1.25, 1.25 ]\n" +
            "        },\n" +
            "        \"firstperson_lefthand\": {\n" +
            "            \"rotation\": [ 0, 180, 5 ],\n" +
            "            \"translation\": [ 10, 0, -10 ],\n" +
            "            \"scale\": [ 1.25, 1.25, 1.25 ]\n" +
            "        },\n" +
            "        \"gui\": {\n" +
            "            \"rotation\": [ 15, -25, -5 ],\n" +
            "            \"translation\": [ 2, 3, 0 ],\n" +
            "            \"scale\": [ 0.65, 0.65, 0.65 ]\n" +
            "        },\n" +
            "        \"fixed\": {\n" +
            "            \"rotation\": [ 0, 180, 0 ],\n" +
            "            \"translation\": [ -2, 4, -5],\n" +
            "            \"scale\":[ 0.5, 0.5, 0.5]\n" +
            "        },\n" +
            "        \"ground\": {\n" +
            "            \"rotation\": [ 0, 0, 0 ],\n" +
            "            \"translation\": [ 4, 4, 2],\n" +
            "            \"scale\":[ 0.25, 0.25, 0.25]\n" +
            "        }\n" +
            "    }" +
            "}");
    //Display data for positioning blocks.
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
    private Map<String,BiMap<String,Short>> mapping = new HashMap<>();
    private FileConfiguration modelInfo;
    private List<ResourcePackHandler> handlerList = new ArrayList<>();
    private Map<String,Short> nextKeys = new HashMap<>();
    private File mappingFile;
    private File modelInfoFile;
    @SuppressWarnings("unchecked")
    public ResourcePackAPI() {
        //Start at key 1, damage 0 is dedicated to the default item
        //items includes blocks.
        nextKeys.put("items",(short)1);
        nextKeys.put("weapons",(short)1);
        nextKeys.put("shields",(short)1);
        nextKeys.put("bows",(short)1);
        mapping.putIfAbsent("items",HashBiMap.create());
        mapping.putIfAbsent("weapons",HashBiMap.create());
        mapping.putIfAbsent("shields",HashBiMap.create());
        mapping.putIfAbsent("bows",HashBiMap.create());
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
            JSONObject mapping = getJSON(mappingFile.toPath());
            mapping.keys().forEachRemaining(key -> {
                JSONObject vals = mapping.getJSONObject(key);
                vals.keys().forEachRemaining( key2 -> {
                    ResourcePackAPI.this.mapping.get(key).put(key2, (short) vals.getInt(key2));
                    if (nextKeys.get(key) == vals.getInt(key2)) {
                        nextKeys.put(key, (short) (nextKeys.get(key)+1));
                    }
                });
            });
            modelInfoFile = new File(NMSUtils.getInstance().getDataFolder(),"modelInfo.yml");
            modelInfoFile.createNewFile();
            modelInfo = Utils.getConfig(modelInfoFile);
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
        if (!mapping.get("items").containsKey(block)) {
            throw new InvalidBlockException(block);
        }
        short id = mapping.get("items").get(block);
        Block b = l.getBlock();
        b.setType(Material.MOB_SPAWNER);
        try {
            NMSUtils.getInstance().getUtil().updateBlockNBT(b, String.format("{RequiredPlayerRange:0s," +
                    "SpawnData:{id:\"minecraft:armor_stand\",Invisible:1,Marker:1," +
                    "ArmorItems:[0:{},1:{},2:{},3:{id:\"minecraft:diamond_hoe\",Count:1b,Damage:%ds,tag:{Unbreakable:1}}]}}", id));
        } catch (MCException e) {
            e.printStackTrace();
        }
    }
    public ItemStack getWeapon(String weapon) {
        return getItemStack(weapon,Material.DIAMOND_SWORD,"weapons");
    }
    public ItemStack getShield(String shield) {
        return getItemStack(shield,Material.SHIELD,"shields");
    }
    public ItemStack getBow(String bow)  {
        return getItemStack(bow,Material.BOW,"bows");
    }
    public ItemStack getItemStack(String item) {
        return getItemStack(item,Material.DIAMOND_HOE,"items");
    }
    private ItemStack getItemStack(String item, Material material, String itemType) {
        if (!mapping.containsKey(itemType) || !mapping.get(itemType).containsKey(item)) {
            throw new InvalidItemException(item);
        }
        int id = mapping.get(itemType).get(item);
        if (itemType.equals("items")) {
            if (id > Material.DIAMOND_HOE.getMaxDurability()) {
                id -= Material.DIAMOND_HOE.getMaxDurability();
                material = Material.DIAMOND_PICKAXE;
            }
        }
        ItemStack it = new ItemStack(material,1,(short)id);
        ItemMeta meta = it.getItemMeta();
        meta.spigot().setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        meta.addItemFlags(ItemFlag.HIDE_DESTROYS);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        ModelInfo info = (ModelInfo) modelInfo.get(item,new ModelInfo(item));
        info.applyToMeta(meta);
        it.setItemMeta(meta);
        return it;
    }
    public ModelInfo getModelInfo(String item) {
        if (!modelInfo.contains(item)) {
            setInformation(item,new ModelInfo(item));
        }
        return (ModelInfo) modelInfo.get(item);
    }
    public void uploadZIP() throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); ZipOutputStream zos = new ZipOutputStream(baos)) {
            //Load the pack on its own
            compressWithFilter(Paths.get(NMSUtils.getInstance().getDataFolder().toString(),"pack"),null,zos,(path,fileName) -> IOUtils.copy(new FileInputStream(path.toFile()), zos),this::filterFiles);
            //Parse custom items
            compressWithFilter(Paths.get(NMSUtils.getInstance().getDataFolder().toString(),"customItems"),MODEL_PREFIX+"item",zos,(path,fileName) -> processItem(getJSON(path),fileName.replace(MODEL_PREFIX,""),"items",zos),path->true);
            //Parse custom shields
            compressWithFilter(Paths.get(NMSUtils.getInstance().getDataFolder().toString(),"customShields"),MODEL_PREFIX+"item",zos,(path,fileName) -> processItem(getJSON(path),fileName.replace(MODEL_PREFIX,""),"shields",zos),path->true);
            //Parse custom bows
            compressWithFilter(Paths.get(NMSUtils.getInstance().getDataFolder().toString(),"customBows"),MODEL_PREFIX+"item",zos,(path,fileName) -> processItem(getJSON(path),fileName.replace(MODEL_PREFIX,""),"bows",zos),path->true);
            //Parse custom weapons
            compressWithFilter(Paths.get(NMSUtils.getInstance().getDataFolder().toString(),"customWeapons"),MODEL_PREFIX+"item",zos,(path,fileName) -> processItem(getJSON(path),fileName.replace(MODEL_PREFIX,""),"weapons",zos),path->true);
            //Parse custom blocks, add in block metadata
            compressWithFilter(Paths.get(NMSUtils.getInstance().getDataFolder().toString(),"customBlocks"),MODEL_PREFIX+"block",zos,(path,fileName) -> processBlock(getJSON(path),fileName.replace(MODEL_PREFIX,""),zos),path->true);
            Files.write(mappingFile.toPath(),new JSONObject(mapping).toString(4).getBytes());
            //First half of items
            writeFile(MODEL_PREFIX+"item/diamond_hoe.json", convertMapping(mapping.get("items"),
                    "item/diamond_hoe", Material.DIAMOND_HOE,(short)0,"items"),zos);
            //Second half of items
            writeFile(MODEL_PREFIX+"item/diamond_pickaxe.json", convertMapping(mapping.get("items"),
                    "item/diamond_pickaxe", Material.DIAMOND_PICKAXE,Material.DIAMOND_HOE.getMaxDurability(),"items"),zos);
            //weapons
            writeFile(MODEL_PREFIX+"item/diamond_sword.json", convertMapping(mapping.get("weapons"),
                    "item/diamond_sword", Material.DIAMOND_SWORD,(short)0,"weapons"),zos);
            //bows
            writeFile(MODEL_PREFIX+"item/bow.json", convertMapping(mapping.get("bows"),
                    "item/bow", Material.BOW,(short)0,"bows"),zos);
            //shields
            writeFile(MODEL_PREFIX+"item/shield.json", convertMapping(mapping.get("shields"),
                    "item/shield", Material.SHIELD,(short)0,"shields"),zos);
            zos.closeEntry();
            zos.close();
            for (ResourcePackHandler resourcePackHandler : handlerList) {
                resourcePackHandler.uploadZip(baos.toByteArray());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void writeFile(String fileName,String json,ZipOutputStream zos) throws IOException {
        zos.putNextEntry(new ZipEntry(fileName));
        zos.write(json.getBytes());
        zos.closeEntry();
    }
    private String convertMapping(BiMap<String, Short> mapping, String defaultModelName, Material material, short minDurability, String typeName) {
        Map<String,Object> itemData = new HashMap<>();
        itemData.put("parent","item/handheld");
        Map<String,Object> textures = new HashMap<>();
        List<Override> overrides = new ArrayList<>();
        switch (typeName) {
            case "shields":
                itemData.put("display",shieldDisplay.toMap().get("display"));
                itemData.put("parent","builtin/entity");
                textures.put("shield", "entity/shield_base_nopattern");
                textures.put("pattern", "entity/shield_base");
                textures.put("particle", "items/shears");
                overrides.add(new Override(new ShieldPredicate(0,0,0), defaultModelName));
                overrides.add(new Override(new ShieldPredicate(0,0,1),"item/shield_blocking"));
                break;
            case "bows":
                textures.put("layer0", "items/bow_standby");
                itemData.put("display",bowDisplay.toMap().get("display"));
                overrides.add(new Override(new BowPullingDamagePredicate(0,0.25), defaultModelName));
                overrides.add(new Override(new BowPullingDamagePredicate(0,0.50), defaultModelName));
                overrides.add(new Override(new BowPullingDamagePredicate(0,0.75), defaultModelName));
                overrides.add(new Override(new BowPullingPredicate(1), "item/bow_pulling_0"));
                overrides.add(new Override(new BowPullPullingPredicate(0.65,1), "item/bow_pulling_1"));
                overrides.add(new Override(new BowPullPullingPredicate(0.9,1), "item/bow_pulling_2"));
                break;
            default:
                textures.put("layer0", "items"+ defaultModelName.substring(4));
                //Add a default undamaged predicate
                overrides.add(new Override(new DamagePredicate(0,0), defaultModelName));
                break;
        }
        itemData.put("textures",textures);
        //sort as minecraft predicates are done in order
        TreeMap<Short,String> inv = new TreeMap<>(mapping.inverse());
        for (short id : inv.keySet()) {
            String model = inv.get(id);
            if (minDurability != 0) {
                if (id <= minDurability) continue;
            }
            id -= minDurability;
            if (id > material.getMaxDurability()) continue;
            model = model.replace(".json","");
            double realId = (double)id/material.getMaxDurability();
            switch (typeName) {
                case "shields":
                    overrides.add(new Override(new ShieldPredicate(0,realId,0),model));
                    overrides.add(new Override(new ShieldPredicate(0,realId,1),model+"_blocking"));
                    break;
                case "bows" :
                    overrides.add(new Override(new BowPullingDamageDamagedPredicate(0,realId, 0),model));
                    overrides.add(new Override(new BowPullingDamageDamagedPredicate(1,realId, 1),model+"_0"));
                    overrides.add(new Override(new BowDamagePredicate(0,realId,1,0.65),model+"_1"));
                    overrides.add(new Override(new BowDamagePredicate(0,realId,1,0.9),model+"_2"));
                    break;
                default:
                    overrides.add(new Override(new DamagePredicate(0,realId),model));
                    break;
            }
        }
        switch (typeName) {
            case "shields":
                overrides.add(new Override(new ShieldPredicate(1,0,0), defaultModelName));
                overrides.add(new Override(new ShieldPredicate(1,0,1),"item/shield_blocking"));
                break;
            case "bows" :
                overrides.add(new Override(new BowPullingDamageDamagedPredicate(0,0, 1),"item/bow"));
                overrides.add(new Override(new BowPullingDamageDamagedPredicate(1,0, 1),"item/bow_pulling_0"));
                overrides.add(new Override(new BowDamagePredicate(1,0,1,0.65),"item/bow_pulling_1"));
                overrides.add(new Override(new BowDamagePredicate(1,0,1,0.9),"item/bow_pulling_2"));
                break;
            default:
                //Add a predicate that is applied when using the hoe normally.
                overrides.add(new Override(new DamagePredicate(1,0), defaultModelName));
                break;
        }
        itemData.put("overrides",overrides);
        Gson gson = new GsonBuilder().enableComplexMapKeySerialization()
                .setPrettyPrinting().create();
        return gson.toJson(itemData);
    }
    private void predicateToMap(String json, String itemType, short minValue, Material material) {
        List<Map> overrides = (List<Map>) new Gson().fromJson(json,Map.class).get("overrides");
        overrides.forEach(override -> {
            String model = (String) override.get("model");
            if ((itemType.equals("shields") && model.endsWith("_blocking")) || (itemType.equals("bows") && (model.endsWith("_0") || model.endsWith("_1") || model.endsWith("_2")))) return;
            Map<String,Double> predicate = (Map<String, Double>) override.get("predicate");
            short realDamage = (short) (Math.round(predicate.get("damage")*material.getMaxDurability()));
            realDamage+=minValue;
            this.nextKeys.put(itemType,(short) Math.max(this.nextKeys.get(itemType),realDamage+1));
            if (mapping.get(itemType).containsValue(realDamage)) {
                System.out.println(model+" is attempting to bind to an existing id: "+realDamage);
                return;
            }
            mapping.get(itemType).put(model,realDamage);
        });
    }
    //We need to deal with mapping + the diamond hoe here.
    private void processBlock(JSONObject json, String name, OutputStream os) throws IOException {
        //Apply model for placing inside mob spawner
        if (!json.has("display")) {
            json.put("display",blockDisplay);
        }
        IOUtils.write(json.toString(),os);
        //Blocks are just items with a special display value.
        if (!mapping.containsKey(name)) {
            mapping.get("items").put(name,findNextKey("items"));
        }
    }

    private short findNextKey(String itemType) {
        short nextKey = nextKeys.get(itemType);
        while (mapping.get(itemType).containsValue(++nextKey));
        nextKeys.put(itemType,nextKey);
        return nextKey;
    }


    private void processItem(JSONObject json, String name, String itemType, OutputStream os) throws IOException {
        IOUtils.write(json.toString(),os);
        //Dont add the seperate _blocking as its own thing
        if ((itemType.equals("shields") && name.endsWith("_blocking")) || (itemType.equals("bows") && (name.endsWith("_0") || name.endsWith("_1") || name.endsWith("_2")))) return;
        if (!mapping.get(itemType).containsKey(name)) {
            mapping.get(itemType).put(name,findNextKey(itemType));
        }
    }

    private JSONObject getJSON(Path p) throws IOException {
        String text = String.join("\n",Files.readAllLines(p));
        if (!text.startsWith("{")) return new JSONObject();
        return new JSONObject(text);
    }
    private boolean filterFiles(Path path) {
        try {
            if (path.getFileName().toString().equals("diamond_hoe.json")) {
                predicateToMap(String.join("\n",Files.readAllLines(path)),"items",(short)0, Material.DIAMOND_HOE);
                System.out.println("File diamond_hoe.json imported. Deleting old model file.");
                path.toFile().delete();
                return false;
            }
            if (path.getFileName().toString().equals("diamond_pickaxe.json")) {
                predicateToMap(String.join("\n",Files.readAllLines(path)),"items",Material.DIAMOND_HOE.getMaxDurability(), Material.DIAMOND_PICKAXE);
                System.out.println("File diamond_pickaxe.json imported. Deleting old model file.");
                path.toFile().delete();
                return false;
            }
            if (path.getFileName().toString().equals("diamond_sword.json")) {
                predicateToMap(String.join("\n",Files.readAllLines(path)),"weapons",(short)0, Material.DIAMOND_SWORD);
                System.out.println("File diamond_sword.json imported. Deleting old model file.");
                path.toFile().delete();
                return false;
            }
            if (path.getFileName().toString().equals("bow.json")) {
                predicateToMap(String.join("\n",Files.readAllLines(path)),"bows",(short)0, Material.BOW);
                System.out.println("File bow.json imported. Deleting old model file.");
                path.toFile().delete();
                return false;
            }
            if (path.getFileName().toString().equals("shield.json")) {
                predicateToMap(String.join("\n",Files.readAllLines(path)),"shields",(short)0, Material.SHIELD);
                System.out.println("File shield.json imported. Deleting old model file.");
                path.toFile().delete();
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
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
                if (!filter.shouldCopy(file)) return;
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
        String itemType = getTypeFromMaterial(item.getType());
        if (itemType == null) return null;
        if (!item.hasItemMeta()) return null;
        if (!item.getItemMeta().spigot().isUnbreakable()) return null;
        short durability = item.getDurability();
        if (item.getType() == Material.DIAMOND_PICKAXE) {
            durability += Material.DIAMOND_HOE.getMaxDurability();
        }
        if (!mapping.get(itemType).containsValue(durability)) return null;
        return mapping.get(itemType).inverse().get(durability);
    }
    private String getTypeFromMaterial(Material m) {
        switch (m) {
            case DIAMOND_HOE:
            case DIAMOND_PICKAXE:
                return "items";
            case BOW:
                return "bows";
            case SHIELD:
                return "shields";
            case DIAMOND_SWORD:
                return "weapons";
            default: return null;
        }
    }

    private interface FilterConsumer {
        boolean shouldCopy(Path path) throws IOException;
    }
    private interface PathConsumer {
        void accept(Path path, String filename) throws IOException;
    }
    private static final String MODEL_PREFIX = "assets/minecraft/models/";
    public void setInformation(String model, ModelInfo info) {
        this.modelInfo.set(model, info);
        try {
            this.modelInfo.save(modelInfoFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static final HashMap<String,String[]> skipMap = new HashMap<>();
    static {
        skipMap.put("shields",new String[]{"_blocking"});
        skipMap.put("bows",new String[]{"_0","_1","_2"});
    }
}
