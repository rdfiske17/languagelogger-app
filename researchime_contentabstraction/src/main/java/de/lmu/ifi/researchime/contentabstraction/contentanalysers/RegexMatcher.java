package de.lmu.ifi.researchime.contentabstraction.contentanalysers;

import android.app.job.JobParameters;

public class RegexMatcher extends AbstractContentEventAnalyser {
    @Override
    public boolean onStartJob(JobParameters params) {
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}
