package de.lmu.ifi.researchime.contentabstraction.contentanalysers;

import android.support.test.InstrumentationRegistry;
import androidx.test.rule.ServiceTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.SmallTest;

import com.raizlabs.android.dbflow.config.FlowConfig;
import com.raizlabs.android.dbflow.config.FlowManager;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.lmu.ifi.researchime.contentabstraction.model.ContentUnitEventType;
import de.lmu.ifi.researchime.contentabstraction.model.config.LogicalCategoryList;
import de.lmu.ifi.researchime.contentabstraction.model.rawcontent.ContentChangeEvent;
import de.lmu.ifi.researchime.contentabstraction.model.rawcontent.ContentUnit;

import static de.lmu.ifi.researchime.contentabstraction.treetagger.TreeTaggerTest.copyAsset;


@RunWith(AndroidJUnit4.class)
@SmallTest
public class WordCategorizerJobServiceTest {

    @Rule
    public final ServiceTestRule mServiceRule = new ServiceTestRule();

    private static LogicalCategoryList lcl;

    @BeforeClass
    public static void init(){
        //SugarContext.init(InstrumentationRegistry.getTargetContext());
        FlowManager.init(new FlowConfig.Builder(InstrumentationRegistry.getTargetContext()).build());

        // setup list config
        lcl = new LogicalCategoryList();
        lcl.setDownloaded(true);
        lcl.setLogicallistId(1L);
        lcl.setLogicallistName("liwc-de");
        lcl.setPreappyLemmaExtraction(false);

        // copy wordlist file
        copyAsset(InstrumentationRegistry.getTargetContext(), "liwc-de.rime");
    }

    @AfterClass
    public static void cleanup(){
        //SugarContext.terminate();
        FlowManager.destroy();
    }

    @Test
    public void shouldImportWordCategoryList() throws IOException {
       //TODO WordCategorizerJobService wordCategorizerJobService = new WordCategorizerJobService(lcl);
      // TODO  wordCategorizerJobService.importWordCategoryList(InstrumentationRegistry.getTargetContext(), "liwc-de.rime");
    }

    @Test
    public void shouldRunCategorySearch() throws IOException {
        List<ContentChangeEvent> events = new ArrayList<>();
        events.add(new ContentChangeEvent(ContentUnitEventType.ADDED,null,new ContentUnit("Hallo")));
        events.add(new ContentChangeEvent(ContentUnitEventType.ADDED,null,new ContentUnit("wie")));
        events.add(new ContentChangeEvent(ContentUnitEventType.ADDED,null,new ContentUnit("es")));
        events.add(new ContentChangeEvent(ContentUnitEventType.ADDED,null,new ContentUnit("dir")));
        events.add(new ContentChangeEvent(ContentUnitEventType.ADDED,null,new ContentUnit("heute")));


//      TODO  WordCategorizerJobService wordCategorizerJobService = new WordCategorizerJobService(lcl);
//        Map<String,String> word2Category = wordCategorizerJobService.importWordCategoryList(InstrumentationRegistry.getTargetContext(), "liwc-de.rime");
//        wordCategorizerJobService.createWordCategoryActions(word2Category, events);
    }

//    @Test
//    public void shouldTestService() throws TimeoutException {
//        // Create the service Intent.
//        Intent serviceIntent =
//                new Intent(InstrumentationRegistry.getTargetContext(),//ApplicationProvider.getApplicationContext(),
//                        WordCategorizerJobService.class);
//
//        // Data can be passed to the service via the Intent.
//       // serviceIntent.putExtra(WordCategorizerJobService.SEED_KEY, 42L);
//
//        // Bind the service and grab a reference to the binder.
//        IBinder binder = mServiceRule.bindService(serviceIntent);
//
//        // Get the reference to the service, or you can call
//        // public methods on the binder directly.
////        WordCategorizerJobService service =
////                ((WordCategorizerJobService.LocalBinder) binder).getService();
//
//        // Verify that the service is working correctly.
//       // assertThat(service.getRandomInt()).isAssignableTo(Integer.class);
//    }



}
