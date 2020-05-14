package de.lmu.ifi.researchime.contentabstraction.contentanalysers;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.SmallTest;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Set;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class WordFrequencyCounterJobServiceTest {

    @Test
    public void shouldLookForWordsInWhitelist(){
        WordFrequencyCounterJobService wordFrequencyCounterJobService = new WordFrequencyCounterJobService();
        Set<String> wordset = wordFrequencyCounterJobService.importWordWhitelist(InstrumentationRegistry.getTargetContext(), "wordlists/derewo-v-ww-bll-320000g-2012-12-31-1.0.txt");

        String[] someWords = new String[]{"Auto","Straße","Lastwagenfahrer","Zyrtolith","Beutejagd","adöhsfjpda","aäsdfiadsf","Bahn","Kassenzettel","Übung"};

        for(String aWord : someWords){
            System.out.println("whitelist does"+(wordset.contains(aWord) ? " ":" not ")+"contain "+aWord);
        }
    }

}
