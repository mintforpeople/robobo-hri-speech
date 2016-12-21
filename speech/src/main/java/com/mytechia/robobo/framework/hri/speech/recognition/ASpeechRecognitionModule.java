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

package com.mytechia.robobo.framework.hri.speech.recognition;

import java.util.HashSet;

/**
 * Abstract class that manages listeners
 */
public abstract class ASpeechRecognitionModule implements ISpeechRecognitionModule{
    //The set of listeners
    private HashSet<ISpeechRecognitionListener> listeners;

    //Class constructor
    public ASpeechRecognitionModule(){
        listeners = new HashSet<ISpeechRecognitionListener>();
    }


    @Override
    public void suscribe(ISpeechRecognitionListener listener) {
        listeners.add(listener);
    }

    @Override
    public void unsuscribe(ISpeechRecognitionListener listener) {
        listeners.remove(listener);
    }


    /**
     * Notifies the listener when a phrase is recognized
     * @param phrase The phrase recognized
     * @param timestamp The time when the phrase was recognized
     */
    protected void notifyPhrase(String phrase, Long timestamp){
        for (ISpeechRecognitionListener listener:listeners){
            listener.phraseRecognized(phrase,timestamp);
        }
    }

    /**
     * Notifies the listeners the startup of the module
     */
    protected void notifyStartup(){
        for (ISpeechRecognitionListener listener:listeners){
            listener.onModuleStart();
        }
    }
}
