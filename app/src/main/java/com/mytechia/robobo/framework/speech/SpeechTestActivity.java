package com.mytechia.robobo.framework.speech;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.mytechia.robobo.framework.RoboboManager;
import com.mytechia.robobo.framework.exception.ModuleNotFoundException;
import com.mytechia.robobo.framework.hri.speech.production.ISpeechProductionModule;
import com.mytechia.robobo.framework.hri.speech.recognition.ISpeechDetectionModule;
import com.mytechia.robobo.framework.service.RoboboServiceHelper;

public class SpeechTestActivity extends AppCompatActivity {

    private ISpeechProductionModule productionModule;
    private ISpeechDetectionModule detectionModule;
    private RoboboManager manager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speech_test);
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
        } catch (ModuleNotFoundException e) {
            e.printStackTrace();
        }
    }
}
