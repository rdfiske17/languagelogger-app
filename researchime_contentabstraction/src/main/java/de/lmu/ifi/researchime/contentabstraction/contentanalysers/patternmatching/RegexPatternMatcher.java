package de.lmu.ifi.researchime.contentabstraction.contentanalysers.patternmatching;

import com.raizlabs.android.dbflow.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.lmu.ifi.researchime.base.logging.LogHelper;
import de.lmu.ifi.researchime.contentabstraction.model.rawcontent.ContentUnit;

public class RegexPatternMatcher implements PatternMatcher {

    private final String regex;

    private static final String TAG = "RegexPatternMatcher";

    public RegexPatternMatcher(String regex) {
        this.regex = regex;
    }

    @Override
    public List<String> match(ContentUnit contentUnit) {

        List<String> abstractedActions = new ArrayList<>();

        String s = contentUnit.getContent();
        if (StringUtils.isNullOrEmpty(s)){
            return abstractedActions;
        }
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(s);
        List<String> matchList = new ArrayList<String>();

        while (m.find()) {
            matchList.add(m.group());
        }

        for(int i=0;i<matchList.size();i++){
            LogHelper.i(TAG,i+":"+matchList.get(i));
            abstractedActions.add(matchList.get(i));
        }
        return abstractedActions;
    }
}
