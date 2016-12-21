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
import com.mytechia.robobo.framework.IModule;
/**
 * Interface of the ROBOBO Speech Recognition Module
 */
public interface ISpeechRecognitionModule extends IModule {
    /**
     * Adds a phrase to the collection
     * @param phrase The phrase to be added
     */
    void addPhrase(String phrase);
    /**
     * Removes a phrase from the collection
     * @param phrase The phrase to be removed
     */
    void removePhrase(String phrase);
    /**
     * Updates the pocketsphinx search with the contents of the recognizable phrases collection.
     * Should be called after addPhrase() and removePhrase()
     */
    void updatePhrases();
    /**
     * Clear all the phrases in the recognizer
     */
    void cleanPhrases();

    /**
     * Pauses the recognition
     */
    void pauseRecognition();

    /**
     * Resumes the keyword search
     */
    void resumeRecognition();

    /**
     * Checks if the recognizer has started
     * @return True if it has started
     */
    Boolean hasStarted();

    /**
     * Switches to the default keyword search
     */
    void setKeywordSearch();

    /**
     * Switches to a grammar based search
     * @param searchName Id of the search
     * @param grammarFileName Name of the file containing the grammar, with extension,
     *                        located on assets/sync
     */
    void setGrammarSearch(String searchName, String grammarFileName);

    /**
     * Suscribes a listener to the speech detection notifications
     * @param listener The listener to be added
     */
    void suscribe(ISpeechRecognitionListener listener);
    /**
     * Unsuscribes a listener from the speech detection notifications
     * @param listener The listener to be removed
     */
    void unsuscribe(ISpeechRecognitionListener listener);

}
