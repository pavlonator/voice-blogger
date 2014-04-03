package com.uno.voiceblogger.glass;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.glass.media.CameraManager;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Pavlo Cherkashyn
 */
public class NewPostActivity extends Activity {
    private static final String TAG = "NewPostActivity";

    private static final int SPEECH_REQUEST = 31;
    private static final int TAKE_PICTURE_REQUEST = 32;


    private List<PostEntry> entries = null;
    private TextView textView = null;
    boolean listenForCommand = true;
    boolean pictureCaptureInProgress = false;
    boolean exitInProgress = false;


    StringBuffer currentTextEntry = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentTextEntry = new StringBuffer("");
        entries = new ArrayList<PostEntry>();
        textView = new TextView(this);
        addContentView(textView, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT));
        displaySpeechRecognizer();
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    private void displaySpeechRecognizer() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        startActivityForResult(intent, SPEECH_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            if (isSuccessfulSpeechResult(requestCode, resultCode)) {
                List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                String spokenText = results.get(0);

                if (listenForCommand) {
                    if (spokenText.equalsIgnoreCase("picture")) {
                        showNotification("adding a picture to the post ...");
                        takePicture();
                        pictureCaptureInProgress = true;
                    } else if (spokenText.equalsIgnoreCase("text")) {
                        showNotification("writing some text to the post ...");
                        listenForCommand = false;
                    } else if (spokenText.equalsIgnoreCase("submit")) {
                        showNotification("about to submit the post ...");
                    } else if (spokenText.equalsIgnoreCase("exit")) {
                        finish();
                        exitInProgress = true;
                    } else {
                        showNotification("whoops, did not get that ...");
                    }
                } else {
                    onTextReady(spokenText);
                    listenForCommand = true;

                }
            } else if (isCancelledPictureResult(requestCode, resultCode)) {
                pictureCaptureInProgress = false;
            } else if (isSuccessfulPictureResult(requestCode, resultCode)) {
                pictureCaptureInProgress = false;
                onPictureReady(data);
            }
        } finally {
            try{
                if( !pictureCaptureInProgress && !exitInProgress){
                    displaySpeechRecognizer();
                }
            } catch (Exception e){
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private boolean isCancelledPictureResult(int requestCode, int resultCode) {
        return requestCode == TAKE_PICTURE_REQUEST && resultCode == RESULT_CANCELED;
    }

    private boolean isSuccessfulPictureResult(int requestCode, int resultCode) {
        return requestCode == TAKE_PICTURE_REQUEST && resultCode == RESULT_OK;
    }

    private boolean isSuccessfulSpeechResult(int requestCode, int resultCode) {
        return requestCode == SPEECH_REQUEST && resultCode == RESULT_OK;
    }

    private void onTextReady(String spokenText) {
        currentTextEntry.append(' ').append(spokenText);
        Log.i(TAG, "current text recorded buffer:" + currentTextEntry.toString());
    }

    private void onPictureReady(Intent data) {
        String picturePath = data.getStringExtra(CameraManager.EXTRA_PICTURE_FILE_PATH);
        processPictureWhenReady(picturePath);
        showNotification("Picture has been added");
    }

    private void showNotification(CharSequence text) {
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(this, text, duration);
        toast.show();
    }

    private void processPictureWhenReady(String picturePath) {
    }


    private void takePicture() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, TAKE_PICTURE_REQUEST);
    }

}
