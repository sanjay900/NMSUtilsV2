package net.tangentmc.nmsUtils.resourcepacks.handlers;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import net.tangentmc.nmsUtils.NMSUtils;
import net.tangentmc.nmsUtils.resourcepacks.ResourcePackHandler;
import org.bukkit.configuration.ConfigurationSection;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class SFTP extends ResourcePackHandler {
    private String username;
    private String password;
    private String hostname;
    private boolean isKeyBased;
    private String key;
    private String serverPath;
    private String url;
    public SFTP(ConfigurationSection config) {
        super(config);
        config = config.getConfigurationSection("sftp");
        url = config.getString("url");
        username = config.getString("auth.username");
        password = config.getString("auth.password");
        hostname = config.getString("hostname");
        serverPath = config.getString("server_path");
        key = config.getString("auth.key_file");
        isKeyBased = config.getBoolean("auth.keyBasedAuthentication");
    }

    @Override
    public void uploadZip(byte[] zip) throws Exception {
        Session s = null;
        ChannelSftp chan = null;
        try {
            s = getSession();
            chan = getSFTPChannel(s);
            chan.cd(serverPath);
            chan.put(new ByteArrayInputStream(zip),zipName);
        } finally {
            if(chan!= null) {
                chan.exit();
                chan.disconnect();
            }
            if (s != null) {
                s.disconnect();
            }
        }
    }

    @Override
    public String getUrl() throws IOException {
        return url+"?t="+System.currentTimeMillis();
    }

    private Session getSession() throws JSchException {
        JSch jsch = new JSch();
        if (isKeyBased) {
            if (!password.isEmpty())
                jsch.addIdentity(NMSUtils.getInstance().getDataFolder()+"/"+key,password);
            else
                jsch.addIdentity(NMSUtils.getInstance().getDataFolder()+"/"+key);
        }
        String hostname = this.hostname.split(":")[0];
        int port = 22;
        if (this.hostname.contains(":")) {
            port = Integer.parseInt(this.hostname.split(":")[1]);
        }
        Session session = jsch.getSession(username, hostname, port);
        if (!isKeyBased) {
            session.setPassword(password);
        }
        java.util.Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.connect();
        return session;
    }
    private ChannelSftp getSFTPChannel(Session session) throws JSchException {
        ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");
        channel.connect();
        return channel;
    }
}
