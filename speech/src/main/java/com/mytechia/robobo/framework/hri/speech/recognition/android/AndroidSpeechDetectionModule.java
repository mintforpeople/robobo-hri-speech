package com.mytechia.robobo.framework.hri.speech.recognition.android;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
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


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Properties;

/**
 * Implementation of the Robobo speech detection module using the Vosk-Android library
 */
public class AndroidSpeechDetectionModule extends ASpeechDetectionModule {

    private SpeechRecognizer speechRecognizer;
    private Intent recognizerIntent;
    private Context context;
    private boolean isListening = false;
    private float samplerate = 16000.f;
    private String TAG = "AndroidSpeechDetectionModule";
    private Locale loc = null;

    private VoiceRecognitionCallback callback;

    public interface VoiceRecognitionCallback {
        void onSpeechResult(String result);
        void onError(String error);
    }

    @Override
    public void startup(RoboboManager manager) throws InternalErrorException {
        context = manager.getApplicationContext();
        callback = new VoiceRecognitionCallback() {
            @Override
            public void onSpeechResult(String result) {
                processResult(result);
            }

            @Override
            public void onError(String error) {
                m.log(LogLvl.ERROR, TAG, "Error: " + error);
            }
        };

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

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);

        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, loc);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.getPackageName());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                Log.d(TAG, "Ready for speech");
            }

            @Override
            public void onBeginningOfSpeech() {
                Log.d(TAG, "Speech started");
            }

            @Override
            public void onRmsChanged(float rmsdB) {}

            @Override
            public void onBufferReceived(byte[] buffer) {}

            @Override
            public void onEndOfSpeech() {
                Log.d(TAG, "Speech ended");
            }

            @Override
            public void onError(int error) {
                Log.e(TAG, "Error: " + error);
                if (callback != null) {
                    if (error != SpeechRecognizer.ERROR_NO_MATCH){
                        callback.onError("Error code: " + error);
                    }
                }
                restartListening();  // Attempt to recover from error
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String result = matches.get(0);
                    Log.d(TAG, "Result: " + result);
                    if (callback != null) {
                        callback.onSpeechResult(result);
                    }
                }
                restartListening();
            }

            @Override
            public void onPartialResults(Bundle partialResults) {}

            @Override
            public void onEvent(int eventType, Bundle params) {}
        });

        startListening();
    }

    public void startListening() {
        if (!isListening) {
            isListening = true;
            speechRecognizer.startListening(recognizerIntent);
            Log.d(TAG, "Listening started");
        }
    }

    public void stopListening() {
        isListening = false;
        if (speechRecognizer != null) {
            speechRecognizer.stopListening();
            speechRecognizer.cancel();
            speechRecognizer.destroy();
        }
    }

    private void restartListening() {
        isListening = false;
        startListening();
    }

    @Override
    public void shutdown() throws InternalErrorException {

    }

    @Override
    public String getModuleInfo() {
        return "Speech detection module";
    }

    @Override
    public String getModuleVersion() {
        return "v0.1";
    }

    private void processResult(String s) {
        //Check better iteration options
        if (!doDetection)
            return;
        if (!s.equals("")) notifyPhrase(s, true);
    }
}
