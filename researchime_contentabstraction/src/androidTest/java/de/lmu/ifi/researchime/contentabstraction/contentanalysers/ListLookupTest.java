package de.lmu.ifi.researchime.contentabstraction.contentanalysers;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import androidx.test.rule.ServiceTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.SmallTest;

import com.raizlabs.android.dbflow.config.FlowConfig;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.language.Select;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.lmu.ifi.researchime.contentabstraction.test.Word2Category;
import de.lmu.ifi.researchime.contentabstraction.test.Word2Category_Table;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class ListLookupTest {

        @Rule
        public final ServiceTestRule mServiceRule = new ServiceTestRule();

        @BeforeClass
        public static void init(){
            //SugarContext.init(InstrumentationRegistry.getTargetContext());
            FlowManager.init(new FlowConfig.Builder(InstrumentationRegistry.getTargetContext()).build());
        }

        @AfterClass
        public static void cleanup(){
            //SugarContext.terminate();
            FlowManager.destroy();
        }

        @Test
        public void sqliteTest(){
            List<String> searchWords = Arrays.asList("Das Studium der Medieninformatik ist eine Variante des Informatik Studiums Mit Mathematik und den Grundlagen der Informatik bilden die klassischen Elemente eines Informatik-Studiums einen wichtigen Bestandteil Medieninformatik ist anwendungsorientierter als Informatik damit ist gemeint dass die Entwicklung von Anwendungen stärker im Vordergrund steht " +
                    "Der Studiengang gibt einen Überblick über Multimedia-Technologien z.B. die Kodierung Verarbeitung und Verbreitung audiovisueller Inhalte Dabei bindet das Studium immer wieder Inhalte anderer Disziplinen mit ein Es geht weniger um die Anwendung existierender Software zur Medienbearbeitung sondern viel mehr um das Verständnis der Prinzipien dahinter und die Fähigkeit Multimedia-Software selbst zu entwickeln " +
                    "Das Studium der Medieninformatik ist ein stark technisch-theoretisch orientiertes Studium Kernpunkt des Studiums ist nicht die Erstellung von multimedialen Inhalten Videos Webseiten Die nachfolgende Grafik zeigt die Bestandeteile aus welchen sich der Studiengang Medieninformatik zusammensetzt".split(" "));

            System.out.println("------- creating category list ----------");
            importWordCategoryListToSqlite(InstrumentationRegistry.getTargetContext(), "wordcategories/intelliLIWC.dat");

            System.out.println("------- reading from category list ----------");
            Long startTs = System.currentTimeMillis();
            Map<String,String> foundWords = new HashMap<>();
            for (String word: searchWords){
                Word2Category foundWord = new Select().from(Word2Category.class).where(Word2Category_Table.word.is(word)).querySingle();
                if (foundWord != null){
                    foundWords.put(foundWord.getWord(), foundWord.getCategory());
                } else {
                    foundWords.put(word, "---");
                }
            }
            Long timeTook = System.currentTimeMillis() - startTs;

            System.out.println("------- the results ----------");
            System.out.println("words: "+searchWords.size());
            System.out.println("took: "+timeTook+"ms");
            for(Map.Entry<String,String> aResult : foundWords.entrySet()){
                System.out.println(aResult.getKey()+": "+aResult.getValue());
            }

        }

    private void importWordCategoryListToSqlite(Context context, String wordlistFilename){
        Long startTime = System.currentTimeMillis();

        try {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(context.getAssets().open(wordlistFilename)));
            String line;

            while ((line = br.readLine()) != null) {
                String[] lineAsArray;
                if((lineAsArray = line.split("\t")).length == 2){
                    String[] words = lineAsArray[0].split(",");
                    for (String word : words){
                        new Word2Category(word, lineAsArray[1]).save();
                    }
                } else {
                  //  LogHelper.w(TAG, "did not import line "+line);
                }
            }
            br.close();
        }
        catch (IOException e) {
            //You'll need to add proper error handling here
            e.printStackTrace();
        }
        System.out.println("wordlist "+wordlistFilename+" import took "+(System.currentTimeMillis()-startTime)+"ms");

    }

}
