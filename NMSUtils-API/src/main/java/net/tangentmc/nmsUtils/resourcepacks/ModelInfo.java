package net.tangentmc.nmsUtils.resourcepacks;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.*;
import java.util.*;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ModelInfo implements ConfigurationSerializable {
    private boolean enabled = true;
    private String name = null;
    private String breakSound = null;
    private List<String> pattern = null;
    private boolean breakImmediately = false;
    private String permission = null;
    private List<String> lore = null;
    private short itemDurability;
    private Map<String,String> ingredients;
    @SuppressWarnings("unchecked")
    public static ModelInfo deserialize(Map<String, Object> args) {
        ModelInfo info = new ModelInfo();
        info.enabled = (boolean) args.get("enabled");
        info.name = (String) args.get("name");
        if (args.containsKey("breakSound")) {
            info.breakSound =  (String) args.get("breakSound");
        }
        if (args.containsKey("breakImmediately")) {
            info.breakImmediately = (boolean) args.get("breakImmediately");
        }
        if (args.containsKey("recipe")) {
            info.pattern = (List<String>) ((Map<String, Object>)args.get("recipe")).get("pattern");
            info.ingredients = (Map<String, String>) ((Map<String, Object>)args.get("recipe")).get("ingredients");
        }
        if (args.containsKey("permission")) {
            info.permission =  (String) args.get("permission");
        }
        if (args.containsKey("lore")) {
            info.lore = (List<String>) args.get("lore");
        }
        return info;
    }

    public void applyToMeta(ItemMeta meta) {
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&',name));
        if (lore != null) meta.setLore(lore.stream().map(s -> ChatColor.translateAlternateColorCodes('&',s)).collect(Collectors.toList()));
    }
    @java.lang.Override
    public Map<String, Object> serialize() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("enabled",enabled);
        map.put("name",name);
        if (breakSound != null) {
            map.put("breakSound", breakSound);
            map.put("breakImmediately", breakImmediately);
        }
        if (pattern != null) {
            HashMap<String,Object> recipe = new HashMap<>();
            recipe.put("pattern",pattern);
            recipe.put("ingredients",ingredients);
            map.put("recipe",recipe);
        }
        if (permission != null) {
            map.put("permission",permission);
        }
        if (lore != null) {
            map.put("lore",lore);
        }
        return map;
    }

    public void updateViaCommand(String[] args2, CommandSender sender) {
        if (args2.length < 2) {
            sender.sendMessage("Invalid arguments for command!");
            return;
        }
        switch (args2[0]) {
            case "name":
                name = String.join(" ",args2);
                name = name.substring(name.indexOf(' '));
            case "recipe":
                assignFromCraftingWindow();
        }
    }

    private void assignFromCraftingWindow() {
        //TODO: this
    }

    public ModelInfo(String itemName) {
        name = itemName.replace(".json","");
        permission = name.substring(name.lastIndexOf("/")+1);
    }
}
