package de.lmu.ifi.researchime.contentabstraction.treetagger;


import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

import de.lmu.ifi.researchime.base.logging.LogHelper;


/**
 * Created by Peter on 28.03.2018. in PhoneStudy project
 */

public class ShellExecuter {

    private final String TAG = "ShellExecuter";

    public ShellExecuter() {

    }

    public void execute(String[] command) {
        try {
            // Executes the command.
            ProcessBuilder pb = new ProcessBuilder(command);
            Process process = pb.start();

            InputStream out = process.getInputStream();
            InputStream errorOut = process.getErrorStream();
//
            String outString = IOUtils.toString(out);
            String errorOutString = IOUtils.toString(errorOut);

            // Waits for the command to finish.
            process.waitFor();


            LogHelper.d(TAG,"EXITVALUE: "+ process.exitValue());
            LogHelper.d(TAG,"INPUTSTREAM:"+ "\n" + outString);
            LogHelper.d(TAG,"OUTPUTSTREAM/ERRORSTREAM:"+ "\n" + errorOutString);


        } catch (IOException e) {
            throw new RuntimeException(e);
        }catch (InterruptedException e){
            throw new RuntimeException(e);
        }
    }
}