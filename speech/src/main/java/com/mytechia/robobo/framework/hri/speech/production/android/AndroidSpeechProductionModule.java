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
package com.mytechia.robobo.framework.hri.speech.production.android;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
import android.util.Log;

import com.mytechia.commons.framework.exception.InternalErrorException;
import com.mytechia.robobo.framework.RoboboManager;
import com.mytechia.robobo.framework.exception.ModuleNotFoundException;
import com.mytechia.robobo.framework.hri.speech.production.ASpeechProductionModule;
import com.mytechia.robobo.framework.hri.speech.production.ISpeechProductionModule;
import com.mytechia.robobo.framework.hri.speech.production.ITtsVoice;
import com.mytechia.robobo.framework.hri.speech.production.VoiceNotFoundException;
import com.mytechia.robobo.framework.remote_control.remotemodule.Command;
import com.mytechia.robobo.framework.remote_control.remotemodule.ICommandExecutor;
import com.mytechia.robobo.framework.remote_control.remotemodule.IRemoteControlModule;


/**
 * Implementation of the speech production module
 */
public class AndroidSpeechProductionModule extends ASpeechProductionModule {

    //region VAR
    private TextToSpeech tts = null;
    private Locale loc = null;
    private Context context = null;
    Collection<ITtsVoice> voices = null;
    private String TAG = "AnsdroidSpeechP";
    //endregion

    //region ISpeechProductionModule Methods
    @Override
    /**
     * Says a text through the phone speaker
     * @param text The text to be said
     * @param priority The priority of the phrase, ISpeechProductionModule.PRIORITY_HIGH / LOW
     */
    public void sayText(String text, Integer priority) {

        if (priority == ISpeechProductionModule.PRIORITY_HIGH){
//            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
            tts.speak(text,TextToSpeech.QUEUE_FLUSH,null,"ROBSPEECH");
        }else
        if (priority == ISpeechProductionModule.PRIORITY_LOW){
//            tts.speak(text, TextToSpeech.QUEUE_ADD, null);
            tts.speak(text,TextToSpeech.QUEUE_ADD,null,"ROBSPEECH");

        }
    }

    @Override
    /**
     * Sets a new locale for the Text To Speech object
     * @param newloc new Locale to set
     */
    public void setLocale(Locale newloc){

        loc = newloc;
        tts.setLanguage(loc);
    }

    @Override
    /**
     *  Sets the current voice of the text to speech generator
     *  @param name The name of the voice to use
     *  @throws VoiceNotFoundException
     */
    public void selectVoice(String name) throws VoiceNotFoundException{


        //Iterate over the voices and if the desired voice is found, set it in the tts object

        Voice v = null;
        Collection<Voice> voices = tts.getVoices();



        //Iterate over the collection searching for the required voice
        for (Voice vo : voices) {
            if (vo.getName().equals(name)){
                v = vo;
            }
        }

        //Throw exception if no suitable voice is found
        if (v == null){
            Log.e("TTS","Error: voice "+name+"not found");
            throw new VoiceNotFoundException("Voice "+name+" not found");


        }

        tts.setVoice(v);


    }
    @Override
    /**
     * Sets a voice on the text to speech engine
     * @param voice The  voice to use
     * @throws VoiceNotFoundException
     */
    public void selectTtsVoice(ITtsVoice voice) throws VoiceNotFoundException{


        tts.setVoice(((AndroidTtsVoice) voice).getInternalVoice());


    }

    @Override
    /**
     *  Returns a collection of the available voices for text to speech
     *  @return A collection of the available voices names
     */
    public Collection<String> getStringVoices(){



        Collection<String> results = new ArrayList<String>();

        for (ITtsVoice v : this.voices) {

            results.add(v.getVoiceName());
        }
        return results;



    }
    @Override
    public Collection<ITtsVoice> getVoices(){

        return this.voices;

    }
    //endregion

    //region IModule Methods
    @Override
    /**
     *  Starts the TextToSpeech engine
     *  @param frameworkManager instance of the Robobo framework manager
     *  @throws InternalErrorException
     */

    public void startup(RoboboManager roboboManager) throws InternalErrorException {
        context = roboboManager.getApplicationContext();
        try {
            remoteControlModule = roboboManager.getModuleInstance(IRemoteControlModule.class);
        }catch (ModuleNotFoundException e){
            e.printStackTrace();
        }

        //Default language of the OS
        loc = Locale.getDefault();



        //Creation the TTS object
        tts = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {

                tts.setLanguage(loc);
                voices = new ArrayList<>();
                Collection<Voice> voicesColl = tts.getVoices();
                for (Voice v : voicesColl) {
                    if (!v.isNetworkConnectionRequired()){
                        ITtsVoice ttsV = new AndroidTtsVoice(v);
                        voices.add(ttsV);
                    }

                }
            }

        }
        );




        tts.setPitch(3.5f);
        tts.setSpeechRate(1.8f);
        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String s) {

            }

            @Override
            public void onDone(String s) {
                notifyEndOfSpeech();
            }

            @Override
            public void onError(String s) {

            }
        });



        roboboManager.getModuleInstance(IRemoteControlModule.class).registerCommand("TALK", new ICommandExecutor() {
            @Override
            public void executeCommand(Command c, IRemoteControlModule rcmodule) {
                sayText(c.getParameters().get("text"),PRIORITY_HIGH);
            }
        });




    }


    @Override
    /**
     * Stops the TextToSpeech engine and frees the resources
     * @throws InternalErrorException
     */
    public void shutdown() throws InternalErrorException {
        //Liberación de recursos del text to speech
        tts.shutdown();

    }



    @Override
    public String getModuleInfo() {
        return "Android Speech Production Module";
    }

    @Override
    public String getModuleVersion() {
        return "0.3.1";
    }



    //endregion

}
