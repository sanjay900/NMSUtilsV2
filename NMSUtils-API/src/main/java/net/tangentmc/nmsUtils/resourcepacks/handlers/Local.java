package net.tangentmc.nmsUtils.resourcepacks.handlers;

import lombok.Getter;
import net.tangentmc.nmsUtils.NMSUtils;
import net.tangentmc.nmsUtils.resourcepacks.ResourcePackHandler;
import org.apache.commons.codec.digest.DigestUtils;
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
        this.hash = config.getString("zip_hash");
    }

    @Override
    public void uploadZip(byte[] zip) throws Exception {
        Files.write(Paths.get(path),zip);
        hash = DigestUtils.sha1Hex(zip).toLowerCase();
        NMSUtils.getInstance().getConfig().set("resourcepackapi.local.zip_hash",this.hash);
        NMSUtils.getInstance().saveConfig();
    }

    @Override
    public String getUrl() throws IOException {
        return url;
    }
}
