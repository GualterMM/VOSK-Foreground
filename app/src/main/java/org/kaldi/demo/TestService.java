package org.kaldi.demo;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import org.kaldi.Assets;
import org.kaldi.KaldiRecognizer;
import org.kaldi.Model;
import org.kaldi.RecognitionListener;
import org.kaldi.SpeechService;
import org.kaldi.Vosk;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.security.Provider;

import static org.kaldi.demo.App.CHANNEL_ID;

/*
TODO: Fix whatever's causing the crashes (Fatal signal 11, code 1). As you can see, this is almost a carbon copy of the KaldiActivity class, albeit a crude one.
I removed any methods I deemed "unnecessary", since I'll be using only the recognizeMicrophone() method and no file recognition or UI elements will be necessary.
I suspect the bug lays on the SetupTask class. As mentioned in the comments on the KaldiActivity class, "Recognizer initialization is a time-consuming and it involves IO,
so we execute it in async task".
Since I don't know if I can initialize the Recognizer in the main activity (DemoActivity) and pass the Model object to this service by an Intent, I set to execute the SetupTask
in this service. However, when the service is called by the activity, the model is most likely not initialized yet, and then I get a segmentation fault error.

I hope I'm correct in my assessment, but I really wouldn't be surprised if I'm not. As I stated, I'm fairly new to Android development, and services have been a tricky subject
for me to learn.

In any case, I wanted to thank you, Nickolay, for the help. Besides the VOSK demo, most Speech Recognition related questions on StackOverflow have been answered by you,
and they've helped me in great lengths for developing my project :)
 */
public class TestService extends Service implements RecognitionListener{

    private static final String TAG = "TestService";

    private Model model;
    private SpeechService speechService;

    @Override
    public void onCreate() {
        super.onCreate();

        new SetupTask(this).execute();


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        recognizeMicrophone();

        Intent notificationIntent = new Intent(this, DemoActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Test Service")
                .setContentText("Check Logcat for transcription")
                .setSmallIcon(R.drawable.ic_mic)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);

        return START_NOT_STICKY;
    }

    private static class SetupTask extends AsyncTask<Void, Void, Exception> {
        WeakReference<TestService> activityReference;

        SetupTask(TestService activity) {
            this.activityReference = new WeakReference<>(activity);
        }

        @Override
        protected Exception doInBackground(Void... params) {
            try {
                Assets assets = new Assets(activityReference.get());
                File assetDir = assets.syncAssets();
                Log.d("KaldiDemo", "Sync files in the folder " + assetDir.toString());

                Vosk.SetLogLevel(0);

                activityReference.get().model = new Model(assetDir.toString() + "/model-android");
            } catch (IOException e) {
                return e;
            }
            return null;
        }

//        @Override
//        protected void onPostExecute(Exception result) {
//            if (result != null) {
//                activityReference.get().setErrorState(String.format(activityReference.get().getString(R.string.failed), result));
//            } else {
//                activityReference.get().setUiState(STATE_READY);
//            }
//        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        if (speechService != null) {
            speechService.cancel();
            speechService.shutdown();
        }

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onPartialResult(String hypothesis) {
        Log.d(TAG, hypothesis);
    }

    @Override
    public void onResult(String hypothesis) {
        Log.d(TAG, hypothesis);
        // resultView.append(hypothesis + "\n");
        if(hypothesis.contains("ajuda")){
            Log.d(TAG, "ajuda detected");
        }

    }

    @Override
    public void onError(Exception e) {
        Log.d(TAG, "onError: " + e.getMessage());
        // setErrorState(e.getMessage());

    }

    @Override
    public void onTimeout() {
        speechService.cancel();
        speechService = null;
        // setUiState(STATE_READY);
    }

    public void recognizeMicrophone() {
        if (speechService != null) {
            // setUiState(STATE_DONE);
            speechService.cancel();
            speechService = null;
        } else {
            // setUiState(STATE_MIC);
            try {
                KaldiRecognizer rec = new KaldiRecognizer(model, 16000.0f);
                speechService = new SpeechService(rec, 16000.0f);
                speechService.addListener(this);
                speechService.startListening();
            } catch (IOException e) {
                Log.d(TAG, e.getMessage());
                // setErrorState(e.getMessage());
            }
        }
    }
}
