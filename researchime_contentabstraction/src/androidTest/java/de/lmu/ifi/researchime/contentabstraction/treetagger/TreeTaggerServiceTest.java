package de.lmu.ifi.researchime.contentabstraction.treetagger;

import android.content.Intent;
import android.os.IBinder;
import android.support.test.InstrumentationRegistry;
import androidx.test.rule.ServiceTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.SmallTest;

import org.annolab.tt4j.TreeTaggerException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class TreeTaggerServiceTest {

    @Rule
    public final ServiceTestRule mServiceRule = new ServiceTestRule();

    @Test
    public void shouldLemmatizeWords() throws TimeoutException, InterruptedException, IOException, TreeTaggerException {

        Intent serviceIntent = new Intent(InstrumentationRegistry.getTargetContext(), TreeTaggerService.class);


        // Bind the service and grab a reference to the binder.
        IBinder binder = mServiceRule.bindService(serviceIntent);

        final TreeTaggerService service = ((TreeTaggerService.LocalBinder) binder).getService();


        assertThat(service.lemmatizeWord("Hallo"), is("Hallo"));
        assertThat(service.lemmatizeWord("machst"), is("machen"));
        assertThat(service.lemmatizeWord("ging"), is("gehen"));
        assertThat(service.lemmatizeWord("."), is("."));

    }

}
