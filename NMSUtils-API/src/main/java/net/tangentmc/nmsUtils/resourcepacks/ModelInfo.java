package net.tangentmc.nmsUtils.resourcepacks;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.ComponentBuilder.FormatRetention;
import net.tangentmc.nmsUtils.NMSUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.conversations.*;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.*;
import java.lang.Override;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static net.md_5.bungee.api.ChatColor.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ModelInfo implements ConfigurationSerializable {
    private boolean enabled = true;
    private String displayName = null;
    private String name = null;
    private List<String> pattern = null;
    private boolean breakImmediately = false;
    private String permission = null;
    private List<String> lore = null;
    private Map<String,String> ingredients;
    private boolean rotatable = false;
    private boolean collisions = false;
    private boolean rotateAnyAngle = false;
    private String modelType;
    private short modelId;

    public ModelInfo(String itemName, String modelType, short modelId) {
        displayName = itemName.replace(".json","");
        permission = itemName.substring(itemName.lastIndexOf("/")+1);
        name = itemName;
        this.modelType = modelType;
        this.modelId = modelId;
    }

    @SuppressWarnings("unchecked")
    public static ModelInfo deserialize(Map<String, Object> args) {
        ModelInfo info = new ModelInfo();
        info.enabled = (boolean) args.get("enabled");
        info.displayName = (String) args.get("displayName");
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
        if (args.containsKey("rotatable")) {
            info.rotatable = (boolean) args.get("rotatable");
        }
        if (args.containsKey("collisions")) {
            info.collisions = (boolean) args.get("collisions");
        }
        if (args.containsKey("rotateAnyAngle")) {
            info.rotateAnyAngle = (boolean) args.get("rotateAnyAngle");
        }
        if (args.containsKey("shortName")) {
            info.name = (String) args.get("shortName");
        }
        return info;
    }

    public void applyToMeta(ItemMeta meta) {
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&',displayName));
        if (lore != null) meta.setLore(lore.stream().map(s -> ChatColor.translateAlternateColorCodes('&',s)).collect(Collectors.toList()));
    }
    @java.lang.Override
    public Map<String, Object> serialize() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("enabled",enabled);
        map.put("displayName",displayName);
        map.put("breakImmediately", breakImmediately);

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
        map.put("rotatable",rotatable);
        map.put("collisions",collisions);
        map.put("rotateAnyAngle",rotateAnyAngle);
        return map;
    }
    public static List<String> getCommands() {
        return Arrays.asList("displayname","breakimmediately","recipe","blockcollision","canrotate","lock90","lore");
    }
    public void updateViaCommand(String[] args2, CommandSender sender) {
        if (args2.length < 1) {
            sender.sendMessage("Invalid arguments for command!");
            return;
        }

        if (args2.length == 1) {
            switch (args2[0].toLowerCase()) {
                case "displayname":
                    askForString("Please enter a display name",str->this.displayName = ChatColor.RESET+str, (Conversable) sender);
                    break;
                case "lore":
                    List<String> newLore = new ArrayList<>();
                    new ConversationFactory(NMSUtils.getInstance()).withFirstPrompt(new StringPrompt () {
                        boolean first = true;
                        @Override
                        public String getPromptText(ConversationContext context) {
                            if (first)
                                return "Please enter the items lore. Type stop to stop entering lore";
                            return "Enter the next line. Type stop to stop entering lore";
                        }

                        @Override
                        public Prompt acceptInput(ConversationContext context, String input) {
                            newLore.add(ChatColor.translateAlternateColorCodes('&',"&r"+input));
                            if (input.equals("stop")) {
                                lore = newLore;
                                NMSUtils.getInstance().getResourcePackAPI().saveConf();
                                return null;
                            }
                            first = false;
                            return this;
                        }
                    }).buildConversation((Conversable) sender).begin();
                    break;
                case "breakimmediately":
                    askForBoolean("Should this block break immediately? (true/false)",bool->this.breakImmediately = bool, (Conversable) sender);
                    break;
                case "recipe":
                    assignFromCraftingWindow();
                    break;
                case "blockcollision":
                    askForBoolean("Should this block have collisions? (true/false)",bool->this.collisions = bool, (Conversable) sender);
                    break;
                case "canrotate":
                    askForBoolean("Should this block rotate? (true/false)",bool->this.rotatable = bool, (Conversable) sender);
                    break;
                case "lock90":
                    askForBoolean("Should this block limit its rotations to 90 degrees? (true/false)\n(note that if this setting is false, collisions are automatically disabled.)",
                            bool->this.rotateAnyAngle = !bool, (Conversable) sender);
                    break;
                default:
                    sender.sendMessage("Invalid arguments for command!");
                    break;

            }
        }

    }
    private void askForBoolean(String prompt, Consumer<Boolean> consumer, Conversable sender) {
        new ConversationFactory(NMSUtils.getInstance()).withFirstPrompt(new BooleanPrompt() {
            @Override
            public String getPromptText(ConversationContext context) {
                return prompt;
            }

            @Override
            protected Prompt acceptValidatedInput(ConversationContext context, boolean input) {
                consumer.accept(input);
                NMSUtils.getInstance().getResourcePackAPI().saveConf();
                return null;
            }
        }).buildConversation(sender).begin();
    }
    private void askForString(String prompt, Consumer<String> consumer, Conversable sender) {
        new ConversationFactory(NMSUtils.getInstance()).withFirstPrompt(new StringPrompt () {
            @Override
            public String getPromptText(ConversationContext context) {
                return prompt;
            }

            @Override
            public Prompt acceptInput(ConversationContext context, String input) {
                consumer.accept(ChatColor.translateAlternateColorCodes('&',input));
                NMSUtils.getInstance().getResourcePackAPI().saveConf();
                return null;
            }
        }).buildConversation(sender).begin();
    }

    private void assignFromCraftingWindow() {
        //TODO: this
        NMSUtils.getInstance().getResourcePackAPI().saveConf();
    }

    public String toString() {
        return TextComponent.toLegacyText(getComponent());
    }
    public ComponentBuilder makeLabel(ComponentBuilder c, String label, String command) {
        return c.append(label, FormatRetention.NONE).color(YELLOW)
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(command)))
                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND,command));
    }
    public BaseComponent[] getComponent() {
        ComponentBuilder cb = new ComponentBuilder("Model Information: ").color(AQUA).underlined(true).bold(true).append("\n").append("\n");
        cb.append("Enabled: ").color(YELLOW).append(enabled?"True":"False", FormatRetention.NONE).color(enabled?GREEN:RED).append("\n")
                .append("Name: ").color(YELLOW).append(name, FormatRetention.NONE).append("\n")
                .append("Type: ").color(YELLOW).append(modelType, FormatRetention.NONE).append("\n")
                .append("ID: ", FormatRetention.NONE).color(YELLOW).append(modelId+"", FormatRetention.NONE).append("\n");
        makeLabel(cb,"Display Name: ","/updatemodel "+ name +" displayName")
                .append(displayName, FormatRetention.NONE).append("\n");
        makeLabel(cb,"Break Immediately: ","/updatemodel "+ name +" breakImmediately")
                .append(breakImmediately?"True":"False", FormatRetention.NONE).color(breakImmediately?GREEN:RED).append("\n");
        makeLabel(cb,"Lore: ","/updatemodel "+ name +" lore")
                .append(lore==null?"No lore set":"\n"+String.join("\n",lore), FormatRetention.NONE).append("\n");
        makeLabel(cb,"Crafting recipe: ","/updatemodel "+ name +" recipe");
        makeLabel(cb,"Click to View","/modelinfo "+ name +" recipe").color(ChatColor.WHITE).bold(true).append("\n");
        makeLabel(cb,"Block collision: ","/updatemodel "+ name +" blockCollision")
                .append(collisions?"True":"False", FormatRetention.NONE).color(collisions?GREEN:RED).append("\n")
                .append("Rotation Information: ", FormatRetention.NONE).color(YELLOW).append("\n");
        makeLabel(cb,"   Can Rotate: ","/updatemodel "+ name +" canRotate")
                .append(rotatable?"True":"False", FormatRetention.NONE).color(rotatable?GREEN:RED).append("\n");
        makeLabel(cb,"   Lock rotations to 90 degrees: ","/updatemodel "+ name +" lock90")
                .append(!rotateAnyAngle?"True":"False", FormatRetention.NONE).color(!rotateAnyAngle?GREEN:RED)
                .create();
        return cb.create();
    }

    public void assignTypeId(String itemName, String modelType, short modelId) {
        this.name = itemName;
        this.modelType = modelType;
        this.modelId = modelId;
    }
}
