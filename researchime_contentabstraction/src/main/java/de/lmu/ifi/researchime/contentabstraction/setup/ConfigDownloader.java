package de.lmu.ifi.researchime.contentabstraction.setup;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import de.lmu.ifi.researchime.base.RestClient;
import de.lmu.ifi.researchime.base.logging.LogHelper;
import de.lmu.ifi.researchime.contentabstraction.model.config.LogicalList;
import de.lmu.ifi.researchime.contentabstraction.model.config.LogicalWordList;
import de.lmu.ifi.researchime.contentabstraction.model.config.RIMEContentAbstractionConfig;

public class ConfigDownloader {

    private static final String TAG = "ConfigDownloader";
    private final Context context;
    private final String serverBaseUrl;

    public ConfigDownloader(Context context, String serverBaseUrl) {
        this.context = context;
        this.serverBaseUrl = serverBaseUrl;
    }

    public RIMEContentAbstractionConfig downloadConfig(){
        Gson gson = new Gson();
        JsonObject jsonObject = RestClient.get(context).getContentAbstractionConfig();
        return gson.fromJson(jsonObject, RIMEContentAbstractionConfig.class);
    }

    public void downloadListFile(LogicalList logicalList) throws IOException {
        OkHttpClient client = new OkHttpClient();
        client.setReadTimeout(60, TimeUnit.SECONDS);
        Request request = new Request.Builder().url(serverBaseUrl+"/api/contentabstraction/"+(logicalList instanceof LogicalWordList ? "logicalwordlist" : "logicalcategorylist")+"/"+logicalList.getLogicallistId()).build();
        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            throw new IOException("Failed to download file: " + response);
        }
        FileOutputStream fos = context.openFileOutput(logicalList.getLocalFilename(), Context.MODE_PRIVATE);
        fos.write(response.body().bytes());
        fos.close();
        LogHelper.i(TAG, "downloaded logical list "+logicalList.getLogicallistId());

        File file = new File(context.getFilesDir(), logicalList.getLocalFilename());
        if (file.exists() && file.isFile() && file.length() > 0) {
            logicalList.setDownloaded(true);
            logicalList.update();
        }
        else {
            throw new IOException("downloading and saving "+logicalList.getLocalFilename()+" failed. The targetFile does not exist, is no file or is empty.");
        }
    }

}
