package com.mytechia.robobo.framework.hri.speech.recognition;


import android.text.TextUtils;

import com.mytechia.robobo.framework.RoboboManager;
import com.mytechia.robobo.framework.remote_control.remotemodule.Command;
import com.mytechia.robobo.framework.remote_control.remotemodule.ICommandExecutor;
import com.mytechia.robobo.framework.remote_control.remotemodule.IRemoteControlModule;
import com.mytechia.robobo.framework.remote_control.remotemodule.Status;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

public abstract class ASpeechDetectionModule implements ISpeechDetectionModule {

    protected static String BUNDLELANGKEY = "lang";
    public HashSet<ISpeechListener> anyListeners;
    public HashMap<String, ISpeechListener> phraselisteners;
    public List<String> remotePhrases;
    protected RoboboManager m;
    protected IRemoteControlModule remoteModule = null;
    protected boolean doDetection = true;
    protected boolean detectAnything = true;

    public ASpeechDetectionModule(){
        anyListeners = new HashSet<ISpeechListener>();
        phraselisteners = new HashMap<String, ISpeechListener>();
        remotePhrases = new ArrayList<String>();
    }

    //public void

    @Override
    public void suscribePhrase(ISpeechListener listener, String phrase) {
        remotePhrases.add(phrase);
        phraselisteners.put(phrase, listener);
    }

    @Override
    public void unsuscribePhrase(ISpeechListener listener, String phrase) {
        remotePhrases.remove(phrase);
        phraselisteners.remove(phrase);
    }

    @Override
    public void suscribeAny(ISpeechListener listener) {
        anyListeners.add(listener);
    }

    @Override
    public void unsuscribeAll(final ISpeechListener listener) {
        anyListeners.remove(listener);
        if(phraselisteners.containsValue(listener)) {
            phraselisteners.values().removeAll(Collections.singleton(listener)); // remove(listener) would only remove the first one
        }
    }

    @Override
    public void toggleDetection(boolean status){
        this.doDetection = status;
    }

    public void toggleDetectPhrases(boolean status){
        this.detectAnything = !status;
    }

    protected void registerCommands(){
        remoteModule.registerCommand("SPEECH-ADD-PHRASE", new ICommandExecutor() {
            @Override
            public void executeCommand(Command c, IRemoteControlModule rcmodule) {
                String phrase = c.getParameters().get("phrase");
                if(!remotePhrases.contains(phrase)){remotePhrases.add(phrase);}

                Status status = new Status("REGISTERED-PHRASES");
                status.putContents("phrases", TextUtils.join(",", remotePhrases));
                remoteModule.postStatus(status);

            }
        });

        remoteModule.registerCommand("SPEECH-REMOVE-PHRASE", new ICommandExecutor() {
            @Override
            public void executeCommand(Command c, IRemoteControlModule rcmodule) {
                String phrase = c.getParameters().get("phrase");
                if(remotePhrases.contains(phrase)){remotePhrases.remove(phrase);}

                Status status = new Status("REGISTERED-PHRASES");
                status.putContents("phrases", TextUtils.join(",", remotePhrases));
                remoteModule.postStatus(status);
            }
        });

        remoteModule.registerCommand("START-SPEECH-DETECTION", new ICommandExecutor() {
            @Override
            public void executeCommand(Command c, IRemoteControlModule rcmodule) {
                toggleDetection(true);
            }
        });
        remoteModule.registerCommand("STOP-SPEECH-DETECTION", new ICommandExecutor() {
            @Override
            public void executeCommand(Command c, IRemoteControlModule rcmodule) {
                toggleDetection(false);
            }
        });

        // Any switches to anything, doesn't delete registered phrases
        remoteModule.registerCommand("SPEECH-DETECT-ANY", new ICommandExecutor() {
            @Override
            public void executeCommand(Command c, IRemoteControlModule rcmodule) {
                toggleDetectPhrases(false);
            }
        });

        // Any switches to anything, doesn't delete registered phrases
        remoteModule.registerCommand("SPEECH-DETECT-PHRASES-ONLY", new ICommandExecutor() {
            @Override
            public void executeCommand(Command c, IRemoteControlModule rcmodule) {
                toggleDetectPhrases(true);
            }
        });
    }

    private boolean isRegisteredPhrase(String message){
        for (String rPhrase : remotePhrases) {
            return message.contains(rPhrase);
        }
        return false;
    }

    protected void notifyPhrase(String message, boolean finalResult){
        for (String key : phraselisteners.keySet()) {
            if (message.contains(key)) {
                phraselisteners.get(key).onResult(message);
            }
        }
        for (ISpeechListener l : anyListeners) {
            l.onResult(message);
        }
        if(detectAnything || (!detectAnything && isRegisteredPhrase(message))){
            if (remoteModule != null && !message.equals("")){
                Status st = new Status("SPEECH");
                st.putContents("message", message);
                st.putContents("final", finalResult + "");
                remoteModule.postStatus(st);
            }
        }
    }
}
