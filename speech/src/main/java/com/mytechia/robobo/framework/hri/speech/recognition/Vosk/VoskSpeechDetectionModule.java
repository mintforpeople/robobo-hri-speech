package com.mytechia.robobo.framework.hri.speech.recognition.Vosk;

import android.content.res.AssetManager;
import android.util.Log;

import com.mytechia.commons.framework.exception.InternalErrorException;
import com.mytechia.robobo.framework.LogLvl;
import com.mytechia.robobo.framework.RoboboManager;
import com.mytechia.robobo.framework.exception.ModuleNotFoundException;
import com.mytechia.robobo.framework.hri.speech.recognition.ASpeechDetectionModule;
import com.mytechia.robobo.framework.hri.speech.recognition.ISpeechListener;
import com.mytechia.robobo.framework.remote_control.remotemodule.IRemoteControlModule;
import com.mytechia.robobo.framework.remote_control.remotemodule.Status;

import org.json.JSONException;
import org.json.JSONObject;

import org.vosk.LibVosk;
import org.vosk.LogLevel;
import org.vosk.Model;
import org.vosk.Recognizer;
import org.vosk.android.RecognitionListener;
import org.vosk.android.SpeechService;
import org.vosk.android.SpeechStreamService;
import org.vosk.android.StorageService;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;

/**
 * Implementation of the Robobo speech detection module using the Vosk-Android library
 */
public class VoskSpeechDetectionModule extends ASpeechDetectionModule implements RecognitionListener {
    private Model model;
    private SpeechService speechService;
    private Recognizer recognizer;
    private float samplerate = 16000.f;
    private String TAG = "VoskSpeechDetectionModule";

    private Locale loc = null;


    @Override
    public void startup(RoboboManager manager) throws InternalErrorException {

        m = manager;
        // Load propreties from file
        Properties properties = new Properties();
        AssetManager assetManager = manager.getApplicationContext().getAssets();
        String lang = manager.getOptions().getString(BUNDLELANGKEY);

        try {
            InputStream inputStream = assetManager.open("speech.properties");
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Load remote module
        try {
            remoteModule = manager.getModuleInstance(IRemoteControlModule.class);
        } catch (ModuleNotFoundException e) {
            remoteModule = null;
            e.printStackTrace();
        }
        if (remoteModule!=null){
            registerCommands();
        }

        if (lang != null) {
            loc = new Locale(lang);
        }else {
            //Default language of the OS
            loc = Locale.getDefault();
        }

        m.log(LogLvl.DEBUG, TAG,"Loaded Locale: " + loc.getLanguage());


        m.log(LogLvl.DEBUG, TAG,"Properties loaded");

        samplerate = Float.parseFloat(properties.getProperty("model_samplerate"));
        m.log(LogLvl.DEBUG, TAG,":   samplerate");


        ///
        StorageService.unpack(m.getApplicationContext(), "model-" + loc.getLanguage(), "model",
                (model) -> {
                    this.model = model;
                    m.log(LogLvl.DEBUG, TAG,":   model " + model);
                    m.log(LogLvl.DEBUG, TAG,"Model loaded");
                    soundServiceStartup();
                },
                (exception) -> m.logError(TAG, exception.getMessage(), exception));
        ///

    }

    private void soundServiceStartup(){
        try {
            recognizer = new Recognizer(model, samplerate);
            m.log(LogLvl.DEBUG, TAG,"Recognizer loaded");
            speechService = new SpeechService(recognizer, samplerate);
            m.log(LogLvl.DEBUG, TAG,"Sound service loaded");
            speechService.startListening(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void shutdown() throws InternalErrorException {
        if (speechService != null) {
            speechService.cancel();
            speechService.shutdown();
        }
    }

    @Override
    public String getModuleInfo() {
        return "Speech detection module";
    }

    @Override
    public String getModuleVersion() {
        return "v0.1";
    }

    @Override
    public void onPartialResult(String s) {
        //processResult(s);
    }

    @Override
    public void onResult(String s) {
        processResult(s);
    }

    @Override
    public void onFinalResult(String s) { processResult(s, true);}

    private void processResult(String s){
        processResult(s, false);
    }

    private void processResult(String s, boolean finalResult){
        //Check better iteration options
        if (!doDetection)
            return;
        try {
            JSONObject jsonObject = new JSONObject(s);
            if(jsonObject.has("text")) {
                String message =  jsonObject.getString("text");
                if (!message.equals("")) notifyPhrase(message, finalResult);
            }

        }catch (JSONException e) {
            m.log(LogLvl.ERROR, TAG,"Couldnt Process Vosk JSON object: " + e.getMessage());
        }
    }

    @Override
    public void onError(Exception e) {
        m.log(LogLvl.ERROR,TAG,e.getMessage());
    }

    @Override
    public void onTimeout() {
        //Check for debug

        m.log(LogLvl.TRACE, TAG, "Vosk Recognizer timed out. Restarting. Time: " + System.currentTimeMillis());
        speechService.cancel();
        soundServiceStartup();
    }

    /*
    // Vosk model find word does not work
    @Override
    public boolean isWordInModel(String word) {
        return (model.vosk_model_find_word(word) > 0);
    }
    */
}
