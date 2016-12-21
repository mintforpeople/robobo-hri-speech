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


import com.mytechia.robobo.framework.IModule;

import java.util.Collection;
import java.util.Locale;

/**
 * Intrerface of the robobo text to speech module
 */
public interface ISpeechProductionModule extends IModule {
    Integer PRIORITY_HIGH = 1;
    Integer PRIORITY_LOW = 0;


    /**
     * Says the text through the phone speakers.
     * @param text The text to be said
     * @param priority The priority of the speech (PRIORITY_HIGH, PRIORITY_LOW)
     */
    void sayText(String text, Integer priority);

    /**
     * Sets a new locale for the Text To Speech object
     * @param newloc new Locale to set
     */
    void setLocale(Locale newloc);

    /**
     *  Sets the current voice of the text to speech generator
     *  @param name The name of the voice to use
     *  @throws VoiceNotFoundException, UnsupportedOperationException
     */
    void selectVoice(String name) throws VoiceNotFoundException;


    /**
     *  Sets the current voice of the text to speech generator
     *  @param voice The  voice to use
     *  @throws VoiceNotFoundException, UnsupportedOperationException
     */
    void selectTtsVoice(ITtsVoice voice) throws VoiceNotFoundException;

    /**
     *  Returns a collection of the available voices for text to speech
     *  @return A collection of the available voices
     *  @throws UnsupportedOperationException
     */
    Collection<ITtsVoice> getVoices();

    /**
     * Returns a collection of the voice names
     * @return  A collection of the available voices names
     */
    Collection<String> getStringVoices();
}
