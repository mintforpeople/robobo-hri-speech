/*******************************************************************************
 *
 *   Copyright 2016 Mytech Ingenieria Aplicada <http://www.mytechia.com>
 *   Copyright 2016 Luis Llamas <luis.llamas@mytechia.com>
 *
 *   This file is part of Robobo HRI Modules.
 *
 *   Robobo HRI Modules is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Robobo HRI Modules is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with Robobo HRI Modules.  If not, see <http://www.gnu.org/licenses/>.
 *
 ******************************************************************************/

package com.mytechia.robobo.framework.hri.speech.recognition.pocketsphinx;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Debug;
import android.os.Environment;
import android.util.Log;
import android.widget.TextView;

import com.mytechia.commons.framework.exception.InternalErrorException;
import com.mytechia.robobo.framework.LogLvl;
import com.mytechia.robobo.framework.RoboboManager;
import com.mytechia.robobo.framework.hri.speech.R;
import com.mytechia.robobo.framework.hri.speech.recognition.ASpeechRecognitionModule;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.HashSet;


import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;

import static edu.cmu.pocketsphinx.SpeechRecognizerSetup.defaultSetup;


/**
 * Implementation of the Robobo speech recognition module usng the PocketSphinx library
 */
public class PocketSphinxSpeechRecognitionModule extends ASpeechRecognitionModule implements RecognitionListener {

    //region VAR
    private SpeechRecognizer recognizer;
    private static final String PHRASEFILENAME = "phrases.gram";
    private String TAG = "SpeechRecognitionModule";
    private static final String KEYWORDSEARCH = "KWSEARCH";

    private String threshold = " /1e-1/\n";

    private static final String MOV_SEARCH = "MOVSEARCH";

    private AbstractCollection<String> recognizablePhrases;
    private static final Integer HASHSETSIZE = 128;
    private File phraseFile;
    private File assetsDir;

    private String hyp = "";

    private HashSet<String> searches = new HashSet<String>();

    private Context context;

    private String currentSearch;

    private Boolean hasStarted = false;
    private Boolean paused = false;


    //endregion

    public  PocketSphinxSpeechRecognitionModule(){
        super();
    }

    @Override
    /**
     * Adds a phrase to the collection
     * @param phrase The phrase to be added
     */
    public void addPhrase(String phrase) {


        recognizablePhrases.add(phrase);

    }

    @Override
    /**
     * Removes a phrase from the collection
     * @param phrase The phrase to be removed
     */
    public void removePhrase(String phrase) {

        if(!recognizablePhrases.remove(phrase)){
            m.log(LogLvl.WARNING, "PS_SpeechRecognition","Phrase "+phrase+" not found in the recognizable set");
        }

    }

    @Override
    public void pauseRecognition(){
        paused = true;
        recognizer.stop();
    }


    @Override
    public void resumeRecognition(){
        paused = false;
        recognizer.startListening(currentSearch);//, timeout);
    }


    @Override
    /**
     * Updates the pocketsphinx search with the contents of the recognizable phrases collection.
     * Should be called after addPhrase() and removePhrase()
     */
    public void updatePhrases(){
        PrintWriter writer = null;
        recognizer.stop();

        try {
            //Deletes the old file
            writer = new PrintWriter(phraseFile);
            writer.print("");
            writer.close();
            writer = new PrintWriter(phraseFile);
            writer.print("");
            //Iterates over all the current phrases and adds them to the file
            for (String phrase:recognizablePhrases){
                m.log(TAG,"Adding phrase: "+phrase);
                writer.append(phrase+threshold);

            }
            //Set the keyword search with the new file
            writer.close();
            recognizer.addKeywordSearch(KEYWORDSEARCH,phraseFile);
        } catch (FileNotFoundException e) {
            Log.e("PS_SpeechRecognition", "Phrase file not initialized");
            e.printStackTrace();
        }
        try {
            m.log(TAG,phraseFile.list()[1]);
        }catch (NullPointerException npe){
            m.log(LogLvl.ERROR, TAG, "null array");
        }

        currentSearch = KEYWORDSEARCH;
        recognizer.startListening(KEYWORDSEARCH);//,timeout);



    }

    @Override
    /**
     * Clear all the phrases in the recognizer
     */
    public void cleanPhrases() {
        //Clear the collection
        recognizablePhrases.clear();
        //Update the recognizer
        updatePhrases();

    }

    @Override
    public void startup(final RoboboManager roboboManager) throws InternalErrorException {
        m = roboboManager;
        m.log(TAG,"Startup Recognition Module");

        //Create a new hashset for phrases
        recognizablePhrases = new HashSet<String>(HASHSETSIZE);
        searches.add(KEYWORDSEARCH);
        currentSearch = KEYWORDSEARCH;

        context = roboboManager.getApplicationContext();
        //Get current directory for the app
        File appRootDir = roboboManager.getApplicationContext().getFilesDir();
        //Create a new text file for storing the phrases
        phraseFile = new File(Environment.getExternalStorageDirectory(),PHRASEFILENAME);




        // Recognizer initialization is a time-consuming and it involves IO,
        // so we execute it in async task

        new AsyncTask<Void, Void, Exception>() {
            @Override
            protected Exception doInBackground(Void... params) {
                try {
                    m.log(LogLvl.TRACE, TAG, "AT/----------");
                    m.log(LogLvl.TRACE, TAG, "AT/Start AsyncTask");
                    Assets assets = new Assets(roboboManager.getApplicationContext());

                    m.log(LogLvl.TRACE, TAG,"AT/ "+assets.toString());
                    assetsDir = assets.syncAssets();
                    setupRecognizer(assetsDir);


                } catch (IOException e) {
                    return e;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Exception result) {
                if (result != null) {
                    //throw new InternalErrorException("Could not start recognizer");
                    m.log(LogLvl.ERROR, TAG,"AT/Could not start recognizer");
                    m.log(LogLvl.ERROR, TAG,"AT/Exception: "+result.toString());
                    m.log(LogLvl.TRACE, TAG,"AT/End");
                } else {

                    m.log(LogLvl.DEBUG, TAG,"AT/Starting keyword listener");
                    //Update search and start listening
                    //updatePhrases();
                    m.log(LogLvl.TRACE, TAG,"AT/End");
                    m.log(LogLvl.TRACE, TAG, "AT/----------");
                    hasStarted = true;
                    notifyStartup();
                }
            }
        }.execute();


    }



    @Override
    public void shutdown() throws InternalErrorException {
        if (recognizer != null){
        //Cancel the listening
        recognizer.cancel();
        //Shutdown the recognizer
        recognizer.shutdown();
        //Delete the phrase file
        phraseFile.delete();
        }

    }

    @Override
    public String getModuleInfo() {
        return "PocketSphinx Voice recognition module";
    }

    @Override
    public String getModuleVersion() {
        return "v0.1";
    }

    @Override
    public Boolean hasStarted(){
        return hasStarted;
    }


    private void setupRecognizer(File assetsDir) throws IOException {
        m.log(TAG, "Setting up recognizer");
        // The recognizer can be configured to perform multiple searches
        // of different kind and switch between them



        recognizer = defaultSetup()
                .setAcousticModel(new File(assetsDir, "en-us-ptm"))
                .setDictionary(new File(assetsDir, "cmudict-en-us.dict"))

                        // To disable logging of raw audio comment out this call (takes a lot of space on the device)
                .setRawLogDir(assetsDir)
                .setFloat("-vad_threshold", 3.0)
                        // Threshold to tune for keyphrase to balance between false alarms and misses
                .setKeywordThreshold(1e-45f)

                        // Use context-independent phonetic search, context-dependent is too slow for mobile
                .setBoolean("-allphone_ci", true)

                .getRecognizer();
        recognizer.addListener(this);



    }


    @Override
    public void onBeginningOfSpeech() {

    }

    @Override
    public void onEndOfSpeech() {
        recognizer.stop();
    }

    @Override
    public void onPartialResult(Hypothesis hypothesis) {

        String text = "null";
        if (hypothesis != null){text = hypothesis.getHypstr();}
        if (hypothesis == null || text.equals("null"))
            return;

        hyp = text;
        m.log(LogLvl.TRACE, TAG,"Recognized part "+text);


    }

    @Override
    public void onResult(Hypothesis hypothesis) {
        if ((hypothesis != null)&&(hyp.equals(hypothesis.getHypstr()))) {

            String text = hypothesis.getHypstr();
            //TODO Filtrar por probabilidad

            m.log(LogLvl.TRACE, TAG,"Recognized "+text+" Prob: "+hypothesis.getBestScore());
            long time = System.currentTimeMillis();
            notifyPhrase(text,time);
        }
        else{ m.log(LogLvl.TRACE, TAG,"Recognized nothing");}

        if (! paused){
            recognizer.startListening(currentSearch);//, timeout);
        }

    }


    @Override
    public void onError(Exception e) {

    }

    @Override
    public void onTimeout() {

    }

    //TODO Permitir varias busquedas diferentes?
    public void setGrammarSearch(String searchName, String grammarFileName){

//        if (!searches.contains(searchName)) {
            searches.add(searchName);
            File languageModel = new File(assetsDir, grammarFileName);
            recognizer.stop();
            recognizer.addGrammarSearch(searchName, languageModel);
            recognizer.startListening(searchName);
            currentSearch = searchName;
//        }else{
//            recognizer.stop();
//            recognizer.startListening(searchName);
//            currentSearch = searchName;
//        }

    }

    public void setKeywordSearch(){
        recognizer.stop();
        currentSearch = KEYWORDSEARCH;
        recognizer.startListening(KEYWORDSEARCH);
    }
}