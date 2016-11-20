package net.tangentmc.nmsUtils.utils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.Plugin;

public class CommandBuilder
{
    private static CommandMap commandMap;
    private static Map<String,Command> commandHashMap;
    private String commandLabel;
    private String description;
    private List<String> aliases;
    private String usage;
    private String permission;
    private String permissionMessage;
    private String fromPlugin;
    private CommandExecutor commandExecutor;
    private TabExecutor tabExecutor;

    public CommandBuilder(String command)
    {
        commandLabel = command;
    }

    public void build()
    {
        register();
    }

    public CommandBuilder withCommandExecutor(CommandExecutor exec)
    {
        commandExecutor = exec;
        return this;
    }

    public CommandBuilder withPlugin(Plugin plugin)
    {
        fromPlugin = plugin.getName();
        return this;
    }

    public CommandBuilder withPermissionMessage(String message)
    {
        permissionMessage = ChatColor.translateAlternateColorCodes('&', message);
        return this;
    }

    public CommandBuilder withPermission(String permission)
    {
        this.permission = permission;
        return this;
    }

    public CommandBuilder withUsage(String usage)
    {
        this.usage = usage;
        return this;
    }

    public CommandBuilder withAliases(String... aliases)
    {
        this.aliases = Arrays.asList(aliases);
        return this;
    }

    public CommandBuilder withDescription(String description)
    {
        this.description = description;
        return this;
    }

    public CommandBuilder withTabExecutor(TabExecutor tab)
    {
        tabExecutor = tab;
        return this;
    }

    private void register()
    {
        if ((commandLabel == null) || (commandLabel.isEmpty())) {
            throw new CommandNotPreparedException("Command does not have a name.");
        }
        ReflectCommand command = new ReflectCommand(commandLabel);

        if (commandExecutor == null) {
            throw new CommandNotPreparedException(commandLabel + " does not have an executor.");
        }
        if (aliases != null) {
            command.setAliases(aliases);
        }
        if (description != null) {
            command.setDescription(description);
        }
        if (permission != null) {
            command.setPermission(permission);
        }
        if (permissionMessage != null) {
            command.setPermissionMessage(permissionMessage);
        }
        if (usage != null) {
            command.setUsage(usage);
        }
        if (tabExecutor != null) {
            command.setTabExecutor(tabExecutor);
        }
        //Deregister existing commands, fix for reloading plugins
        if (getCommandMap().getCommand(commandLabel) != null) {
            getKnownCommands().remove(commandLabel);
        }
        getCommandMap().register(commandLabel, fromPlugin != null ? fromPlugin : "", command);
        command.setExecutor(commandExecutor);
    }
    private Map<String,Command> getKnownCommands() {
        if (commandHashMap != null) return commandHashMap;
        try
        {
            Field f = getCommandMap().getClass().getDeclaredField("knownCommands");
            f.setAccessible(true);
            commandHashMap = (Map<String, Command>) f.get(getCommandMap());
            return commandHashMap;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return getKnownCommands();
    }
    private CommandMap getCommandMap()
    {
        if (commandMap == null) {
            try
            {
                Field f = Bukkit.getServer().getClass().getDeclaredField("commandMap");
                f.setAccessible(true);
                commandMap = (CommandMap)f.get(Bukkit.getServer());
                return getCommandMap();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        } else {
            return commandMap;
        }
        return getCommandMap();
    }

    private final class ReflectCommand
            extends Command
    {
        private CommandExecutor executor;
        private TabExecutor tabExecutor;

        public void setExecutor(CommandExecutor executor)
        {
            this.executor = executor;
        }

        public void setTabExecutor(TabExecutor tabExecutor)
        {
            this.tabExecutor = tabExecutor;
        }

        protected ReflectCommand(String command)
        {
            super(command);
        }

        public boolean execute(CommandSender sender, String commandLabel, String[] args)
        {
            return (executor != null) && (executor.onCommand(sender, this, commandLabel, args));
        }

        public List<String> tabComplete(CommandSender sender, String commandLabel, String[] args)
        {
            if (tabExecutor != null) {
                return tabExecutor.onTabComplete(sender, this, usage, args);
            }
            return null;
        }
    }

    public class CommandNotPreparedException
            extends RuntimeException
    {
        public CommandNotPreparedException(String message)
        {
            super();
        }
    }

    public static CommandBuilder buildCommand(String command)
    {
        return new CommandBuilder(command);
    }
}
