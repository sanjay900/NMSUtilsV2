package net.tangentmc.nmsUtils.resourcepacks.handlers;

import lombok.Getter;
import net.tangentmc.nmsUtils.NMSUtils;
import net.tangentmc.nmsUtils.resourcepacks.ResourcePackHandler;
import org.bukkit.configuration.ConfigurationSection;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Local extends ResourcePackHandler {
    private String path;
    private String url;
    @Getter
    private String hash;

    public Local(ConfigurationSection config) {
        super(config);
        config = config.getConfigurationSection("local");
        path = config.getString("folder_path");
        url = config.getString("url");
        hash = config.getString("uploaded_hash");
    }

    @Override
    public void uploadZip(byte[] zip, String hash) throws Exception {
        Files.write(Paths.get(path),zip);
        this.hash = hash;
        NMSUtils.getInstance().getConfig().set("local.uploaded_hash",this.hash);
        NMSUtils.getInstance().saveConfig();
    }

    @Override
    public String getUrl() throws IOException {
        return url+"?t="+System.currentTimeMillis();
    }
}
