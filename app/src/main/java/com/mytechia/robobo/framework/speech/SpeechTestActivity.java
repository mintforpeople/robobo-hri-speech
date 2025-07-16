package com.mytechia.robobo.framework.speech;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
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

            Log.d("SpeechTestActivity", "Pitch:"+ productionModule.getPitch());
            Log.d("SpeechTestActivity", "Rate:"+ productionModule.getSpeechRate());

        } catch (ModuleNotFoundException e) {
            e.printStackTrace();
        } catch (InternalErrorException e) {
            throw new RuntimeException(e);
        }
    }
}
