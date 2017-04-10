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
import org.apache.commons.io.IOUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;

public class ResourcePackAPI {
    private static JSONObject blockDisplay = new JSONObject("{\"head\": { \"rotation\": [ -30, 0, 0 ], \"translation\": [ 0, -30.75, -7.25 ], \"scale\": [ 3.0125, 3.0125, 3.0125 ]}}");
    private BiMap<String,Integer> mapping = HashBiMap.create();
    private List<ResourcePackHandler> handlerList = new ArrayList<>();
    //Start at key 1, damage 0 is dedicated to the default hoe.
    private int nextKey = 1;
    private File mappingFile;
    public ResourcePackAPI(boolean client) {
        mappingFile = new File(".");
        if (!client) {
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
            mappingFile = NMSUtils.getInstance().getDataFolder();
        }
        mappingFile = new File(mappingFile,"mapping.json");
        try {
            mappingFile.createNewFile();
            String text = String.join("\n",Files.readAllLines(mappingFile.toPath()));
            if (text.isEmpty()) {
                return;
            }
            JSONObject mapping = new JSONObject(text);
            mapping.keys().forEachRemaining(key -> {
                ResourcePackAPI.this.mapping.put(key,mapping.getInt(key));
                if (nextKey == mapping.getInt(key)) nextKey++;
            });
        } catch (IOException ignored) {}
    }
    public void uploadZipFile(String zip) throws Exception {
        byte[] bytes = Files.readAllBytes(Paths.get(zip));
        for (ResourcePackHandler resourcePackHandler : handlerList) {
            resourcePackHandler.uploadZip(bytes);
            System.out.println(resourcePackHandler.getUrl());
        }

    }
    public void exportZIP() throws IOException {
        try (FileOutputStream baos = new FileOutputStream("test.zip"); ZipOutputStream zos = new ZipOutputStream(baos)) {
            //Load the pack on its own
            compressWithFilter(Paths.get("pack"),null,zos,(path) -> IOUtils.copy(new FileInputStream(path.toFile()), zos),this::filterFiles);
            //Parse custom items
            compressWithFilter(Paths.get("customItems"),MODEL_PREFIX+"item",zos,(path) -> processItem(getJSON(path),path.toString().replace('\\','/'),zos),this::filterFiles);
            //Parse custom blocks, add in block metadata
            compressWithFilter(Paths.get("customBlocks"),MODEL_PREFIX+"block",zos,(path) -> processBlock(getJSON(path),path.toString().replace('\\','/'),zos),this::filterFiles);
            Files.write(mappingFile.toPath(),new JSONObject(mapping).toString(4).getBytes());
            zos.putNextEntry(new ZipEntry("assets/minecraft/models/item/diamond_hoe.json"));
            String hoeJSON = convertMappingToHoe(mapping);
            zos.write(hoeJSON.getBytes());
            zos.closeEntry();

        }
    }


    private String convertMappingToHoe(BiMap<String, Integer> mapping) {
        Map<String,Object> hoeData = new HashMap<>();
        hoeData.put("parent","item/handheld");
        Map<String,Object> textures = new HashMap();
        textures.put("layer0","items/diamond_hoe");
        hoeData.put("textures",textures);
        List<Override> overrides = new ArrayList<>();
        overrides.add(new Override(new Predicate(0,0),"item/diamond_hoe"));
        BiMap<Integer,String> inv = mapping.inverse();
        for (Integer id : inv.keySet()) {
            String model = inv.get(id);
            model = "item"+model.substring(model.indexOf("/")).replace(".json","");
            double realId = (double)id/DIAMOND_HOE_MAX;
            overrides.add(new Override(new Predicate(0,realId),model));
        }
        overrides.add(new Override(new Predicate(1,0),"item/diamond_hoe"));
        hoeData.put("overrides",overrides);
        Gson gson = new GsonBuilder().enableComplexMapKeySerialization()
                .setPrettyPrinting().create();
        System.out.println(gson.toJson(hoeData));
        return gson.toJson(hoeData);
    }
    private void predicateToMap(String json) {
        List<Map> overrides = (List<Map>) new Gson().fromJson(json,Map.class).get("overrides");
        overrides.forEach(override -> {
            Map<String,Double> predicate = (Map<String, Double>) override.get("predicate");
            int realDamage = (int) (Math.round(predicate.get("damage")*DIAMOND_HOE_MAX));
            this.nextKey = Math.max(this.nextKey,realDamage+1);
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
        int id = mapping.containsKey(name)?mapping.get(name):findNextKey();
        mapping.put(name,id);
    }

    private int findNextKey() {
        while (mapping.containsValue(nextKey++));
        return nextKey;
    }


    private void processItem(JSONObject json, String name, OutputStream os) throws IOException {
        IOUtils.write(json.toString(),os);
        int id = mapping.containsKey(name)?mapping.get(name):findNextKey();
        mapping.put(name,id);
    }

    public static void main(String[] args) {
        ResourcePackAPI api = new ResourcePackAPI(true);
        try {
            api.exportZIP();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private JSONObject getJSON(Path p) throws IOException {
        return new JSONObject(String.join("\n",Files.readAllLines(p)));
    }
    private boolean filterFiles(Path path) {
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
                if (!filter.shouldCopy(file)) return;
                //Substring away the pathName
                String fileName = file.toString().substring(path.toString().length());
                if (subDir != null) {
                    fileName = subDir+fileName;
                }
                if (fileName.startsWith(File.separator)) fileName = fileName.substring(1);
                if (file.toFile().isDirectory()) fileName+="/";
                if (fileName.length()==1) return;
                ZipEntry entry = new ZipEntry(fileName);
                zos.putNextEntry(entry);
                if (!file.toFile().isDirectory()) {
                    consumer.accept(file);
                }
                zos.closeEntry();
            } catch (IOException e) {
                if (e instanceof ZipException) return;
                e.printStackTrace();
            }
        });
    }
    private interface FilterConsumer {
        boolean shouldCopy(Path path) throws IOException;
    }
    private interface PathConsumer {
        void accept(Path path) throws IOException;
    }
    private static final String MODEL_PREFIX = "assets/minecraft/models/";
    private static final int DIAMOND_HOE_MAX = 1561;
}
