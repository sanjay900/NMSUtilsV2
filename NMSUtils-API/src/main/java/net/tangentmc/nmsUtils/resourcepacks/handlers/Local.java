package net.tangentmc.nmsUtils.resourcepacks.handlers;

import net.tangentmc.nmsUtils.resourcepacks.ResourcePackHandler;
import org.bukkit.configuration.ConfigurationSection;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Local extends ResourcePackHandler {
    private String path;
    private String url;

    public Local(ConfigurationSection config) {
        super(config);
        config = config.getConfigurationSection("local");
        path = config.getString("folder_path");
        url = config.getString("url");
    }

    @Override
    public void uploadZip(byte[] zip) throws Exception {
        Files.write(Paths.get(path),zip);
    }

    @Override
    public String getUrl() throws IOException {
        return url+"?t="+System.currentTimeMillis();
    }
}
