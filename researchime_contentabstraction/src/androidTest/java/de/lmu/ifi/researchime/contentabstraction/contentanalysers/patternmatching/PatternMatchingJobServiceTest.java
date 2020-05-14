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

import de.lmu.ifi.researchime.contentabstraction.model.ContentUnitEventType;
import de.lmu.ifi.researchime.contentabstraction.model.abstractedcontent.AbstractedAction;
import de.lmu.ifi.researchime.contentabstraction.model.abstractedcontent.AbstractedActionRawContent;
import de.lmu.ifi.researchime.contentabstraction.model.abstractedcontent.AbstractedWordAction;
import de.lmu.ifi.researchime.contentabstraction.model.config.PatternMatcherConfig;
import de.lmu.ifi.researchime.contentabstraction.model.rawcontent.ContentChangeEvent;
import de.lmu.ifi.researchime.contentabstraction.model.rawcontent.ContentUnit;

import static de.lmu.ifi.researchime.contentabstraction.contentanalysers.patternmatching.PatternMatcherJobService.EMOJI_OBFUSCATION_TAG;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class PatternMatchingJobServiceTest {

    private static final String EMOJI_REGEX = "[\uD83C-\uDBFF\uDC00-\uDFFF]+";

    @BeforeClass
    public static void init(){
        FlowManager.init(new FlowConfig.Builder(InstrumentationRegistry.getTargetContext()).build());
    }

    @Test
    public void shouldDetectOneEmojiSequenceRaw(){

        String emojiString = "\uD83C\uDF83\uD83C\uDF84\uD83C\uDFDC\uD83C\uDF0D\uD83C\uDFDE\uD83C\uDFDC\uD83C\uDFDD\uD83C\uDFDA\uD83D\uDC36\uD83D\uDC2F";

        ContentChangeEvent contentChangeEvent = new ContentChangeEvent(
                ContentUnitEventType.ADDED,
                null,
                new ContentUnit(emojiString)
        );

        PatternMatcherConfig patternMatcherConfig = new PatternMatcherConfig();
        patternMatcherConfig.setLogRawContent(true);
        patternMatcherConfig.setRegex(EMOJI_REGEX);

        PatternMatcherJobService patternMatcherJobService = new PatternMatcherJobService();
        List<AbstractedAction> abstractedActions = patternMatcherJobService.runPatternMatcher(
                contentChangeEvent,
                patternMatcherConfig
        );

        assertThat(abstractedActions, is(notNullValue()));
        assertThat(abstractedActions.size(), is(1));
        assertThat(abstractedActions.get(0).getContentUnitEventType(), is(ContentUnitEventType.ADDED));
        assertThat(abstractedActions.get(0) instanceof AbstractedActionRawContent, is(true));
        assertThat(((AbstractedActionRawContent) abstractedActions.get(0)).getRawContentAfter(), is(emojiString));
    }

    @Test
    public void shouldDetectOneEmojiSequenceObfuscated(){

        String emojiString = "\uD83C\uDF83\uD83C\uDF84\uD83C\uDFDC\uD83C\uDF0D\uD83C\uDFDE\uD83C\uDFDC\uD83C\uDFDD\uD83C\uDFDA\uD83D\uDC36\uD83D\uDC2F";

        ContentChangeEvent contentChangeEvent = new ContentChangeEvent(
                ContentUnitEventType.ADDED,
                null,
                new ContentUnit(emojiString)
        );

        PatternMatcherConfig patternMatcherConfig = new PatternMatcherConfig();
        patternMatcherConfig.setLogRawContent(false);
        patternMatcherConfig.setRegex(EMOJI_REGEX);

        PatternMatcherJobService patternMatcherJobService = new PatternMatcherJobService();
        List<AbstractedAction> abstractedActions = patternMatcherJobService.runPatternMatcher(
                contentChangeEvent,
                patternMatcherConfig
        );

        assertThat(abstractedActions, is(notNullValue()));
        assertThat(abstractedActions.size(), is(1));
        assertThat(abstractedActions.get(0).getContentUnitEventType(), is(ContentUnitEventType.ADDED));
        assertThat(abstractedActions.get(0) instanceof AbstractedWordAction, is(true));
        assertThat(((AbstractedWordAction) abstractedActions.get(0)).getCategoryAfter(), is(EMOJI_OBFUSCATION_TAG));
    }
}
