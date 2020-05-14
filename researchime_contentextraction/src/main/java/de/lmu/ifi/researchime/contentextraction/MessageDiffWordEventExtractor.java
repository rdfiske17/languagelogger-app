package de.lmu.ifi.researchime.contentextraction;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.lmu.ifi.researchime.contentabstraction.model.ContentUnitEventType;
import de.lmu.ifi.researchime.contentabstraction.model.InputContent;
import de.lmu.ifi.researchime.contentabstraction.model.rawcontent.ContentUnit;
import de.lmu.ifi.researchime.contentextraction.logging.LogHelper;
import de.lmu.ifi.researchime.contentabstraction.model.rawcontent.ContentChangeEvent;
import de.lmu.ifi.researchime.contentextraction.model.event.Event;

import static java.lang.String.join;


public class MessageDiffWordEventExtractor extends AbstractWordExtractor {

    public static final String TAG = "MessageDiffWordEventExtractor";

    public MessageDiffWordEventExtractor() {

    }

    /**
     * extracts word-level content change events from single-character content change events
     * @param allEvents list of input events. Does not include the former state!
     *               E.g. if a text field contains "Hallo" and the user changes it to "Hallo Max",
     * @param initialContent  The text that was in the textfield before this edit, or null if that is unknown. Then it will be guessed.
     *               the events would be "Hallo ","Hallo M", "Hallo Ma","Hallo Max"
     * @return list of high level events. For the example above it would be a list with one item: Word added "Max"
     *         Not possible 100% accurate yet. See {@see WordCategoriesEventExtractorTest} for all supported special cases
     *
     *         TODO adapting to the HLCCE changes, this method has to detect emojis and punctuations as well
     */
    public List<ContentChangeEvent> extractHigherLevelContentChangeEvents(InputContent inputContent, List<Event> allEvents, String initialContent) {
        LogHelper.i(TAG,"extractHigherLevelContentChangeEvents() with "+allEvents.size()+" events");

        // filter to keep only Content Change events
        List<Event> events = new ArrayList<>();
        for(Event event : allEvents){
            if (event instanceof de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent) {
                events.add(event);
            }
        }

        // extract the higher-level content change events
        List<ContentChangeEvent> highLevelEvents = new ArrayList<>();
        // we extract the events: word x added, word x changed to y, word x deleted
        if (events.size() < 2){
            // we need at least 2 events, meaning at least one change
            return new ArrayList<>();
        }


        //we do not know the initial content. assumptions:
        if (initialContent == null) {
            initialContent = ((de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent) events.get(0)).getContent(); // default
            if (((de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent) events.get(0)).getContent().length() == 1) {
                initialContent = ""; // assumption that this one character was added, and the input was empty before
            }
        }
        // TODO any way regarding word edit and removal?

        List<String> wordsBeforeEvent = null;
        //Change done to correct case wherein a single word with punctuation is changed example 'whether.' -> 'Weather.' . split method did not consider
        // '.' as separate word
        //List<String> wordsAfterLastSavedHighlevelEvent = Arrays.asList(initialContent.split(" "));
        List<String> wordsAfterLastSavedHighlevelEvent = splitSentenceIntoWords(initialContent);
        int indexOfRecentlyChangedWord = -1;
        String wordBeforeEdit = ""; // is updated only if a word-edit event completed
        String previouslyEditedWordAfterLastEdit = null;
        String contentBeforeEvent = null; // is updated with every change event
        Integer newWordStartedAtI = null;
        List<String> wordsNewSaved = null; // the loop's wordsNew list, saved over the iterations
        //Hashmaps to keep track of splitted and joined words
        Map<String, List<Object>> splittedWords = new HashMap<String, List<Object>>();
        Map<String, List<Object>> joinedWords = new HashMap<String, List<Object>>();

        for (int i = 0; i<events.size(); i++) {
            contentBeforeEvent = i > 0 ? ((de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent)events.get(i-1)).getContent() : initialContent;
            // reduce multiple spaces in a row to one space character
            wordsBeforeEvent = splitSentenceIntoWords(contentBeforeEvent);
            String contentNew = ((de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent)events.get(i)).getContent().replaceAll("(\\s+)"," ");
            List<String> wordsNew = splitSentenceIntoWords(contentNew);

            // skip space changes
            if (contentNew.replaceAll(" ","").equals(contentBeforeEvent.replaceAll(" ",""))){
                if (contentNew.endsWith(" ") && !contentBeforeEvent.endsWith(" ") && indexOfRecentlyChangedWord == wordsNew.size()-1) {
                    // if space was added to the end => an edit or add of the last word is completed
                    // case 1.1: it is a new word (works only for more than 1 character long words)
                    if (previouslyEditedWordAfterLastEdit != null) {
                        if ("".equals(wordBeforeEdit)) {
                            highLevelEvents.add(new ContentChangeEvent(inputContent, ContentUnitEventType.ADDED,null,new ContentUnit(previouslyEditedWordAfterLastEdit), events.size() > newWordStartedAtI ? events.get(newWordStartedAtI).getTimestamp() : events.get(events.size()-1).getTimestamp()));
                            wordsAfterLastSavedHighlevelEvent = wordsBeforeEvent;
                            previouslyEditedWordAfterLastEdit = null;
                            indexOfRecentlyChangedWord = -1;
                        }
                        // case 2: an existing word was edited
                        else {
                            highLevelEvents.add(new ContentChangeEvent(inputContent, ContentUnitEventType.CHANGED, new ContentUnit(wordBeforeEdit), new ContentUnit(previouslyEditedWordAfterLastEdit), events.get(i-1).getTimestamp()));
                            wordsAfterLastSavedHighlevelEvent = wordsBeforeEvent;
                            previouslyEditedWordAfterLastEdit = null;
                            indexOfRecentlyChangedWord = -1;
                            wordBeforeEdit = "";
                        }
                    }
                    //Modified to match operators
                    // case 1.2 a new word was completed, that has only 1 character, and is a word- or emoji character
                    else if (newWordStartedAtI == i-1 && "".equals(wordBeforeEdit) && (wordsNew.get(wordsNew.size()-1).matches(WORD_AND_EMOJI_MATCHER)|| wordsNew.get(wordsNew.size()-1).matches(OPERATOR_MATCHER))) {
                        // save that 1-character word
                        highLevelEvents.add(new ContentChangeEvent(inputContent, ContentUnitEventType.ADDED,null,new ContentUnit(wordsNew.get(wordsNew.size()-1)), events.size() > newWordStartedAtI ? events.get(newWordStartedAtI).getTimestamp() : events.get(events.size()-1).getTimestamp()));
                        wordsAfterLastSavedHighlevelEvent = wordsBeforeEvent;
                        previouslyEditedWordAfterLastEdit = null;
                        indexOfRecentlyChangedWord = -1;
                        newWordStartedAtI = null;
                    }
                }
                //a new word with a single character is added in between the words.
                else if(indexOfRecentlyChangedWord != -1 && previouslyEditedWordAfterLastEdit == null && "".equals(wordBeforeEdit)){
                    highLevelEvents.add(new ContentChangeEvent(inputContent, ContentUnitEventType.ADDED,null,new ContentUnit(wordsNew.get(indexOfRecentlyChangedWord)), events.size() > newWordStartedAtI ? events.get(newWordStartedAtI).getTimestamp() : events.get(events.size()-1).getTimestamp()));
                    wordsAfterLastSavedHighlevelEvent = wordsBeforeEvent;
                    previouslyEditedWordAfterLastEdit = null;
                    indexOfRecentlyChangedWord = -1;
                    newWordStartedAtI = null;
                }

                //Identify and store splitted strings in a hashmap
                //Consider test case 'split2'
                /* Event:- W1W2W3W4 is splitted as W1 W2W3W4  -> Hashmap entry {W1W2W3W4: W1 W2W3W4}
                * Event:- W2W3W4 is splitted to W2 W3W4 -> Hashmap entry {W1W2W3W4: W1 W2 W3W4} (If the hashmap consists of a key with value equal to new splitted word,
                *    the string is replaced with the splitted value i.e. W2W3W4 is replaced with W2 W3W4 for the key W1W2W3W4
                * else new key->value pair is added
                * Event:- W3W4 is splitted as W3 W4 -> Hashmap entry {W1W2W3W4: W1 W2 W3 W4}
                * */
                if(wordsBeforeEvent.size() < wordsNew.size()){
                    if(wordsNew.size() == wordsBeforeEvent.size() + 1){
                        List<String> oldWords = new ArrayList<>(wordsBeforeEvent);
                        List<String> newWords = new ArrayList<>(wordsNew);
                        //new words are removed from wordsBeforeEvent and the old words are removed from WordsAfterEvent. This retains only the words changed and  respective changes
                        //consider old event 'W1 W2W3W4' -> OldWords are (W1, W2W3W4)
                        //new event 'W1 W2 W3W4' -> NewWords are (W1, W2, W3W4)
                        //After removal of words we have the old word W2W3W4 which is changed and (W2, W3W4) as the respective changes to the word
                        oldWords.removeAll(wordsNew);
                        newWords.removeAll(wordsBeforeEvent);
                        String splittedWord = TextUtils.join("", newWords);

                        if(!oldWords.isEmpty() && splittedWord.equals(oldWords.get(0))) {
                            if(!splittedWords.isEmpty()){
                                for (Map.Entry<String, List<Object>> item : splittedWords.entrySet()) {
                                    if(!item.getValue().isEmpty()){
                                        List<String> previouslySplittedWords = (List<String>) item.getValue().get(0);
                                        if(previouslySplittedWords.contains(splittedWord)){
                                            previouslySplittedWords.remove(splittedWord);
                                            previouslySplittedWords.addAll(newWords);

                                            if(item.getValue().size() > 1){
                                                item.getValue().set(1, events.get(i - 1).getTimestamp());
                                            }

                                            break;
                                        }
                                        else{
                                            List<Object> temp = new ArrayList<>();
                                            temp.add(newWords);
                                            temp.add(events.get(i - 1).getTimestamp());
                                            splittedWords.put(splittedWord, temp);
                                        }
                                    }
                                }
                            }
                            else{
                                List<Object> temp = new ArrayList<>();
                                temp.add(newWords);
                                temp.add(events.get(i - 1).getTimestamp());
                                splittedWords.put(splittedWord, temp);
                            }
                            wordsAfterLastSavedHighlevelEvent = wordsBeforeEvent;
                            previouslyEditedWordAfterLastEdit = null;
                            indexOfRecentlyChangedWord = -1;
                            newWordStartedAtI = null;
                            wordBeforeEdit = "";
                        }
                    }
                }

                //Identify and store combined strings in a hashmap
                //Consider test case 'join1'
                /* Event:- W1 W2 is joined as W1W2  -> Hashmap entry {W1W2: W1 W2}  (Key is the joined string: value are the words before joining)
                 * Event:- W1W2 W3 is joined as W1W2W3 -> Hashmap entry {W1W2W3: W1 W2 W3}
                 * (If the newly joined word contains previously joined word i.e. W1W2W3 contains W1W2,
                 *   then the newly joined word W3 is added to the value set of existing key pair (W1, W2),
                 *   the existing key is removed
                 *   and new key 'W1W2W3' with value (W1 W2 W3) is added to hashmap                 *
                 * else new key->value pair is added
                 * */
                if(wordsBeforeEvent.size() > wordsNew.size()){
                    if(wordsNew.size() == wordsBeforeEvent.size() - 1){
                        List<String> oldWords = new ArrayList<>(wordsBeforeEvent);
                        List<String> newWords = new ArrayList<>(wordsNew);
                        oldWords.removeAll(wordsNew);    //
                        newWords.removeAll(wordsBeforeEvent);
                        String joinedWord = TextUtils.join("", oldWords);
                        if(!newWords.isEmpty() && joinedWord.equals(newWords.get(0))) {
                            if(!joinedWords.isEmpty()){
                                for (Map.Entry<String, List<Object>> item : joinedWords.entrySet()) {
                                    String previouslyJoinedWord = item.getKey();
                                    if(!item.getValue().isEmpty()){
                                        if(joinedWord.contains(previouslyJoinedWord)){
                                            List<String> previousWords = (List<String>) item.getValue().get(0);
                                            joinedWords.remove(previouslyJoinedWord);
                                            oldWords.remove(previouslyJoinedWord);
                                            previousWords.addAll(oldWords);
                                            List<Object> temp = new ArrayList<>();
                                            temp.add(previousWords);
                                            temp.add(events.get(i - 1).getTimestamp());
                                            joinedWords.put(joinedWord, temp);

                                            break;
                                        }
                                        else{
                                            List<Object> temp = new ArrayList<>();
                                            temp.add(oldWords);
                                            temp.add(events.get(i - 1).getTimestamp());
                                            joinedWords.put(joinedWord, temp);
                                        }
                                    }
                                }
                            }
                            else{
                                List<Object> temp = new ArrayList<>();
                                temp.add(oldWords);
                                temp.add(events.get(i - 1).getTimestamp());
                                joinedWords.put(joinedWord, temp);
                            }
                            wordsAfterLastSavedHighlevelEvent = wordsBeforeEvent;
                            previouslyEditedWordAfterLastEdit = null;
                            indexOfRecentlyChangedWord = -1;
                            newWordStartedAtI = null;
                            wordBeforeEdit = "";

                        }
                    }


                }
                continue;
            }


            // a new word was started
            if (wordsBeforeEvent.size() < wordsNew.size()) {
                // Find out at which index that happened:
                for (int j = 0; j<Math.max(wordsBeforeEvent.size(),wordsNew.size()); j++) {
                    if (j > wordsBeforeEvent.size()-1 || j > wordsNew.size()-1 || !wordsBeforeEvent.get(j).equals(wordsNew.get(j))) {
                        // word j did change.
                        if (previouslyEditedWordAfterLastEdit != null && indexOfRecentlyChangedWord != -1 && indexOfRecentlyChangedWord != j && newWordStartedAtI != null) {
                            // if word j is not the same word as in the last event, save the last word
                            highLevelEvents.add(new ContentChangeEvent(inputContent, ContentUnitEventType.ADDED,null,new ContentUnit(previouslyEditedWordAfterLastEdit), events.size() > newWordStartedAtI ? events.get(newWordStartedAtI).getTimestamp() : events.get(events.size()-1).getTimestamp()));
                            wordsAfterLastSavedHighlevelEvent = wordsBeforeEvent;
                            indexOfRecentlyChangedWord = -1;

                            // then reset variables
                            wordBeforeEdit = "";
                            previouslyEditedWordAfterLastEdit = wordsNew.get(j);
                        }
                        else if((previouslyEditedWordAfterLastEdit != null && indexOfRecentlyChangedWord != -1 && indexOfRecentlyChangedWord != j && wordBeforeEdit != null)){
                            highLevelEvents.add(new ContentChangeEvent(inputContent, ContentUnitEventType.CHANGED, new ContentUnit(wordBeforeEdit), new ContentUnit(previouslyEditedWordAfterLastEdit), events.get(i-1).getTimestamp()));
                            wordBeforeEdit = "";
                            previouslyEditedWordAfterLastEdit = wordsNew.get(j);
                        }
                        else if(previouslyEditedWordAfterLastEdit == null && indexOfRecentlyChangedWord != -1 && newWordStartedAtI != null && wordBeforeEdit == null){
                            highLevelEvents.add(new ContentChangeEvent(inputContent, ContentUnitEventType.ADDED,null,new ContentUnit(previouslyEditedWordAfterLastEdit), events.size() > newWordStartedAtI ? events.get(newWordStartedAtI).getTimestamp() : events.get(events.size()-1).getTimestamp()));
                            wordsAfterLastSavedHighlevelEvent = wordsBeforeEvent;
                            indexOfRecentlyChangedWord = -1;
                            wordBeforeEdit = "";
                            previouslyEditedWordAfterLastEdit = wordsNew.get(j);
                        }
                        newWordStartedAtI = i;
                        indexOfRecentlyChangedWord = j;
                        break;
                    }
                }
            }

            //Changed for test case How is the weather today - previously it added whether as removed event and did not add the punctuation change
            // the removal of an existing word finished
            else if (wordsBeforeEvent.size() > wordsNew.size()) {
                int indexOfWordRemoved = -1;
                for (int k = 0; k<Math.max(wordsBeforeEvent.size(),wordsNew.size()); k++) {
                    if (k > wordsBeforeEvent.size()-1 || k > wordsNew.size()-1 || !wordsBeforeEvent.get(k).equals(wordsNew.get(k))) {
                        indexOfWordRemoved = k;
                        break;
                    }
                    else
                        continue;
                }

                if (indexOfRecentlyChangedWord != -1 && indexOfWordRemoved != indexOfRecentlyChangedWord){
                    highLevelEvents.add(new ContentChangeEvent(inputContent, ContentUnitEventType.CHANGED, new ContentUnit(wordBeforeEdit), new ContentUnit(previouslyEditedWordAfterLastEdit), events.get(i-1).getTimestamp()));
                    highLevelEvents.add(new ContentChangeEvent(inputContent, ContentUnitEventType.REMOVED, new ContentUnit(wordsBeforeEvent.get(indexOfWordRemoved)), null, events.get(i-1).getTimestamp()));
                    wordsAfterLastSavedHighlevelEvent = wordsNew;
                    previouslyEditedWordAfterLastEdit = null;
                    indexOfRecentlyChangedWord = -1;
                    wordBeforeEdit = "";



                }else{
                    // a word that is about to be edited turns out to be removed completely
                    if ("".equals(wordBeforeEdit)) { // the case if the first edit is the removal of one-character word
                        wordBeforeEdit = wordsBeforeEvent.get(indexOfWordRemoved);
                        /*for (int j = 0; j<wordsBeforeEvent.size(); j++) {
                            if (j>wordsNew.size()-1 || !wordsBeforeEvent.get(j).equals(wordsNew.get(j))) {
                                wordBeforeEdit = wordsBeforeEvent.get(j);
                                break;
                            }
                        }*/
                    }
                    highLevelEvents.add(new ContentChangeEvent(inputContent, ContentUnitEventType.REMOVED, new ContentUnit(wordBeforeEdit), null, events.get(Math.max(i-1,0)).getTimestamp()));
                    wordsAfterLastSavedHighlevelEvent = wordsNew;
                    previouslyEditedWordAfterLastEdit = null;
                    indexOfRecentlyChangedWord = -1;
                    wordBeforeEdit = "";
                }
            }


            // an existing word was edited / continued
            else if (true) {

                // get index of changed word
                for (int indexOfCurrentlyEditedWord = 0; indexOfCurrentlyEditedWord<Math.max(wordsBeforeEvent.size(),wordsNew.size()); indexOfCurrentlyEditedWord++) {
                    if (!wordsBeforeEvent.get(indexOfCurrentlyEditedWord).equals(wordsNew.get(indexOfCurrentlyEditedWord))) {
                        // indexOfCurrentlyEditedWord is the word that changed!
                        // if this time an other word was edited as before, log the edits of before as high level event
                        if (indexOfRecentlyChangedWord != -1 && indexOfRecentlyChangedWord != indexOfCurrentlyEditedWord && previouslyEditedWordAfterLastEdit != null) {
                            // the edit/create/(remove?) of a word is completed
                            // case 1: this is a new word
                            if ("".equals(wordBeforeEdit)) {
                                highLevelEvents.add(new ContentChangeEvent(inputContent, ContentUnitEventType.ADDED,null,new ContentUnit(previouslyEditedWordAfterLastEdit), events.size() > newWordStartedAtI ? events.get(newWordStartedAtI).getTimestamp() : events.get(events.size()-1).getTimestamp()));
                                wordsAfterLastSavedHighlevelEvent = wordsBeforeEvent;
                                indexOfRecentlyChangedWord = -1;
                            }
                            // case 2: an existing word was edited (TODO also removed?)
                            else {
                                highLevelEvents.add(new ContentChangeEvent(inputContent, ContentUnitEventType.CHANGED, new ContentUnit(wordBeforeEdit), new ContentUnit(previouslyEditedWordAfterLastEdit), events.get(i-1).getTimestamp()));
                                wordsAfterLastSavedHighlevelEvent = wordsBeforeEvent;
                                indexOfRecentlyChangedWord = -1;
                            }
                        }
                        if (indexOfRecentlyChangedWord != indexOfCurrentlyEditedWord){
                            // if a new word-edit starts, save the before-state of that word
                            wordBeforeEdit = wordsBeforeEvent.get(indexOfCurrentlyEditedWord);
                        }

                        indexOfRecentlyChangedWord = indexOfCurrentlyEditedWord;
                        previouslyEditedWordAfterLastEdit = wordsNew.get(indexOfCurrentlyEditedWord);
                        break;
                    }

                }
            }
            wordsNewSaved = wordsNew;
        }
        wordsAfterLastSavedHighlevelEvent = wordsBeforeEvent;
        // save the currently buffered edits
        if ((wordsAfterLastSavedHighlevelEvent.size() < wordsBeforeEvent.size()) && previouslyEditedWordAfterLastEdit != null) {
            // the last unsaved action was a word add
            highLevelEvents.add(new ContentChangeEvent(inputContent, ContentUnitEventType.ADDED, null, new ContentUnit(previouslyEditedWordAfterLastEdit), events.size() > newWordStartedAtI ? events.get(newWordStartedAtI).getTimestamp() : events.get(events.size()-1).getTimestamp()));
        }
        else if (wordsAfterLastSavedHighlevelEvent.size() == wordsBeforeEvent.size()) {
            // the last unsaved action was an edit
            if (previouslyEditedWordAfterLastEdit != null) { // can be null e.g. if the last edit was a space removal
                if (!wordBeforeEdit.isEmpty()) {
                    highLevelEvents.add(new ContentChangeEvent(inputContent, ContentUnitEventType.CHANGED, new ContentUnit(wordBeforeEdit), new ContentUnit(previouslyEditedWordAfterLastEdit), events.get(events.size()-1).getTimestamp()));
                } else {
                    highLevelEvents.add(new ContentChangeEvent(inputContent, ContentUnitEventType.ADDED, null, new ContentUnit(previouslyEditedWordAfterLastEdit), events.size() > newWordStartedAtI ? events.get(newWordStartedAtI).getTimestamp() : events.get(events.size()-1).getTimestamp()));
                }
            }
        }
        else if (wordsAfterLastSavedHighlevelEvent.size() > wordsBeforeEvent.size()) {
            // the last unsaved action was a removal
            highLevelEvents.add(new ContentChangeEvent(inputContent, ContentUnitEventType.REMOVED, new ContentUnit(wordBeforeEdit), null, events.get(events.size()-1).getTimestamp()));
        }

        if (
                wordsNewSaved != null
                        && events != null
                        && previouslyEditedWordAfterLastEdit == null
                        && indexOfRecentlyChangedWord != -1
                        && wordsNewSaved.get(indexOfRecentlyChangedWord).length() <= 2
                        && wordsNewSaved.get(indexOfRecentlyChangedWord).matches(OPERATOR_MATCHER)){
            highLevelEvents.add(new ContentChangeEvent(inputContent, ContentUnitEventType.ADDED, null, new ContentUnit(wordsNewSaved.get(indexOfRecentlyChangedWord)), events.size() > newWordStartedAtI ? events.get(newWordStartedAtI).getTimestamp() : events.get(events.size()-1).getTimestamp()));
        }
        else if (
                wordsNewSaved != null
                        && events != null
                        && newWordStartedAtI != null
                        && newWordStartedAtI == events.size()-1
                        && wordsNewSaved.get(wordsNewSaved.size()-1).length() <= 2
                        && wordsNewSaved.get(wordsNewSaved.size()-1).matches(WORD_AND_EMOJI_MATCHER)){
            highLevelEvents.add(new ContentChangeEvent(inputContent, ContentUnitEventType.ADDED, null, new ContentUnit(wordsNewSaved.get(wordsNewSaved.size()-1)), events.size() > newWordStartedAtI ? events.get(newWordStartedAtI).getTimestamp() : events.get(events.size()-1).getTimestamp()));
        }
        else if (previouslyEditedWordAfterLastEdit == null && (wordsNewSaved != null) && (wordsBeforeEvent.size() < wordsNewSaved.size())){
            highLevelEvents.add(new ContentChangeEvent(inputContent, ContentUnitEventType.ADDED, null, new ContentUnit(wordsNewSaved.get(wordsNewSaved.size()-1)), events.size() > newWordStartedAtI ? events.get(newWordStartedAtI).getTimestamp() : events.get(events.size()-1).getTimestamp()));
        }

        //Verify if there were any high level split events
        if(!splittedWords.isEmpty()){
            for (Map.Entry<String, List<Object>> item : splittedWords.entrySet()){
                boolean wordAdded = false;
                List<Object> temp = item.getValue();
                List<String> tempWords = null;
                if(!temp.isEmpty() && temp.size()>1) {
                    tempWords = (List<String>) temp.get(0);
                    //The splitted strings are matched if initial content.
                    //If the initial content does not contain the string, the high level event is ADDED - see testcase 'AddWordsInBetweenLateSpace'
                    for (String str : tempWords) {
                        if (!initialContent.contains(str)) {
                            highLevelEvents.add(new ContentChangeEvent(inputContent, ContentUnitEventType.ADDED, null, new ContentUnit(str), (Long) temp.get(1)));
                            wordAdded = true;
                        }
                    }
                    if(!wordAdded)
                        highLevelEvents.add(new ContentChangeEvent(inputContent, ContentUnitEventType.SPLITTED, new ContentUnit(item.getKey()), new ContentUnit(TextUtils.join(" ", tempWords)), (Long) temp.get(1)));
                }
            }
        }

        //Verify if there were any high level Join events
        if(!joinedWords.isEmpty()){
            for (Map.Entry<String, List<Object>> item : joinedWords.entrySet()){
                List<Object> temp = item.getValue();
                if(!temp.isEmpty() && temp.size()>1)
                    highLevelEvents.add(new ContentChangeEvent(inputContent, ContentUnitEventType.JOINED, new ContentUnit(TextUtils.join(" ", (List<String>)temp.get(0))), new ContentUnit(item.getKey()), (Long)temp.get(1)));
            }
        }

        return highLevelEvents;
    }

}
