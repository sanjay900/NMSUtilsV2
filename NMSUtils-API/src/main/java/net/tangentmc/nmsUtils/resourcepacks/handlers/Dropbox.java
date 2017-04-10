package net.tangentmc.nmsUtils.resourcepacks.handlers;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.DeleteErrorException;
import com.dropbox.core.v2.files.WriteMode;
import net.tangentmc.nmsUtils.NMSUtils;
import net.tangentmc.nmsUtils.resourcepacks.ResourcePackHandler;
import org.bukkit.configuration.ConfigurationSection;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class Dropbox extends ResourcePackHandler {
    private static final String DOWNLOAD_OPTION = "&raw=1";
    private String accessToken;
    private String serverPath;
    private String url;

    public Dropbox(ConfigurationSection config) {
        super(config);
        config = config.getConfigurationSection("dropbox");
        this.accessToken = config.getString("access_token");
        this.serverPath = config.getString("folder_path");
        this.url = config.getString("uploaded_url");
    }

    @Override
    public void uploadZip(byte[] zip) throws DbxException, IOException {
        String fileName = serverPath+zipName;
        //If there was a problem deleting, then there probably was nothing to delete.
        try {
            getDropBoxClient().files().delete(fileName);
        } catch (DeleteErrorException ignored) {}
        getDropBoxClient().files().uploadBuilder(fileName)
                .withMode(WriteMode.OVERWRITE)
                .uploadAndFinish(new ByteArrayInputStream(zip));
        url = getDropBoxClient().sharing().createSharedLinkWithSettings(fileName).getUrl()+DOWNLOAD_OPTION;
        NMSUtils.getInstance().getConfig().set("resourcepackapi.dropbox.url",this.url);
        NMSUtils.getInstance().saveConfig();
    }

    @Override
    public String getUrl() {
        return url;
    }
    private DbxClientV2 getDropBoxClient() {
        DbxRequestConfig requestConfig = new DbxRequestConfig("Minecraft/NMSUtilsUploader");
        return new DbxClientV2(requestConfig,accessToken);
    }
}
