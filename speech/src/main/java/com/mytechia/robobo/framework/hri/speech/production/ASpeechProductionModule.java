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
package com.mytechia.robobo.framework.hri.speech.production;

import com.mytechia.robobo.framework.RoboboManager;
import com.mytechia.robobo.framework.remote_control.remotemodule.IRemoteControlModule;
import com.mytechia.robobo.framework.remote_control.remotemodule.Status;

import java.util.HashSet;

public abstract class ASpeechProductionModule implements ISpeechProductionModule {
    //The set of listeners
    protected static String BUNDLELANGKEY = "lang";
    private HashSet<ISpeechProductionListener> listeners;
    protected RoboboManager m;
    protected IRemoteControlModule remoteControlModule = null;

    //Class constructor
    public ASpeechProductionModule(){
        listeners = new HashSet<ISpeechProductionListener>();
    }


    @Override
    public void suscribe(ISpeechProductionListener listener) {
        listeners.add(listener);
    }

    @Override
    public void unsuscribe(ISpeechProductionListener listener) {
        listeners.remove(listener);
    }


    /**
     * Notifies the listener when speech ends
     */
    protected void notifyEndOfSpeech(){
        for (ISpeechProductionListener listener:listeners){
            listener.onEndOfSpeech();
        }
    if (remoteControlModule != null){
        Status s = new Status("ENDOFSPEECH");
        remoteControlModule.postStatus(s);
    }
    }
}
