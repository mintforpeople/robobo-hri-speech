package com.mytechia.robobo.framework.speech;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;

import com.mytechia.commons.framework.exception.InternalErrorException;
import com.mytechia.robobo.framework.RoboboManager;
import com.mytechia.robobo.framework.exception.ModuleNotFoundException;
import com.mytechia.robobo.framework.hri.speech.production.ISpeechProductionListener;
import com.mytechia.robobo.framework.hri.speech.production.ISpeechProductionModule;
import com.mytechia.robobo.framework.hri.speech.recognition.ISpeechDetectionModule;
import com.mytechia.robobo.framework.hri.speech.recognition.ISpeechListener;
import com.mytechia.robobo.framework.service.RoboboServiceHelper;



public class SpeechTestActivity extends AppCompatActivity {

    private ISpeechProductionModule productionModule;
    private ISpeechDetectionModule detectionModule;
    private RoboboManager manager;
    private TextView SpeechTextView;

    private Boolean isSpeaking = false;

    private AudioManager audioManager;

    private class SpeechListener implements ISpeechListener{
        @Override
        public void onResult(String s) {
            if (!isSpeaking){
                SpeechTextView.append(s + "\n");
                isSpeaking = true;
                productionModule.sayText(s, ISpeechProductionModule.PRIORITY_LOW);
            }
        }
    }

    private class SpeechProductionListener implements ISpeechProductionListener{
        @Override
        public void onEndOfSpeech() {
            if (isSpeaking){
                isSpeaking = false;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speech_test);
        SpeechTextView = findViewById(R.id.speechTextView);
        RoboboServiceHelper serviceHelper = new RoboboServiceHelper(this, new RoboboServiceHelper.Listener() {
            @Override
            public void onRoboboManagerStarted(RoboboManager roboboManager) {
                manager = roboboManager;
                startapp();
            }

            @Override
            public void onError(Throwable ex) {

            }
        });
        Bundle options = new Bundle();
        serviceHelper.bindRoboboService(options);

    }

    public void startapp(){
        try {
            productionModule = manager.getModuleInstance(ISpeechProductionModule.class);
            detectionModule = manager.getModuleInstance(ISpeechDetectionModule.class);

            SpeechListener listener = new SpeechListener();
            SpeechProductionListener prodListener = new SpeechProductionListener();
            detectionModule.suscribeAny(listener);
            productionModule.suscribe(prodListener);
            detectionModule.toggleDetection(true);

            audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

            if (!audioManager.isBluetoothScoAvailableOffCall()) {
                Log.e("BT", "SCO not available");
                return;
            }

            // Listen for SCO state
            IntentFilter filter = new IntentFilter(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED);
            registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    int state = intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, -1);
                    if (state == AudioManager.SCO_AUDIO_STATE_CONNECTED) {
                        manager.log("BT", "Bluetooth SCO connected");
                        try {
                            detectionModule.shutdown();
                            detectionModule.startup(manager);
                        } catch (InternalErrorException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    else if (state == AudioManager.SCO_AUDIO_STATE_DISCONNECTED) {
                        manager.log("BT", "Bluetooth SCO disconnected");
                        try {
                            detectionModule.shutdown();
                            detectionModule.startup(manager);
                        } catch (InternalErrorException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }, filter);

            manager.log("SpeechTestActivity", "Pitch:"+ productionModule.getPitch());
            manager.log("SpeechTestActivity", "Rate:"+ productionModule.getSpeechRate());

            Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    audioManager.startBluetoothSco();
                    audioManager.setBluetoothScoOn(true);
                }
            }, 20000);

        } catch (ModuleNotFoundException e) {
            e.printStackTrace();
        }
    }
}
