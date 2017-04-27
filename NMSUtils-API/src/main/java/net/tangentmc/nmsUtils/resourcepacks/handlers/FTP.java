package net.tangentmc.nmsUtils.resourcepacks.handlers;

import lombok.Getter;
import net.tangentmc.nmsUtils.NMSUtils;
import net.tangentmc.nmsUtils.resourcepacks.ResourcePackHandler;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.bukkit.configuration.ConfigurationSection;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

public class FTP extends ResourcePackHandler {
    private String username;
    private String password;
    private String url;
    private String uploadPath;
    private String hostname;
    @Getter
    private String hash;

    public FTP(ConfigurationSection config) {
        super(config);
        config = config.getConfigurationSection("ftp");
        this.username = config.getString("auth.username");
        this.password = config.getString("auth.password");
        this.url = config.getString("url");
        this.uploadPath = config.getString("server_path");
        this.hostname = config.getString("hostname");
        this.hash = config.getString("uploaded_hash");
    }

    @Override
    public void uploadZip(byte[] zip, String hash) throws IOException {
        FTPClient client = getFTPConnection();
            client.storeFile(uploadPath + zipName,new ByteArrayInputStream(zip));
        this.hash = hash;
        NMSUtils.getInstance().getConfig().set("ftp.uploaded_hash",this.hash);
        NMSUtils.getInstance().saveConfig();

    }

    @Override
    public String getUrl() throws IOException {
        return url+"?t="+System.currentTimeMillis();
    }

    public FTPClient getFTPConnection() throws IOException {
        FTPClient client = new FTPClient();
        String hostname = this.hostname.split(":")[0];
        int port = FTPClient.DEFAULT_PORT;
        if (this.hostname.contains(":")) {
            port = Integer.parseInt(this.hostname.split(":")[1]);
        }
        client.connect(hostname,port);
        client.login(username,password);
        return client;
    }
}
