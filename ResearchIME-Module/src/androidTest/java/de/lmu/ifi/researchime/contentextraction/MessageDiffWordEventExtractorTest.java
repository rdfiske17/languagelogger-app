package de.lmu.ifi.researchime.contentextraction;

import android.content.Context;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.raizlabs.android.dbflow.config.FlowManager;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import de.lmu.ifi.researchime.contentabstraction.model.InputContent;
import de.lmu.ifi.researchime.contentextraction.model.event.Event;
import de.lmu.ifi.researchime.contentabstraction.model.rawcontent.ContentChangeEvent;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(AndroidJUnit4.class)
public class MessageDiffWordEventExtractorTest {

    @BeforeClass
    public static void init(){
        Context appContext = InstrumentationRegistry.getTargetContext();
        FlowManager.init(appContext);
    }


    @Test
    public void addFourWordsToExistingSentence() {
        // create (artifical) key input events, similar as Android does it when typing
        // For each change (a typed character, a removed character, a added word (in case of auto completion)) one event has to be added.
        // The event's content always is the content in the textfield after the change (meaning, for the first event we do not know how the textfield content was before)
        List<Event> events = new ArrayList<>();
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo ",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo w",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wi",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wie",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wie ",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wie g",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wie ge",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wie geh",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wie geht",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wie geht ",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wie geht e",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wie geht es",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wie geht es ",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wie geht es d",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wie geht es di",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wie geht es dir",null));

        // The code that actually executes the word splitting - always the same, nothing has to be changed here
        InputContent inputContent = new InputContent();
        MessageDiffWordEventExtractor messageDiffWordEventExtractor = new MessageDiffWordEventExtractor();
        List<ContentChangeEvent> highLevelEvents = messageDiffWordEventExtractor.extractHigherLevelContentChangeEvents(inputContent,events);

        // assertions: check whether the result of the above-called code is as expected
        assertThat(highLevelEvents.size(),is(4)); // assert that the amount of high level events generated is 4
        for (ContentChangeEvent contentChangeEvent : highLevelEvents) {
            assertThat(contentChangeEvent.isWordAddedEvent(), is(true)); // check that each generated high level event is of typed "ADDED"
        }

        // check that the content of the high-level event is correct
        assertThat((highLevelEvents.get(0)).getAddedWord().getAsString(),is("wie"));
        assertThat((highLevelEvents.get(1)).getAddedWord().getAsString(),is("geht"));
        assertThat((highLevelEvents.get(2)).getAddedWord().getAsString(),is("es"));
        assertThat((highLevelEvents.get(3)).getAddedWord().getAsString(),is("dir"));
    }

    @Test
    public void changeOneWord(){

        List<Event> events = new ArrayList<>();
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo Thomas Müller",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo Thoma Müller",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo Thom Müller",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo Tom Müller",null));

        InputContent inputContent = new InputContent();
        MessageDiffWordEventExtractor messageDiffWordEventExtractor = new MessageDiffWordEventExtractor();
        List<ContentChangeEvent> highLevelEvents = messageDiffWordEventExtractor.extractHigherLevelContentChangeEvents(inputContent, events);

        assertThat(highLevelEvents.size(),is(1));
        assertThat(highLevelEvents.get(0).isWordChangedEvent(), is(true));
        assertThat((highLevelEvents.get(0)).getContentUnitBefore().getAsString(),is("Thomas"));
        assertThat((highLevelEvents.get(0)).getContentUnitAfter().getAsString(),is("Tom"));

    }

    @Test
    public void addFourWordsToExistingSentenceWithCorrection() {

        List<Event> events = new ArrayList<>();
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo ",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo w",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wi",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wie",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wie ",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wie g",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wie ge",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wie geh",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wie gehz",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wie gehzz",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wie gehz",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wie geh",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wie geht ",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wie geht e",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wie geht es",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wie geht es ",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wie geht es d",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wie geht es di",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wie geht es dir",null));

        InputContent inputContent = new InputContent();
        MessageDiffWordEventExtractor messageDiffWordEventExtractor = new MessageDiffWordEventExtractor();
        List<ContentChangeEvent> highLevelEvents = messageDiffWordEventExtractor.extractHigherLevelContentChangeEvents(inputContent, events);

        assertThat(highLevelEvents.size(),is(4));
        for (ContentChangeEvent contentChangeEvent : highLevelEvents) {
            assertThat(contentChangeEvent.isWordAddedEvent(), is(true));
        }
        assertThat((highLevelEvents.get(0)).getAddedWord().getAsString(),is("wie"));
        assertThat((highLevelEvents.get(1)).getAddedWord().getAsString(),is("geht"));
        assertThat((highLevelEvents.get(2)).getAddedWord().getAsString(),is("es"));
        assertThat((highLevelEvents.get(3)).getAddedWord().getAsString(),is("dir"));
    }

    @Test
    public void writeNewSentence() {

        List<Event> events = new ArrayList<>();
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("H",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Ha",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hal",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hall",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo ",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo w",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wi",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wie",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wie ",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wie g",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wie ge",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wie geh",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wie geht",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wie geht ",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wie geht e",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wie geht es",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wie geht es ",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wie geht es d",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wie geht es di",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wie geht es dir",null));

        InputContent inputContent = new InputContent();
        MessageDiffWordEventExtractor messageDiffWordEventExtractor = new MessageDiffWordEventExtractor();
        List<ContentChangeEvent> highLevelEvents = messageDiffWordEventExtractor.extractHigherLevelContentChangeEvents(inputContent, events);

        assertThat(highLevelEvents.size(),is(5));
        for (ContentChangeEvent contentChangeEvent : highLevelEvents) {
            assertThat(contentChangeEvent.isWordAddedEvent(), is(true));
        }
        assertThat((highLevelEvents.get(0)).getAddedWord().getAsString(),is("Hallo"));
        assertThat((highLevelEvents.get(1)).getAddedWord().getAsString(),is("wie"));
        assertThat((highLevelEvents.get(2)).getAddedWord().getAsString(),is("geht"));
        assertThat((highLevelEvents.get(3)).getAddedWord().getAsString(),is("es"));
        assertThat((highLevelEvents.get(4)).getAddedWord().getAsString(),is("dir"));
    }

    @Test
    public void addRemoveEdit() {

        List<Event> events = new ArrayList<>();
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo ",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo w",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wi",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wie",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wie ",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wie g",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wie ge",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wie geh",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wie geht",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wie geht ",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wie geht e",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wie geht es",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wie geht es ",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wie geht es",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wie geht e",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wie geht ",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wie geht",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wie gehts",null));

        InputContent inputContent = new InputContent();
        MessageDiffWordEventExtractor messageDiffWordEventExtractor = new MessageDiffWordEventExtractor();
        List<ContentChangeEvent> highLevelEvents = messageDiffWordEventExtractor.extractHigherLevelContentChangeEvents(inputContent, events);

        assertThat(highLevelEvents.size(),is(5));

        assertThat(highLevelEvents.get(0).isWordAddedEvent(), is(true));
        assertThat((highLevelEvents.get(0)).getAddedWord().getAsString(),is("wie"));
        assertThat(highLevelEvents.get(1).isWordAddedEvent(), is(true));
        assertThat((highLevelEvents.get(1)).getAddedWord().getAsString(),is("geht"));
        assertThat(highLevelEvents.get(2).isWordAddedEvent(), is(true));
        assertThat((highLevelEvents.get(2)).getAddedWord().getAsString(),is("es"));
        assertThat(highLevelEvents.get(3).isWordRemovedEvent(), is(true));
        assertThat((highLevelEvents.get(3)).getRemovedWord().getAsString(),is("es"));
        assertThat(highLevelEvents.get(4).isWordChangedEvent(), is(true));
        assertThat((highLevelEvents.get(4)).getContentUnitBefore().getAsString(),is("geht"));
        assertThat((highLevelEvents.get(4)).getContentUnitAfter().getAsString(),is("gehts"));
    }

    @Test
    public void remove2Words() {

        List<Event> events = new ArrayList<>();
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wie geht e dir",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wie geht  dir",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wie geht dir",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wie geh dir",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wie ge dir",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wie g dir",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wie  dir",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wie dir",null));

        InputContent inputContent = new InputContent();
        MessageDiffWordEventExtractor messageDiffWordEventExtractor = new MessageDiffWordEventExtractor();
        List<ContentChangeEvent> highLevelEvents = messageDiffWordEventExtractor.extractHigherLevelContentChangeEvents(inputContent, events);

        assertThat(highLevelEvents.size(),is(2));

        assertThat(highLevelEvents.get(0).isWordRemovedEvent(), is(true));
        assertThat((highLevelEvents.get(0)).getRemovedWord().getAsString(),is("e")); // the system cannot know that e was previously es
        assertThat(highLevelEvents.get(1).isWordRemovedEvent(), is(true));
        assertThat((highLevelEvents.get(1)).getRemovedWord().getAsString(),is("geht"));
    }

    @Test
    public void AddWordsInBetween() {

        List<Event> events = new ArrayList<>();
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo  wie geht es dir so",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo T wie geht es dir so",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo Th wie geht es dir so",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo Tho wie geht es dir so",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo Thom wie geht es dir so",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo Thoma wie geht es dir so",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo Thomas wie geht es dir so",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo Thomas  wie geht es dir so",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo Thomas M wie geht es dir so",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo Thomas Mü wie geht es dir so",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo Thomas Mül wie geht es dir so",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo Thomas Müll wie geht es dir so",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo Thomas Mülle wie geht es dir so",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo Thomas Müller wie geht es dir so",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo Thomas Müller wie geht es dir  so",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo Thomas Müller wie geht es dir h so",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo Thomas Müller wie geht es dir he so",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo Thomas Müller wie geht es dir heu so",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo Thomas Müller wie geht es dir heut so",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo Thomas Müller wie geht es dir heute so",null));

        InputContent inputContent = new InputContent();
        MessageDiffWordEventExtractor messageDiffWordEventExtractor = new MessageDiffWordEventExtractor();
        List<ContentChangeEvent> highLevelEvents = messageDiffWordEventExtractor.extractHigherLevelContentChangeEvents(inputContent, events);

        assertThat(highLevelEvents.size(),is(3));
        for (ContentChangeEvent contentChangeEvent : highLevelEvents) {
            assertThat(contentChangeEvent.isWordAddedEvent(), is(true));
        }
        assertThat((highLevelEvents.get(0)).getAddedWord().getAsString(),is("Thomas"));
        assertThat((highLevelEvents.get(1)).getAddedWord().getAsString(),is("Müller"));
        assertThat((highLevelEvents.get(2)).getAddedWord().getAsString(),is("heute"));
    }

    @Test
    public void split1() {

        List<Event> events = new ArrayList<>();
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent(" It is summer.",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("It is sum mer.",null));

        InputContent inputContent = new InputContent();
        MessageDiffWordEventExtractor messageDiffWordEventExtractor = new MessageDiffWordEventExtractor();
        List<ContentChangeEvent> highLevelEvents = messageDiffWordEventExtractor.extractHigherLevelContentChangeEvents(inputContent, events);

        assertThat(highLevelEvents.size(),is(1));
        assertThat(highLevelEvents.get(0).isWordSplittedEvent(), is(true));
        assertThat((highLevelEvents.get(0)).getContentUnitBefore().getAsString(),is("summer"));
        assertThat((highLevelEvents.get(0)).getContentUnitAfter().getAsString(),is("sum mer"));
    }

    @Test
    public void split2() {

        List<Event> events = new ArrayList<>();
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("W1W2W3W4",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("W1 W2W3W4",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("W1 W2 W3W4",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("W1 W2 W3 W4",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("W1 W2 W3  W4",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("W1 W2 W3 & W4",null));

        InputContent inputContent = new InputContent();
        MessageDiffWordEventExtractor messageDiffWordEventExtractor = new MessageDiffWordEventExtractor();
        List<ContentChangeEvent> highLevelEvents = messageDiffWordEventExtractor.extractHigherLevelContentChangeEvents(inputContent, events);

        assertThat(highLevelEvents.size(),is(2));
        assertThat(highLevelEvents.get(0).isWordAddedEvent(), is(true));
        assertThat((highLevelEvents.get(0)).getAddedWord().getAsString(),is("&"));
        assertThat(highLevelEvents.get(1).isWordSplittedEvent(), is(true));
        assertThat((highLevelEvents.get(1)).getContentUnitBefore().getAsString(),is("W1W2W3W4"));
        assertThat((highLevelEvents.get(1)).getContentUnitAfter().getAsString(),is("W1 W2 W3 W4"));

    }

    @Test
    public void join1() {

        List<Event> events = new ArrayList<>();
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("The words are W1 W2 W3",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("The words are W1W2 W3",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("The words are W1W2W3",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("The words are W1W2W3.",null));

        InputContent inputContent = new InputContent();
        MessageDiffWordEventExtractor messageDiffWordEventExtractor = new MessageDiffWordEventExtractor();
        List<ContentChangeEvent> highLevelEvents = messageDiffWordEventExtractor.extractHigherLevelContentChangeEvents(inputContent, events);

        assertThat(highLevelEvents.size(),is(2));
        assertThat(highLevelEvents.get(0).isWordAddedEvent(), is(true));
        assertThat((highLevelEvents.get(0)).getAddedWord().getAsString(),is("."));
        assertThat(highLevelEvents.get(1).isWordJoinedEvent(), is(true));
        assertThat((highLevelEvents.get(1)).getContentUnitBefore().getAsString(),is("W1 W2 W3"));
        assertThat((highLevelEvents.get(1)).getContentUnitAfter().getAsString(),is("W1W2W3"));

    }



    @Test
    public void splitWords() {
        // TODO does not work yet  - fixed

        List<Event> events = new ArrayList<>();
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Wort1Wort2",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Wort1 Wort2",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Wort1 Wort2 h",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Wort1 Wort2 ha",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Wort1 Wort2 hal",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Wort1 Wort2 hall",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Wort1 Wort2 hallo",null));

        InputContent inputContent = new InputContent();
        MessageDiffWordEventExtractor messageDiffWordEventExtractor = new MessageDiffWordEventExtractor();
        List<ContentChangeEvent> highLevelEvents = messageDiffWordEventExtractor.extractHigherLevelContentChangeEvents(inputContent, events);

        assertThat(highLevelEvents.size(),is(2));
        assertThat(highLevelEvents.get(0).isWordAddedEvent(), is(true));
        assertThat((highLevelEvents.get(0)).getAddedWord().getAsString(),is("hallo"));
        assertThat(highLevelEvents.get(1).isWordSplittedEvent(), is(true));
        assertThat((highLevelEvents.get(1)).getContentUnitBefore().getAsString(),is("Wort1Wort2"));
        assertThat((highLevelEvents.get(1)).getContentUnitAfter().getAsString(),is("Wort1 Wort2"));


    }

    @Test
    public void AddWordsInBetweenLateSpace() {
        // TODO does not work yet -> fixed

        List<Event> events = new ArrayList<>();
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wie geht es dir so",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo Twie geht es dir so",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo Thwie geht es dir so",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo Thowie geht es dir so",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo Thomwie geht es dir so",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo Thomawie geht es dir so",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo Thomaswie geht es dir so",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo Thomas wie geht es dir so",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo Thomas Mwie geht es dir so",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo Thomas Müwie geht es dir so",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo Thomas Mülwie geht es dir so",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo Thomas Müllwie geht es dir so",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo Thomas Müllewie geht es dir so",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo Thomas Müllerwie geht es dir so",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo Thomas Müller wie geht es dir so",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo Thomas Müller wie geht es dir  so",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo Thomas Müller wie geht es dir h so",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo Thomas Müller wie geht es dir he so",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo Thomas Müller wie geht es dir heu so",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo Thomas Müller wie geht es dir heut so",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo Thomas Müller wie geht es dir heute so",null));

        InputContent inputContent = new InputContent();
        MessageDiffWordEventExtractor messageDiffWordEventExtractor = new MessageDiffWordEventExtractor();
        List<ContentChangeEvent> highLevelEvents = messageDiffWordEventExtractor.extractHigherLevelContentChangeEvents(inputContent, events);

        assertThat(highLevelEvents.size(),is(3));
        for (ContentChangeEvent contentChangeEvent : highLevelEvents) {
            assertThat(contentChangeEvent.isWordAddedEvent(), is(true));
        }
        assertThat((highLevelEvents.get(0)).getAddedWord().getAsString(),is("heute"));
        assertThat((highLevelEvents.get(1)).getAddedWord().getAsString(),is("Thomas"));
        assertThat((highLevelEvents.get(2)).getAddedWord().getAsString(),is("Müller"));

    }

    @Test
    public void AddWordsInBetweenLateSpace2() {

        List<Event> events = new ArrayList<>();
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wie geht es dir so",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo Twie geht es dir so",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo Thwie geht es dir so",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo Thowie geht es dir so",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo Thomwie geht es dir so",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo Thomawie geht es dir so",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo Thomaswie geht es dir so",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo Thomaswiea geht es dir so",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo Thomaswieab geht es dir so",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo Thomaswieabc geht es dir so",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo Thomas wieabc geht es dir so",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo Thomas wie abc geht es dir so",null));


        InputContent inputContent = new InputContent();
        MessageDiffWordEventExtractor messageDiffWordEventExtractor = new MessageDiffWordEventExtractor();
        List<ContentChangeEvent> highLevelEvents = messageDiffWordEventExtractor.extractHigherLevelContentChangeEvents(inputContent, events);

        assertThat(highLevelEvents.size(),is(2));
        for (ContentChangeEvent contentChangeEvent : highLevelEvents) {
            assertThat(contentChangeEvent.isWordAddedEvent(), is(true));
        }
        assertThat((highLevelEvents.get(0)).getAddedWord().getAsString(),is("Thomas"));
        assertThat((highLevelEvents.get(1)).getAddedWord().getAsString(),is("abc"));
    }

    @Test
    public void addWordsWithQuestionMark(){
        List<Event> events = new ArrayList<>();
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo ",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo w",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wi",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wie",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wie ",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wie g",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wie ge",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wie geh",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wie geht",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wie geht ",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wie geht e",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wie geht es",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wie geht es ",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wie geht es d",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wie geht es di",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wie geht es dir",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wie geht es dir?",null));

        InputContent inputContent = new InputContent();
        MessageDiffWordEventExtractor messageDiffWordEventExtractor = new MessageDiffWordEventExtractor();
        List<ContentChangeEvent> highLevelEvents = messageDiffWordEventExtractor.extractHigherLevelContentChangeEvents(inputContent, events);

        assertThat(highLevelEvents.size(),is(5));
        assertThat(highLevelEvents.get(0).isWordAddedEvent(), is(true));
        assertThat((highLevelEvents.get(0)).getAddedWord().getAsString(),is("wie"));
        assertThat(highLevelEvents.get(1).isWordAddedEvent(), is(true));
        assertThat((highLevelEvents.get(1)).getAddedWord().getAsString(),is("geht"));
        assertThat(highLevelEvents.get(2).isWordAddedEvent(), is(true));
        assertThat((highLevelEvents.get(2)).getAddedWord().getAsString(),is("es"));
        assertThat(highLevelEvents.get(3).isWordAddedEvent(), is(true));
        assertThat((highLevelEvents.get(3)).getAddedWord().getAsString(),is("dir"));
        assertThat(highLevelEvents.get(4).isWordAddedEvent(), is(true));
        assertThat((highLevelEvents.get(4)).getAddedWord().getAsString(),is("?"));
    }

    @Test
    public void addWordsWithComma(){
        List<Event> events = new ArrayList<>();
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("H",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Ha",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hal",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hall",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo,",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo, ",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo, w",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo, wi",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo, wie",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo, wie ",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo, wie g",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo, wie ge",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo, wie geh",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo, wie geht",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo, wie geht ",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo, wie geht e",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo, wie geht es",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo, wie geht es ",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo, wie geht es d",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo, wie geht es di",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo, wie geht es dir",null));

        InputContent inputContent = new InputContent();
        MessageDiffWordEventExtractor messageDiffWordEventExtractor = new MessageDiffWordEventExtractor();
        List<ContentChangeEvent> highLevelEvents = messageDiffWordEventExtractor.extractHigherLevelContentChangeEvents(inputContent, events);

        assertThat(highLevelEvents.size(),is(6));
        assertThat(highLevelEvents.get(0).isWordAddedEvent(), is(true));
        assertThat((highLevelEvents.get(0)).getAddedWord().getAsString(),is("Hallo"));
        assertThat(highLevelEvents.get(1).isWordAddedEvent(), is(true));
        assertThat((highLevelEvents.get(1)).getAddedWord().getAsString(),is(","));
        assertThat(highLevelEvents.get(2).isWordAddedEvent(), is(true));
        assertThat((highLevelEvents.get(2)).getAddedWord().getAsString(),is("wie"));
        assertThat(highLevelEvents.get(3).isWordAddedEvent(), is(true));
        assertThat((highLevelEvents.get(3)).getAddedWord().getAsString(),is("geht"));
        assertThat(highLevelEvents.get(4).isWordAddedEvent(), is(true));
        assertThat((highLevelEvents.get(4)).getAddedWord().getAsString(),is("es"));
        assertThat(highLevelEvents.get(5).isWordAddedEvent(), is(true));
        assertThat((highLevelEvents.get(5)).getAddedWord().getAsString(),is("dir"));
    }

    @Test
    public void multiplePuncationMarksShouldCountAsOneWord(){
        List<Event> events = new ArrayList<>();
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo ",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo w",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wi",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wie",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wie ",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wie g",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wie ge",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wie geh",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wie geht",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wie geht ",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wie geht e",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wie geht es",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wie geht es ",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wie geht es d",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wie geht es di",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wie geht es dir",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wie geht es dir?",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wie geht es dir??",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo wie geht es dir???",null));

        InputContent inputContent = new InputContent();
        MessageDiffWordEventExtractor messageDiffWordEventExtractor = new MessageDiffWordEventExtractor();
        List<ContentChangeEvent> highLevelEvents = messageDiffWordEventExtractor.extractHigherLevelContentChangeEvents(inputContent, events);

        assertThat(highLevelEvents.size(),is(5));
        assertThat(highLevelEvents.get(0).isWordAddedEvent(), is(true));
        assertThat((highLevelEvents.get(0)).getAddedWord().getAsString(),is("wie"));
        assertThat(highLevelEvents.get(1).isWordAddedEvent(), is(true));
        assertThat((highLevelEvents.get(1)).getAddedWord().getAsString(),is("geht"));
        assertThat(highLevelEvents.get(2).isWordAddedEvent(), is(true));
        assertThat((highLevelEvents.get(2)).getAddedWord().getAsString(),is("es"));
        assertThat(highLevelEvents.get(3).isWordAddedEvent(), is(true));
        assertThat((highLevelEvents.get(3)).getAddedWord().getAsString(),is("dir"));
        assertThat(highLevelEvents.get(4).isWordAddedEvent(), is(true));
        assertThat((highLevelEvents.get(4)).getAddedWord().getAsString(),is("???"));
    }

    @Test
    public void shouldSplitString(){
        MessageDiffWordEventExtractor messageDiffWordEventExtractor = new MessageDiffWordEventExtractor();
        List<String> words = messageDiffWordEventExtractor.splitSentenceIntoWords("Hallöchen, mein Name  ist Flo! Wie geht's dir??");

        assertThat(words,is(notNullValue()));
        assertThat(words.size(), is(11));
        assertThat(words.get(0),is("Hallöchen"));
        assertThat(words.get(1),is(","));
        assertThat(words.get(2),is("mein"));
        assertThat(words.get(3),is("Name"));
        assertThat(words.get(4),is("ist"));
        assertThat(words.get(5),is("Flo"));
        assertThat(words.get(6),is("!"));
        assertThat(words.get(7),is("Wie"));
        assertThat(words.get(8),is("geht's"));
        assertThat(words.get(9),is("dir"));
        assertThat(words.get(10),is("??"));
    }

    @Test
    public void regardSmileysAsWord(){
        List<Event> events = new ArrayList<>();
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("H",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Ha",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hal",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hall",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo ",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo :",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo :)",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo :) ",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo :) W",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo :) Wa",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo :) Was",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo :) Was ",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo :) Was g",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo :) Was ge",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo :) Was geh",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo :) Was geht",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo :) Was geht^",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo :) Was geht^^",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo :) Was geht^^ ",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo :) Was geht^^ ;",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo :) Was geht^^ ;-",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo :) Was geht^^ ;-)",null));

        InputContent inputContent = new InputContent();
        MessageDiffWordEventExtractor messageDiffWordEventExtractor = new MessageDiffWordEventExtractor();
        List<ContentChangeEvent> highLevelEvents = messageDiffWordEventExtractor.extractHigherLevelContentChangeEvents(inputContent, events);

        assertThat(highLevelEvents.size(),is(6));
        assertThat(highLevelEvents.get(0).isWordAddedEvent(), is(true));
        assertThat((highLevelEvents.get(0)).getAddedWord().getAsString(),is("Hallo"));
        assertThat(highLevelEvents.get(1).isWordAddedEvent(), is(true));
        assertThat((highLevelEvents.get(1)).getAddedWord().getAsString(),is(":)"));
        assertThat(highLevelEvents.get(2).isWordAddedEvent(), is(true));
        assertThat((highLevelEvents.get(2)).getAddedWord().getAsString(),is("Was"));
        assertThat(highLevelEvents.get(3).isWordAddedEvent(), is(true));
        assertThat((highLevelEvents.get(3)).getAddedWord().getAsString(),is("geht"));
        assertThat(highLevelEvents.get(4).isWordAddedEvent(), is(true));
        assertThat((highLevelEvents.get(4)).getAddedWord().getAsString(),is("^^"));
        assertThat(highLevelEvents.get(5).isWordAddedEvent(), is(true));
        assertThat((highLevelEvents.get(5)).getAddedWord().getAsString(),is(";-)"));
    }

    @Test
    public void shouldNotGetStuckOnFirstWordInCaseOfTippfehlerCorrection() {

        List<Event> events = new ArrayList<>();
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("H",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Ha ",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hal",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hall",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Halli",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Halli ",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Halli",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hall",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo ",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo d",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo du",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo du ",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo du D",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo du De",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo du Dep",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo du Depp",null));

        InputContent inputContent = new InputContent();
        MessageDiffWordEventExtractor messageDiffWordEventExtractor = new MessageDiffWordEventExtractor();
        List<ContentChangeEvent> highLevelEvents = messageDiffWordEventExtractor.extractHigherLevelContentChangeEvents(inputContent, events);

        assertThat(highLevelEvents.size(),is(4));

        assertThat(highLevelEvents.get(0).isWordAddedEvent(), is(true));
        assertThat((highLevelEvents.get(0)).getAddedWord().getAsString(),is("Halli"));
        assertThat(highLevelEvents.get(1).isWordChangedEvent(), is(true));
        assertThat((highLevelEvents.get(1)).getContentUnitBefore().getAsString(),is("Halli"));
        assertThat((highLevelEvents.get(1)).getContentUnitAfter().getAsString(),is("Hallo"));
        assertThat(highLevelEvents.get(2).isWordAddedEvent(), is(true));
        assertThat((highLevelEvents.get(2)).getAddedWord().getAsString(),is("du"));
        assertThat(highLevelEvents.get(2).isWordAddedEvent(), is(true));
        assertThat((highLevelEvents.get(3)).getAddedWord().getAsString(),is("Depp"));
    }

    @Test
    public void multiEmojisOnlyWord(){
        List<Event> events = new ArrayList<>();
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("H",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Ha ",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hal",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hall",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Halli",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Halli ",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Halli \uD83C\uDF83",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Halli \uD83C\uDF83\uD83C\uDF84",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Halli \uD83C\uDF83\uD83C\uDF84\uD83C\uDFDC",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Halli \uD83C\uDF83\uD83C\uDF84\uD83C\uDFDC\uD83C\uDF0D",null));

        InputContent inputContent = new InputContent();
        MessageDiffWordEventExtractor messageDiffWordEventExtractor = new MessageDiffWordEventExtractor();
        List<ContentChangeEvent> highLevelEvents = messageDiffWordEventExtractor.extractHigherLevelContentChangeEvents(inputContent, events);

        assertThat(highLevelEvents.size(),is(2));
        assertThat(highLevelEvents.get(0).isWordAddedEvent(), is(true));
        assertThat((highLevelEvents.get(0)).getAddedWord().getAsString(),is("Halli"));
        assertThat(highLevelEvents.get(1).isWordAddedEvent(), is(true));
        assertThat((highLevelEvents.get(1)).getAddedWord().getAsString(),is("\uD83C\uDF83\uD83C\uDF84\uD83C\uDFDC\uD83C\uDF0D"));
    }

    @Test
    public void singleEmojiOnlyWord(){
        List<Event> events = new ArrayList<>();
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("H",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Ha ",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hal",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hall",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Halli",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Halli ",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Halli \uD83C\uDF83",null));

        InputContent inputContent = new InputContent();
        MessageDiffWordEventExtractor messageDiffWordEventExtractor = new MessageDiffWordEventExtractor();
        List<ContentChangeEvent> highLevelEvents = messageDiffWordEventExtractor.extractHigherLevelContentChangeEvents(inputContent, events);

        assertThat(highLevelEvents.size(),is(2));
        assertThat(highLevelEvents.get(0).isWordAddedEvent(), is(true));
        assertThat((highLevelEvents.get(0)).getAddedWord().getAsString(),is("Halli"));
        assertThat(highLevelEvents.get(1).isWordAddedEvent(), is(true));
        assertThat((highLevelEvents.get(1)).getAddedWord().getAsString(),is("\uD83C\uDF83"));
    }

    @Test
    public void singleEmojiOnlyWordTailingSpace() {
        List<Event> events = new ArrayList<>();
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("H", null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Ha ", null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hal", null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hall", null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Halli", null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Halli ", null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Halli \uD83C\uDF83", null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Halli \uD83C\uDF83 ", null));

        InputContent inputContent = new InputContent();
        MessageDiffWordEventExtractor messageDiffWordEventExtractor = new MessageDiffWordEventExtractor();
        List<ContentChangeEvent> highLevelEvents = messageDiffWordEventExtractor.extractHigherLevelContentChangeEvents(inputContent, events);

        assertThat(highLevelEvents.size(), is(2));
        assertThat(highLevelEvents.get(0).isWordAddedEvent(), is(true));
        assertThat((highLevelEvents.get(0)).getAddedWord().getAsString(), is("Halli"));
        assertThat(highLevelEvents.get(1).isWordAddedEvent(), is(true));
        assertThat((highLevelEvents.get(1)).getAddedWord().getAsString(), is("\uD83C\uDF83"));
    }


    @Test
    public void capitalizeWord(){
        List<Event> events = new ArrayList<>();
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("This is munich.",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("This is munic.",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("This is muni.",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("This is mun.",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("This is mu.",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("This is m.",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("This is .",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("This is M.",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("This is MU.",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("This is MUN.",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("This is MUNI.",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("This is MUNIC.",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("This is MUNICH.",null));

        InputContent inputContent = new InputContent();
        MessageDiffWordEventExtractor messageDiffWordEventExtractor = new MessageDiffWordEventExtractor();
        List<ContentChangeEvent> highLevelEvents = messageDiffWordEventExtractor.extractHigherLevelContentChangeEvents(inputContent, events);

        assertThat(highLevelEvents.size(),is(2));
        assertThat(highLevelEvents.get(0).isWordRemovedEvent(), is(true));
        assertThat((highLevelEvents.get(0)).getRemovedWord().getAsString(),is("munich"));
        assertThat(highLevelEvents.get(1).isWordAddedEvent(), is(true));
        assertThat((highLevelEvents.get(1)).getAddedWord().getAsString(),is("MUNICH"));
    }



    @Test
    public void changeWordAndEditPunctuation(){
        List<Event> events = new ArrayList<>();
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("H",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Ho",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("How",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("How ",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("How i",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("How is",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("How is ",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("How is t",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("How is th",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("How is the",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("How is the ",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("How is the w",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("How is the wh",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("How is the whe",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("How is the whet",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("How is the wheth",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("How is the whethe",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("How is the whether",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("How is the whether ",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("How is the whether t",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("How is the whether to",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("How is the whether tod",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("How is the whether toda",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("How is the whether today",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("How is the whether today.",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("How is the wether today.",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("How is the weather today.",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("How is the eather today.",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("How is the Weather today.",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("How is the Weather today",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("How is the Weather today?",null));

        InputContent inputContent = new InputContent();
        MessageDiffWordEventExtractor messageDiffWordEventExtractor = new MessageDiffWordEventExtractor();
        List<ContentChangeEvent> highLevelEvents = messageDiffWordEventExtractor.extractHigherLevelContentChangeEvents(inputContent, events);

        assertThat(highLevelEvents.size(),is(9));
        assertThat(highLevelEvents.get(0).isWordAddedEvent(), is(true));
        assertThat((highLevelEvents.get(0)).getAddedWord().getAsString(),is("How"));
        assertThat(highLevelEvents.get(1).isWordAddedEvent(), is(true));
        assertThat((highLevelEvents.get(1)).getAddedWord().getAsString(),is("is"));
        assertThat(highLevelEvents.get(2).isWordAddedEvent(), is(true));
        assertThat((highLevelEvents.get(2)).getAddedWord().getAsString(),is("the"));
        assertThat(highLevelEvents.get(3).isWordAddedEvent(), is(true));
        assertThat((highLevelEvents.get(3)).getAddedWord().getAsString(),is("whether"));
        assertThat(highLevelEvents.get(4).isWordAddedEvent(), is(true));
        assertThat((highLevelEvents.get(4)).getAddedWord().getAsString(),is("today"));
        assertThat(highLevelEvents.get(5).isWordAddedEvent(), is(true));
        assertThat((highLevelEvents.get(5)).getAddedWord().getAsString(),is("."));
        assertThat(highLevelEvents.get(6).isWordChangedEvent(), is(true));
        assertThat((highLevelEvents.get(6)).getContentUnitBefore().getAsString(),is("whether"));
        assertThat((highLevelEvents.get(6)).getContentUnitAfter().getAsString(),is("Weather"));
        assertThat(highLevelEvents.get(7).isWordRemovedEvent(), is(true));
        assertThat((highLevelEvents.get(7)).getRemovedWord().getAsString(),is("."));
        assertThat(highLevelEvents.get(8).isWordAddedEvent(), is(true));
        assertThat((highLevelEvents.get(8)).getAddedWord().getAsString(),is("?"));
    }

    @Test
    public void numberAsAWord(){
        List<Event> events = new ArrayList<>();
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("T",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Th",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("The",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("The ",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("The n",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("The nu",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("The num",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("The numb",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("The numbe",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("The number",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("The number ",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("The number i",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("The number is",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("The number is ",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("The number is 6",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("The number is 65",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("The number is 653",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("The number is 653.",null));

        InputContent inputContent = new InputContent();
        MessageDiffWordEventExtractor messageDiffWordEventExtractor = new MessageDiffWordEventExtractor();
        List<ContentChangeEvent> highLevelEvents = messageDiffWordEventExtractor.extractHigherLevelContentChangeEvents(inputContent, events);

        assertThat(highLevelEvents.size(),is(5));
        for (ContentChangeEvent contentChangeEvent : highLevelEvents) {
            assertThat(contentChangeEvent.isWordAddedEvent(), is(true));
        }
        assertThat((highLevelEvents.get(0)).getAddedWord().getAsString(),is("The"));
        assertThat((highLevelEvents.get(1)).getAddedWord().getAsString(),is("number"));
        assertThat((highLevelEvents.get(2)).getAddedWord().getAsString(),is("is"));
        assertThat((highLevelEvents.get(3)).getAddedWord().getAsString(),is("653"));
        assertThat((highLevelEvents.get(4)).getAddedWord().getAsString(),is("."));
    }

    @Test
    public void AddNumAddPunctuationChangeNum(){
        List<Event> events = new ArrayList<>();
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("The number is 653t4.",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("The number is 653t45.",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("The number is 65345.",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("The number is 653745.",null));

        InputContent inputContent = new InputContent();
        MessageDiffWordEventExtractor messageDiffWordEventExtractor = new MessageDiffWordEventExtractor();
        List<ContentChangeEvent> highLevelEvents = messageDiffWordEventExtractor.extractHigherLevelContentChangeEvents(inputContent, events);

        assertThat(highLevelEvents.size(),is(1));
        assertThat(highLevelEvents.get(0).isWordChangedEvent(), is(true));
        assertThat((highLevelEvents.get(0)).getContentUnitBefore().getAsString(),is("653t4"));
        assertThat((highLevelEvents.get(0)).getContentUnitAfter().getAsString(),is("653745"));
    }

    @Test
    public void commaAndSeparatedNumbers(){
        List<Event> events = new ArrayList<>();
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("T",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Th",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("The",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("The ",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("The n",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("The nu",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("The num",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("The numb",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("The numbe",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("The number",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("The numbers",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("The numbers ",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("The numbers a",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("The numbers ar",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("The numbers are",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("The numbers are ",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("The numbers are 1",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("The numbers are 13",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("The numbers are 13,",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("The numbers are 13, ",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("The numbers are 13, 2",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("The numbers are 13, 27",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("The numbers are 13, 273",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("The numbers are 13, 273,",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("The numbers are 13, 273, ",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("The numbers are 13, 273, 3",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("The numbers are 13, 273, 3 ",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("The numbers are 13, 273, 3 a",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("The numbers are 13, 273, 3 an",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("The numbers are 13, 273, 3 and",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("The numbers are 13, 273, 3 and ",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("The numbers are 13, 273, 3 and 4",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("The numbers are 13, 273, 3 and 43",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("The numbers are 13, 273, 3 and 433",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("The numbers are 13, 273, 3 and 4334",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("The numbers are 13, 273, 3 and 4334.",null));

        InputContent inputContent = new InputContent();
        MessageDiffWordEventExtractor messageDiffWordEventExtractor = new MessageDiffWordEventExtractor();
        List<ContentChangeEvent> highLevelEvents = messageDiffWordEventExtractor.extractHigherLevelContentChangeEvents(inputContent, events);

        assertThat(highLevelEvents.size(),is(11));
        for (ContentChangeEvent contentChangeEvent : highLevelEvents) {
            assertThat(contentChangeEvent.isWordAddedEvent(), is(true));
        }
        assertThat((highLevelEvents.get(0)).getAddedWord().getAsString(),is("The"));
        assertThat((highLevelEvents.get(1)).getAddedWord().getAsString(),is("numbers"));
        assertThat((highLevelEvents.get(2)).getAddedWord().getAsString(),is("are"));
        assertThat((highLevelEvents.get(3)).getAddedWord().getAsString(),is("13"));
        assertThat((highLevelEvents.get(4)).getAddedWord().getAsString(),is(","));
        assertThat((highLevelEvents.get(5)).getAddedWord().getAsString(),is("273"));
        assertThat((highLevelEvents.get(6)).getAddedWord().getAsString(),is(","));
        assertThat((highLevelEvents.get(7)).getAddedWord().getAsString(),is("3"));
        assertThat((highLevelEvents.get(8)).getAddedWord().getAsString(),is("and"));
        assertThat((highLevelEvents.get(9)).getAddedWord().getAsString(),is("4334"));
        assertThat((highLevelEvents.get(10)).getAddedWord().getAsString(),is("."));

    }

    //Change regex - not correct 'Munich-Germany' should be counted as one word
    @Test
    public void combineMultipleWords(){

        List<Event> events = new ArrayList<>();
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("It is sum mer in Munich Germany",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("It is sum mer in MunichGermany",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("It is sum mer in Munich-Germany",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("It is summer in Munich-Germany",null));

        InputContent inputContent = new InputContent();
        MessageDiffWordEventExtractor messageDiffWordEventExtractor = new MessageDiffWordEventExtractor();
        List<ContentChangeEvent> highLevelEvents = messageDiffWordEventExtractor.extractHigherLevelContentChangeEvents(inputContent, events);

        assertThat(highLevelEvents.size(),is(3));
        assertThat(highLevelEvents.get(0).isWordAddedEvent(), is(true));
        assertThat((highLevelEvents.get(0)).getAddedWord().getAsString(),is("-"));
        assertThat(highLevelEvents.get(1).isWordJoinedEvent(), is(true));
        assertThat((highLevelEvents.get(1)).getContentUnitBefore().getAsString(),is("Munich Germany"));
        assertThat((highLevelEvents.get(1)).getContentUnitAfter().getAsString(),is("MunichGermany"));
        assertThat(highLevelEvents.get(2).isWordJoinedEvent(), is(true));
        assertThat((highLevelEvents.get(2)).getContentUnitBefore().getAsString(),is("sum mer"));
        assertThat((highLevelEvents.get(2)).getContentUnitAfter().getAsString(),is("summer"));
    }

    @Test
    public void splitAndCombineNumbers(){
        List<Event> events = new ArrayList<>();
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("The numbers are 65, 27, 33 and 8.",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("The numbers are 65, 27, 3 and 8.",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("The numbers are 65, 273, 3 and 8.",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("The numbers are 65, 23, 3 and 8.",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("The numbers are 657, 23, 3 and 8.",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("The numbers are 657, 23, 3 an 8.",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("The numbers are 657, 23, 3 a 8.",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("The numbers are 657, 23, 3  8.",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("The numbers are 657, 23, 38 8.",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("The numbers are 657, 23, 38.",null));

        InputContent inputContent = new InputContent();
        MessageDiffWordEventExtractor messageDiffWordEventExtractor = new MessageDiffWordEventExtractor();
        List<ContentChangeEvent> highLevelEvents = messageDiffWordEventExtractor.extractHigherLevelContentChangeEvents(inputContent, events);

        assertThat(highLevelEvents.size(),is(6));
        assertThat(highLevelEvents.get(0).isWordChangedEvent(), is(true));
        assertThat((highLevelEvents.get(0)).getContentUnitBefore().getAsString(),is("33"));
        assertThat((highLevelEvents.get(0)).getContentUnitAfter().getAsString(),is("3"));
        assertThat(highLevelEvents.get(1).isWordChangedEvent(), is(true));
        assertThat((highLevelEvents.get(1)).getContentUnitBefore().getAsString(),is("27"));
        assertThat((highLevelEvents.get(1)).getContentUnitAfter().getAsString(),is("23"));
        assertThat(highLevelEvents.get(2).isWordChangedEvent(), is(true));
        assertThat((highLevelEvents.get(2)).getContentUnitBefore().getAsString(),is("65"));
        assertThat((highLevelEvents.get(2)).getContentUnitAfter().getAsString(),is("657"));
        assertThat(highLevelEvents.get(3).isWordRemovedEvent(), is(true));
        assertThat((highLevelEvents.get(3)).getRemovedWord().getAsString(),is("and"));
        assertThat(highLevelEvents.get(4).isWordChangedEvent(), is(true));
        assertThat((highLevelEvents.get(4)).getContentUnitBefore().getAsString(),is("3"));
        assertThat((highLevelEvents.get(4)).getContentUnitAfter().getAsString(),is("38"));
        assertThat(highLevelEvents.get(5).isWordRemovedEvent(), is(true));
        assertThat((highLevelEvents.get(5)).getRemovedWord().getAsString(),is("8"));

    }

    @Test
    public void operatorAsWords(){
        List<Event> events = new ArrayList<>();
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("T",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Th",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("The",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("The ",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("The a",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("The am",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("The amo",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("The amou",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("The amoun",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("The amount",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("The amount ",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("The amount i",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("The amount is",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("The amount is ",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("The amount is 6",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("The amount is 65",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("The amount is 65 ",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("The amount is 65 +",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("The amount is 65 + ",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("The amount is 65 + 2",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("The amount is 65 + 27",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("The amount is 65 + 27.",null));

        InputContent inputContent = new InputContent();
        MessageDiffWordEventExtractor messageDiffWordEventExtractor = new MessageDiffWordEventExtractor();
        List<ContentChangeEvent> highLevelEvents = messageDiffWordEventExtractor.extractHigherLevelContentChangeEvents(inputContent, events);

        assertThat(highLevelEvents.size(),is(7));
        for (ContentChangeEvent contentChangeEvent : highLevelEvents) {
            assertThat(contentChangeEvent.isWordAddedEvent(), is(true));
        }
        assertThat((highLevelEvents.get(0)).getAddedWord().getAsString(),is("The"));
        assertThat((highLevelEvents.get(1)).getAddedWord().getAsString(),is("amount"));
        assertThat((highLevelEvents.get(2)).getAddedWord().getAsString(),is("is"));
        assertThat((highLevelEvents.get(3)).getAddedWord().getAsString(),is("65"));
        assertThat((highLevelEvents.get(4)).getAddedWord().getAsString(),is("+"));
        assertThat((highLevelEvents.get(5)).getAddedWord().getAsString(),is("27"));
        assertThat((highLevelEvents.get(6)).getAddedWord().getAsString(),is("."));

    }

    //Does not work yet
    @Test
    public void editOperators(){
        List<Event> events = new ArrayList<>();
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("The amount is 65 + 27.",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("The amount is 65  27.",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("The amount is 65 * 27.",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("The amount is 65 * 27 .",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("The amount is 65 * 27 -.",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("The amount is 65 * 27 - .",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("The amount is 65 * 27 - 3.",null));

        InputContent inputContent = new InputContent();
        MessageDiffWordEventExtractor messageDiffWordEventExtractor = new MessageDiffWordEventExtractor();
        List<ContentChangeEvent> highLevelEvents = messageDiffWordEventExtractor.extractHigherLevelContentChangeEvents(inputContent, events);

        assertThat(highLevelEvents.size(),is(4));
        assertThat(highLevelEvents.get(0).isWordRemovedEvent(), is(true));
        assertThat((highLevelEvents.get(0)).getRemovedWord().getAsString(),is("+"));
        assertThat(highLevelEvents.get(1).isWordAddedEvent(), is(true));
        assertThat((highLevelEvents.get(1)).getAddedWord().getAsString(),is("*"));
        assertThat(highLevelEvents.get(2).isWordAddedEvent(), is(true));
        assertThat((highLevelEvents.get(2)).getAddedWord().getAsString(),is("-."));
        assertThat(highLevelEvents.get(3).isWordAddedEvent(), is(true));
        assertThat((highLevelEvents.get(3)).getAddedWord().getAsString(),is("3"));

    }

    //Does not work yet
    @Test
    public void operatorsBetweenNumbers(){
        List<Event> events = new ArrayList<>();
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Numbers are 123 345",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Numbers are 123 34*5",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Numbers are 123 3-4*5",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Numbers are 12/3 3-4*5",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Numbers are 1+2/3 3-4*5",null));


        InputContent inputContent = new InputContent();
        MessageDiffWordEventExtractor messageDiffWordEventExtractor = new MessageDiffWordEventExtractor();
        List<ContentChangeEvent> highLevelEvents = messageDiffWordEventExtractor.extractHigherLevelContentChangeEvents(inputContent, events);

        assertThat(highLevelEvents.size(),is(2));
        assertThat(highLevelEvents.get(0).isWordChangedEvent(), is(true));
        assertThat((highLevelEvents.get(0)).getContentUnitBefore().getAsString(),is("123"));
        assertThat((highLevelEvents.get(0)).getContentUnitAfter().getAsString(),is("1+2/3"));
        assertThat(highLevelEvents.get(1).isWordChangedEvent(), is(true));
        assertThat((highLevelEvents.get(1)).getContentUnitBefore().getAsString(),is("345"));
        assertThat((highLevelEvents.get(1)).getContentUnitAfter().getAsString(),is("3-4*5"));
    }

    //Does not work yet
    @Test
    public void addCommaAndAbbreviateWordInBetween(){
        List<Event> events = new ArrayList<>();
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo Professor George",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo Professo George",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo Profess George",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo Profes George",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo Profe George",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo Prof George",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo Prof. George",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo, Prof. George",null));


        InputContent inputContent = new InputContent();
        MessageDiffWordEventExtractor messageDiffWordEventExtractor = new MessageDiffWordEventExtractor();
        List<ContentChangeEvent> highLevelEvents = messageDiffWordEventExtractor.extractHigherLevelContentChangeEvents(inputContent, events);

        assertThat(highLevelEvents.size(),is(3));
        assertThat(highLevelEvents.get(0).isWordChangedEvent(), is(true));
        assertThat((highLevelEvents.get(0)).getContentUnitBefore().getAsString(),is("Professor"));
        assertThat((highLevelEvents.get(0)).getContentUnitAfter().getAsString(),is("Prof"));
        assertThat(highLevelEvents.get(1).isWordAddedEvent(), is(true));
        assertThat((highLevelEvents.get(1)).getAddedWord().getAsString(),is("."));
        assertThat(highLevelEvents.get(2).isWordAddedEvent(), is(true));
        assertThat((highLevelEvents.get(2)).getAddedWord().getAsString(),is(","));
    }

    @Test
    public void newSentenceWithQuestionMarkAtEnd() {

        List<Event> events = new ArrayList<>();
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("H",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Ha",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hal",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hall",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo,",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo, ",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo, w",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo, wi",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo, wie",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo, wie ",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo, wie g",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo, wie ge",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo, wie geh",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo, wie geht",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo, wie geht ",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo, wie geht e",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo, wie geht es",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo, wie geht es ",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo, wie geht es d",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo, wie geht es di",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo, wie geht es dir",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo, wie geht es dir?",null));

        InputContent inputContent = new InputContent();
        MessageDiffWordEventExtractor messageDiffWordEventExtractor = new MessageDiffWordEventExtractor();
        List<ContentChangeEvent> highLevelEvents = messageDiffWordEventExtractor.extractHigherLevelContentChangeEvents(inputContent, events);

        assertThat(highLevelEvents.size(),is(7));
        for (ContentChangeEvent contentChangeEvent : highLevelEvents) {
            assertThat(contentChangeEvent.isWordAddedEvent(), is(true));
        }
        assertThat((highLevelEvents.get(0)).getAddedWord().getAsString(),is("Hallo"));
        assertThat((highLevelEvents.get(1)).getAddedWord().getAsString(),is(","));
        assertThat((highLevelEvents.get(2)).getAddedWord().getAsString(),is("wie"));
        assertThat((highLevelEvents.get(3)).getAddedWord().getAsString(),is("geht"));
        assertThat((highLevelEvents.get(4)).getAddedWord().getAsString(),is("es"));
        assertThat((highLevelEvents.get(5)).getAddedWord().getAsString(),is("dir"));
        assertThat((highLevelEvents.get(6)).getAddedWord().getAsString(),is("?"));
    }

    @Test
    public void removeAddEmoji() {

        List<Event> events = new ArrayList<>();
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("H",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Ha",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hal",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hall",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo,",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo, ",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo, \uDF84",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo, \uDF84 ",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo, \uDF84 w",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo, \uDF84 wi",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo, \uDF84 wie",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo, \uDF84 wie ",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo, \uDF84 wie g",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo, \uDF84 wie ge",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo, \uDF84 wie geh",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo, \uDF84 wie geht",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo, \uDF84 wie geht ",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo, \uDF84 wie geht e",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo, \uDF84 wie geht es",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo, \uDF84 wie geht es ",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo, \uDF84 wie geht es d",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo, \uDF84 wie geht es di",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo, \uDF84 wie geht es dir",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo, \uDF84 wie geht es dir\uD83C",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo, wie geht es dir\uD83C",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo, wie geht es dir\uD83C ",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo, wie geht es dir\uD83C \uDF84",null));


        InputContent inputContent = new InputContent();
        MessageDiffWordEventExtractor messageDiffWordEventExtractor = new MessageDiffWordEventExtractor();
        List<ContentChangeEvent> highLevelEvents = messageDiffWordEventExtractor.extractHigherLevelContentChangeEvents(inputContent, events);

        assertThat(highLevelEvents.size(),is(2));
        assertThat(highLevelEvents.get(0).isWordRemovedEvent(), is(true));
        assertThat((highLevelEvents.get(0)).getRemovedWord().getAsString(),is("\u263A"));
        assertThat(highLevelEvents.get(1).isWordAddedEvent(), is(true));
        assertThat((highLevelEvents.get(1)).getAddedWord().getAsString(),is("\u263A"));
    }

    @Test
    public void newSentenceWithEmojiAndSmileyAsWord() {

        List<Event> events = new ArrayList<>();
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("H",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Ha",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hal",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hall",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo,",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo, ",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo, \uDF84",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo, \uDF84 ",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo, \uDF84 w",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo, \uDF84 wi",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo, \uDF84 wie",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo, \uDF84 wie ",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo, \uDF84 wie g",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo, \uDF84 wie ge",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo, \uDF84 wie geh",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo, \uDF84 wie geht",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo, \uDF84 wie geht ",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo, \uDF84 wie geht e",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo, \uDF84 wie geht es",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo, \uDF84 wie geht es ",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo, \uDF84 wie geht es d",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo, \uDF84 wie geht es di",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo, \uDF84 wie geht es dir",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo, \uDF84 wie geht es dir?",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo, \uDF84 wie geht es dir? ",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo, \uDF84 wie geht es dir? :",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo, \uDF84 wie geht es dir? :)",null));

        InputContent inputContent = new InputContent();
        MessageDiffWordEventExtractor messageDiffWordEventExtractor = new MessageDiffWordEventExtractor();
        List<ContentChangeEvent> highLevelEvents = messageDiffWordEventExtractor.extractHigherLevelContentChangeEvents(inputContent, events);

        assertThat(highLevelEvents.size(),is(9));
        for (ContentChangeEvent contentChangeEvent : highLevelEvents) {
            assertThat(contentChangeEvent.isWordAddedEvent(), is(true));
        }
        assertThat((highLevelEvents.get(0)).getAddedWord().getAsString(),is("Hallo"));
        assertThat((highLevelEvents.get(1)).getAddedWord().getAsString(),is(","));
        assertThat((highLevelEvents.get(2)).getAddedWord().getAsString(),is("\uDF84"));
        assertThat((highLevelEvents.get(3)).getAddedWord().getAsString(),is("wie"));
        assertThat((highLevelEvents.get(4)).getAddedWord().getAsString(),is("geht"));
        assertThat((highLevelEvents.get(5)).getAddedWord().getAsString(),is("es"));
        assertThat((highLevelEvents.get(6)).getAddedWord().getAsString(),is("dir"));
        assertThat((highLevelEvents.get(7)).getAddedWord().getAsString(),is("?"));
        assertThat((highLevelEvents.get(8)).getAddedWord().getAsString(),is(":)"));
    }

    //Does not work
    @Test
    public void emailAsWord() {

        List<Event> events = new ArrayList<>();
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("M",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("My",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("My ",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("My e",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("My em",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("My ema",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("My emai",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("My email",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("My email ",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("My email i",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("My email is",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("My email is ",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("My email is t",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("My email is te",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("My email is tes",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("My email is test",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("My email is test@",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("My email is test@g",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("My email is test@gm",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("My email is test@gma",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("My email is test@gmai",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("My email is test@gmail",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("My email is test@gmail.",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("My email is test@gmail.c",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("My email is test@gmail.co",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("My email is test@gmail.com",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("My email is test@gmail.com.",null));

        InputContent inputContent = new InputContent();
        MessageDiffWordEventExtractor messageDiffWordEventExtractor = new MessageDiffWordEventExtractor();
        List<ContentChangeEvent> highLevelEvents = messageDiffWordEventExtractor.extractHigherLevelContentChangeEvents(inputContent, events);

        assertThat(highLevelEvents.size(),is(5));
        for (ContentChangeEvent contentChangeEvent : highLevelEvents) {
            assertThat(contentChangeEvent.isWordAddedEvent(), is(true));
        }
        assertThat((highLevelEvents.get(0)).getAddedWord().getAsString(),is("My"));
        assertThat((highLevelEvents.get(1)).getAddedWord().getAsString(),is("email"));
        assertThat((highLevelEvents.get(2)).getAddedWord().getAsString(),is("is"));
        assertThat((highLevelEvents.get(3)).getAddedWord().getAsString(),is("test@gmail.com"));
        assertThat((highLevelEvents.get(4)).getAddedWord().getAsString(),is("."));
    }

    //Does not work
    @Test
    public void changeEmail() {

        List<Event> events = new ArrayList<>();
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("My email is test@gmail.com.",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("My email is test_@gmail.com.",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("My email is test_1@gmail.com.",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("My email is test_12@gmail.com.",null));

        InputContent inputContent = new InputContent();
        MessageDiffWordEventExtractor messageDiffWordEventExtractor = new MessageDiffWordEventExtractor();
        List<ContentChangeEvent> highLevelEvents = messageDiffWordEventExtractor.extractHigherLevelContentChangeEvents(inputContent, events);

        assertThat(highLevelEvents.size(),is(1));
        assertThat(highLevelEvents.get(0).isWordChangedEvent(), is(true));
        assertThat((highLevelEvents.get(0)).getContentUnitBefore().getAsString(),is("test@gmail.com"));
        assertThat((highLevelEvents.get(0)).getContentUnitAfter().getAsString(),is("test_12@gmail.com"));
    }

    //Does not work yet
    @Test
    public void abbreviate2() {

        List<Event> events = new ArrayList<>();
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("They live in the United States.",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("They live in the United State.",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("They live in the United Stat.",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("They live in the United Sta.",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("They live in the United St.",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("They live in the United S.",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("They live in the Unite S.",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("They live in the Unit S.",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("They live in the Uni S.",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("They live in the Un S.",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("They live in the U S.",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("They live in the US.",null));


        InputContent inputContent = new InputContent();
        MessageDiffWordEventExtractor messageDiffWordEventExtractor = new MessageDiffWordEventExtractor();
        List<ContentChangeEvent> highLevelEvents = messageDiffWordEventExtractor.extractHigherLevelContentChangeEvents(inputContent, events);

        assertThat(highLevelEvents.size(),is(1));
        assertThat(highLevelEvents.get(0).isWordChangedEvent(), is(true));
        assertThat((highLevelEvents.get(0)).getContentUnitBefore().getAsString(),is("United States"));
        assertThat((highLevelEvents.get(0)).getContentUnitAfter().getAsString(),is("US"));
    }

    //Does not work yet
    @Test
    public void abbreviate3() {

        List<Event> events = new ArrayList<>();
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("United States",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("United State",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("United Stat",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("United Sta",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("United St",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("United S",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Unite S",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Unit S",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Uni S",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Un S",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("U S",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("U. S",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("U.S",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("U.S.",null));


        InputContent inputContent = new InputContent();
        MessageDiffWordEventExtractor messageDiffWordEventExtractor = new MessageDiffWordEventExtractor();
        List<ContentChangeEvent> highLevelEvents = messageDiffWordEventExtractor.extractHigherLevelContentChangeEvents(inputContent, events);

        assertThat(highLevelEvents.size(),is(1));
        assertThat(highLevelEvents.get(0).isWordChangedEvent(), is(true));
        assertThat((highLevelEvents.get(0)).getContentUnitBefore().getAsString(),is("United States"));
        assertThat((highLevelEvents.get(0)).getContentUnitAfter().getAsString(),is("U.S."));
    }


    /**
     * "Hallo du " -> "Hallo du dude"
     */
    @Test
    public void addWordsAfterExistingTextWithTailingSpace(){
        List<Event> events = new ArrayList<>();
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo du d",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo du du",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo du dud",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo du dude",null));
        events.add(new de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent("Hallo du dude ",null));

        InputContent inputContent = new InputContent();
        MessageDiffWordEventExtractor messageDiffWordEventExtractor = new MessageDiffWordEventExtractor();
        List<ContentChangeEvent> highLevelEvents = messageDiffWordEventExtractor.extractHigherLevelContentChangeEvents(inputContent, events);

        assertThat(highLevelEvents.size(),is(1));
        assertThat(highLevelEvents.get(0).isWordAddedEvent(), is(true));
        assertThat((highLevelEvents.get(0)).getAddedWord().getAsString(),is("dude"));
    }

}
