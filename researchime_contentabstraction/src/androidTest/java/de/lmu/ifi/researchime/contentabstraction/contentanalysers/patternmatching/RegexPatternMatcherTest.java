package de.lmu.ifi.researchime.contentabstraction.contentanalysers.patternmatching;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.SmallTest;

import com.raizlabs.android.dbflow.config.FlowConfig;
import com.raizlabs.android.dbflow.config.FlowManager;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import de.lmu.ifi.researchime.contentabstraction.model.rawcontent.ContentUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class RegexPatternMatcherTest {

    private static final String EMOJI_REGEX = "[\uD83C-\uDBFF\uDC00-\uDFFF]+";

    @BeforeClass
    public static void init(){
        FlowManager.init(new FlowConfig.Builder(InstrumentationRegistry.getTargetContext()).build());
    }

    @Test
    public void shouldDetectEmojis(){
        String emojiString = "\uD83C\uDF83\uD83C\uDF84\uD83C\uDFDC\uD83C\uDF0D\uD83C\uDFDE\uD83C\uDFDC\uD83C\uDFDD\uD83C\uDFDA\uD83D\uDC36\uD83D\uDC2F";
        RegexPatternMatcher regexPatternMatcher = new RegexPatternMatcher(EMOJI_REGEX);
        ContentUnit contentUnit = new ContentUnit(emojiString
        );

        List<String> results = regexPatternMatcher.match(contentUnit);
        assertThat(results.size(),is(1));
        assertThat(results.get(0),is(emojiString));
    }

    @Test
    public void shouldDetectEmojisAfterWord(){
        RegexPatternMatcher regexPatternMatcher = new RegexPatternMatcher(EMOJI_REGEX);
        ContentUnit contentUnit = new ContentUnit("Hallo\uD83C\uDF83");

        List<String> results = regexPatternMatcher.match(contentUnit);
        assertThat(results.size(),is(1));
        assertThat(results.get(0),is("\uD83C\uDF83"));
    }

}
