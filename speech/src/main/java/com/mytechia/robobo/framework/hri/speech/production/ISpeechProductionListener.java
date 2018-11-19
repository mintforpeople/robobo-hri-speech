package com.mytechia.robobo.framework.hri.speech.production;

/**
 * Created by luis on 28/6/17.
 */

/**
 * Listener for notifications on speech production events
 */
public interface ISpeechProductionListener {
    /**
     * Notifies when a phrase ends
     */
    public void onEndOfSpeech();
}
