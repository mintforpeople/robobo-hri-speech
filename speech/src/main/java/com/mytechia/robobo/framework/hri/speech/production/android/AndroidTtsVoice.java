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


import android.speech.tts.Voice;

import com.mytechia.robobo.framework.hri.speech.production.ITtsVoice;

import java.util.Locale;

/**
 * Created by luis on 2/5/16.
 */

/**
 * Implementation of the Tts voice interface for the Android TTS module
 */
public class AndroidTtsVoice implements ITtsVoice{


    //Voice object to wrap
    private Voice internalVoice = null;
    //Name of the voice
    private String voiceName = null;
    //Locale of the voice
    private Locale loc = null;
    //Name of the language
    private String voiceLanguage = null;



    public AndroidTtsVoice(Voice vo){
        internalVoice = vo;
        loc = vo.getLocale();
        voiceLanguage = loc.getLanguage();
        voiceName = vo.getName();

    }

    @Override
    public String getVoiceName() {
        return voiceName;
    }

    @Override
    public String getVoiceLanguage() {
        return voiceLanguage;
    }

    @Override
    public String toString() {
        return "TtsVoice{" +
                "voiceName='" + voiceName + '\'' +
                ", voiceLanguage='" + voiceLanguage + '\'' +
                '}';
    }

    public Voice getInternalVoice() {
        return internalVoice;
    }
}
