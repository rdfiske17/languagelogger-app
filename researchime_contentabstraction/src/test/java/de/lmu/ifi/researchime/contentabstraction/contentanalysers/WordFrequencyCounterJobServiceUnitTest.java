package de.lmu.ifi.researchime.contentabstraction.contentanalysers;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class WordFrequencyCounterJobServiceUnitTest {

    @Test
    public void shouldAddNormalWord(){
        WordFrequencyCounterJobService wordFrequencyCounterJobService = new WordFrequencyCounterJobService();

        Set<String> wordset = new HashSet<>();

        wordFrequencyCounterJobService.processLineArray(new String[]{"Auto","12","Nomen"},wordset);

        assertThat(wordset.size(),is(1));
        assertThat(wordset.contains("Auto"),is(true));
    }

    @Test
    public void shouldAddMultipleCommaSeparatedWords(){
        WordFrequencyCounterJobService wordFrequencyCounterJobService = new WordFrequencyCounterJobService();

        Set<String> wordset = new HashSet<>();

        wordFrequencyCounterJobService.processLineArray(new String[]{"der,die,das","0"},wordset);

        assertThat(wordset.size(),is(3));
        assertThat(wordset.contains("der"),is(true));
        assertThat(wordset.contains("die"),is(true));
        assertThat(wordset.contains("das"),is(true));
    }

    @Test
    public void shouldAddWordWithOptional(){
        WordFrequencyCounterJobService wordFrequencyCounterJobService = new WordFrequencyCounterJobService();

        Set<String> wordset = new HashSet<>();

        wordFrequencyCounterJobService.processLineArray(new String[]{"dein(e)","1"},wordset);

        assertThat(wordset.size(),is(2));
        assertThat(wordset.contains("dein"),is(true));
        assertThat(wordset.contains("deine"),is(true));
    }

    @Test
    public void shouldAddWordWithMultipleOptionals(){
        WordFrequencyCounterJobService wordFrequencyCounterJobService = new WordFrequencyCounterJobService();

        Set<String> wordset = new HashSet<>();

        wordFrequencyCounterJobService.processLineArray(new String[]{"deine(r,s)","1"},wordset);

        assertThat(wordset.size(),is(3));
        assertThat(wordset.contains("deine"),is(true));
        assertThat(wordset.contains("deiner"),is(true));
        assertThat(wordset.contains("deines"),is(true));
    }

}
