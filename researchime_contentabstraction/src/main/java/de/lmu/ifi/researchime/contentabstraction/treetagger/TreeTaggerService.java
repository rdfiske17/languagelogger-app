package de.lmu.ifi.researchime.contentabstraction.treetagger;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import org.annolab.tt4j.DefaultExecutableResolver;
import org.annolab.tt4j.TokenHandler;
import org.annolab.tt4j.TreeTaggerException;
import org.annolab.tt4j.TreeTaggerWrapper;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import de.lmu.ifi.researchime.base.logging.LogHelper;

import static java.util.Arrays.asList;

public class TreeTaggerService extends Service {

    private static final String TAG = "TreeTaggerService";

    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public TreeTaggerService getService() {
            // Return this instance of LocalService so clients can call public methods
            return TreeTaggerService.this;
        }
    }



    private final ShellExecuter shellExecutor = new ShellExecuter();
    private TreeTaggerWrapper tt;
    private Map<String,String> lemmatizeResults;
    private Object wordResultFetchMonitor;

    /**
     * copies the treetagger files from assets to files dir
     */
    private void setup(){
        // copy files
        copyAsset(getTreetaggerInstallFilesPath());
        // make the executable executable
        File treeTaggerExecutable = new File(getApplicationContext().getFilesDir().getPath() + "/"+getTreetaggerInstallFilesPath()+"/bin/tree-tagger");
        shellExecutor.execute(new String[]{"system/bin/chmod", "755", treeTaggerExecutable.getAbsolutePath()});
    }

    /**
     * Copy the asset at the specified path to this app's data directory. If the
     * asset is a directory, its contents are also copied.
     *
     * @param path
     * Path to asset, relative to app's assets directory.
     */
    private void copyAsset(String path) {
        AssetManager manager = getApplicationContext().getAssets();

        // If we have a directory, we make it and recurse. If a file, we copy its
        // contents.

        try {
            String[] contents = manager.list(path);

            // The documentation suggests that list throws an IOException, but doesn't
            // say under what conditions. It'd be nice if it did so when the path was
            // to a file. That doesn't appear to be the case. If the returned array is
            // null or has 0 length, we assume the path is to a file. This means empty
            // directories will get turned into files.
            if (contents == null || contents.length == 0)
                throw new IOException();

            // Make the directory.
            File dir = new File(getApplicationContext().getFilesDir(), path);
            dir.mkdirs();

            // Recurse on the contents.
            for (String entry : contents) {
                copyAsset(path + "/" + entry);
            }
        } catch (IOException e) {
            copyFileAsset(getApplicationContext(), path);
        }
    }

    /**
     * Copy the asset file specified by path to app's data directory. Assumes
     * parent directories have already been created.
     *
     * @param path
     * Path to asset, relative to app's assets directory.
     */
    private void copyFileAsset(Context context, String path) {
        File file = new File(context.getFilesDir(), path);
        try {
            InputStream in = context.getAssets().open(path);
            OutputStream out = new FileOutputStream(file);
            IOUtils.copy(in, out);
            out.close();
            in.close();
        } catch (IOException e) {
            LogHelper.e("TAG HERE", e.toString());
        }
    }

    private String getTreetaggerInstallFilesPath(){
        if(Build.SUPPORTED_64_BIT_ABIS.length > 0) {
            // is 64 Bit deviceBuild.SUPPORTED_64_BIT_ABIS
            return "treetagger-64";
        }
        else {
            // assume is 32 Bit device
            return "treetagger-32";
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
    // if not done so far, setup the executables
        if(!new File(getApplicationContext().getFilesDir(), getTreetaggerInstallFilesPath()).exists()){
            setup();
        }

        // launch the executable
        // from now on it is active in the background and consumes memory! Mind to stop TreeTaggerService when your done
        tt = new TreeTaggerWrapper<String>();

        try {
            tt.setModel(getApplicationContext().getFilesDir().getPath() + "/"+getTreetaggerInstallFilesPath()+"/lib/german.par:UTF-8");
        } catch (IOException e) {
            LogHelper.e(TAG,"could not set model for treetagger executable",e);
        }
        ((DefaultExecutableResolver)tt.getExecutableProvider()).setAdditionalPaths(asList(new String[]{getApplicationContext().getFilesDir().getPath() + "/"+getTreetaggerInstallFilesPath()+"/bin"}));

        lemmatizeResults = new HashMap<>();
        wordResultFetchMonitor = new Object();
        tt.setHandler(new TokenHandler() {
            @Override
            public void token(Object token, String pos, String lemma) {
                lemmatizeResults.put((String) token, lemma);
                try {
                    wordResultFetchMonitor.notifyAll();
                } catch(IllegalMonitorStateException e){
                    // thrown if notify is called before wait
                }
            }
        });

       super.onCreate();
    }

    public String lemmatizeWord(String word) throws IOException, TreeTaggerException {
        tt.process(asList(new String[]{word}));
        while(true){
            if (lemmatizeResults.containsKey(word)) {
                return lemmatizeResults.get(word);
            }
            try {
                wordResultFetchMonitor.wait(10000);
            } catch (InterruptedException e) {
                LogHelper.e(TAG,"wordResultFetchMonitor exceeded timeout. So there could no lemma be found for word "+word,e);
                return word;
            }
        }
    }

    @Override
    public void onDestroy() {
        if (tt != null) {
            tt.destroy();
        }
        super.onDestroy();
    }
}
