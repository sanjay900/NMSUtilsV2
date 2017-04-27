package net.tangentmc.nmsUtils.resourcepacks;

import org.bukkit.configuration.ConfigurationSection;

import java.io.IOException;

public abstract class ResourcePackHandler {
    protected String zipName;
    public ResourcePackHandler(ConfigurationSection config) {
        this.zipName = config.getString("pack_name");
    }
    public abstract void uploadZip(byte[] zip, String hash) throws Exception;
    public abstract String getUrl() throws IOException;
    public abstract String getHash() throws IOException;
}
