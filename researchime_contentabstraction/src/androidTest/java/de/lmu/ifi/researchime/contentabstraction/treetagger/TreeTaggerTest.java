package de.lmu.ifi.researchime.contentabstraction.treetagger;

import android.content.Context;
import android.content.res.AssetManager;
import android.support.test.InstrumentationRegistry;
import androidx.test.rule.ServiceTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.SmallTest;

import org.annolab.tt4j.DefaultExecutableResolver;
import org.annolab.tt4j.TokenHandler;
import org.annolab.tt4j.TreeTaggerException;
import org.annolab.tt4j.TreeTaggerWrapper;
import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import de.lmu.ifi.researchime.base.logging.LogHelper;

import static java.util.Arrays.asList;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class TreeTaggerTest {

    @Rule
    public final ServiceTestRule mServiceRule = new ServiceTestRule();

    @BeforeClass
    public static void init() throws IOException {
        Context context = InstrumentationRegistry.getTargetContext();

        copyAsset(context, "treetagger-32");

//        copyFile(context, "treetagger/parameterfiles/german.par", "/german.par");
//        copyFile(context, "treetagger/executables/tree-tagger-ARM32-3.2/bin/tree-tagger", "tree-tagger");
//        copyFile(context, "treetagger/executables/tree-tagger-ARM32-3.2/bin/train-tree-tagger", "train-tree-tagger");
//        copyFile(context, "treetagger/executables/tree-tagger-ARM32-3.2/bin/tree-tagger-flush", "tree-tagger-flush");
//        copyFile(context, "treetagger/executables/tree-tagger-ARM32-3.2/bin/separate-punctuation", "separate-punctuation");

        ShellExecuter shell = new ShellExecuter();
//        String filePath = new File(context.getFilesDir().getPath() + "/treetagger/executables/tree-tagger-ARM32-3.2/bin/tree-tagger").getAbsolutePath();
//        shell.execute(new String[]{"system/bin/chmod", "777", filePath});
//        String filePath2 = new File(context.getFilesDir().getPath() + "/treetagger/parameterfiles/german.par").getAbsolutePath();
//        shell.execute(new String[]{"system/bin/chmod", "777", filePath2});

        String filePath2 = new File(context.getFilesDir().getPath() + "/treetagger-32/bin/tree-tagger").getAbsolutePath();
        shell.execute(new String[]{"system/bin/chmod", "755", filePath2});

//        String filePath2 = new File(context.getFilesDir(), "treetagger/executables").getAbsolutePath();
//        shell.execute(new String[]{"system/bin/chmod","755",filePath2});

    }

    /**
     * Copy the asset at the specified path to this app's data directory. If the
     * asset is a directory, its contents are also copied.
     *
     * @param path
     * Path to asset, relative to app's assets directory.
     */
    public static void copyAsset(Context context, String path) {
        AssetManager manager = context.getAssets();

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
            File dir = new File(context.getFilesDir(), path);
            dir.mkdirs();

            // Recurse on the contents.
            for (String entry : contents) {
                copyAsset(context, path + "/" + entry);
            }
        } catch (IOException e) {
            copyFileAsset(context, path);
        }
    }


    /**
     * Copy the asset file specified by path to app's data directory. Assumes
     * parent directories have already been created.
     *
     * @param path
     * Path to asset, relative to app's assets directory.
     */
    private static void copyFileAsset(Context context, String path) {
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




    @AfterClass
    public static void cleanup(){

    }

    @Test
    public void runTreeTagger() throws IOException, TreeTaggerException {

        final Context context = InstrumentationRegistry.getTargetContext();

        TreeTaggerWrapper tt = new TreeTaggerWrapper<String>();
        try {
            tt.setModel(context.getFilesDir().getPath() + "/treetagger-32/lib/german.par:UTF-8");
            tt.setHandler(new TokenHandler<String>() {
                public void token(String token, String pos, String lemma) {
                    System.out.println(token+"\t"+pos+"\t"+lemma);
                }
            });
            ((DefaultExecutableResolver)tt.getExecutableProvider()).setAdditionalPaths(asList(new String[]{context.getFilesDir().getPath() + "/treetagger-32/bin"}));

            tt.process(asList(new String[] {"Hallo", "gehst", "arbeitete", "male", "."}));
        }
        finally {
            tt.destroy();
        }
    }

}
