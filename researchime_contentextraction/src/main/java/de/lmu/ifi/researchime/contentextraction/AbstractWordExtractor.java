package de.lmu.ifi.researchime.contentextraction;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.lmu.ifi.researchime.contentabstraction.model.InputContent;

import de.lmu.ifi.researchime.contentabstraction.model.rawcontent.ContentChangeEvent;
import de.lmu.ifi.researchime.contentextraction.model.event.Event;

/**
 *   - inputs: InputEvents
 *   - outputs: HLCCEs like added words, removed words, or words contained in the message
 */
public abstract class AbstractWordExtractor {

    public static final String WORD_CHARACTERS = "a-zA-Z0-9äöüß'";
    public static final String WORD_AND_EMOJI_MATCHER = "[a-zA-Z0-9äöüß\uD83C-\uDBFF\uDC00-\uDFFF]*";
    public static final String OPERATOR_MATCHER = "[\\*\\+-/%&|]+";
    public static final String EMAIL_MATCHER = "[a-zA-Z0-9äöüß_\\.]+@[A-Za-z]+\\.[A-Za-z]{1,3}";

    public abstract List<ContentChangeEvent> extractHigherLevelContentChangeEvents(InputContent inputContent, List<Event> allEvents, String initialContent);

    /**
     * Splits a sentence into words. What's special about this compared to {@see String.split}:
     * - punctuation marks are treated as words
     * - no space needed between words and punctuation
     * - double spaces are treated as one space
     * @param string
     * @return
     */
    public List<String> splitSentenceIntoWords(String string){

        String pattern = "(?=(" + // make matcher return overlapping matches
                "(?:(?:^|[^"+WORD_CHARACTERS+"])+(["+WORD_CHARACTERS+"]+)(?:[^"+WORD_CHARACTERS+"]|$)+)" + // match word enclosed by non-word character(s)
                "|(?:(?:^|["+WORD_CHARACTERS+"\\s])+([^"+WORD_CHARACTERS+"\\s]+)(?:["+WORD_CHARACTERS+"\\s]|$)+)" + // match punctuations etc. enclosed by word/space(s)
                "))";
        Pattern r = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        Matcher m = r.matcher(string);
        SortedMap<Integer,String> matches = new TreeMap<>();

        while (m.find()) {
            String word = m.group(2) != null ? m.group(2) : m.group(3);
            int wordStart = m.start(2) != -1 ? m.start(2) : m.start(3);

            // duplicate word matches occur:
            // e.g. "Hallo, du da" matches for word "du" with ", du " and " du " -> only save one "du"
            // by using a SortedMap with word start index as key, duplicate words are replaced (but
            // two same words at different index in sentence are kept), and the words are ordered
            matches.put(wordStart, word);
        }

        return new ArrayList<>(matches.values());
    }

}
