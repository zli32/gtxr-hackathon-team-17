/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chatpcb.java.augmentedimage;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.google.ar.core.Anchor;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.AugmentedImage;
import com.google.ar.core.AugmentedImageDatabase;
import com.google.ar.core.Camera;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.Session;
import com.chatpcb.java.augmentedimage.rendering.AugmentedImageRenderer;
import com.chatpcb.java.common.helpers.CameraPermissionHelper;
import com.chatpcb.java.common.helpers.DisplayRotationHelper;
import com.chatpcb.java.common.helpers.FullScreenHelper;
import com.chatpcb.java.common.helpers.SnackbarHelper;
import com.chatpcb.java.common.helpers.TrackingStateHelper;
import com.chatpcb.java.common.rendering.BackgroundRenderer;
import com.chatpcb.java.xmlparser.BoardDto;
import com.chatpcb.java.xmlparser.BoardParser;
import com.chatpcb.java.xmlparser.BoardPartDto;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException;

import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import javax.xml.parsers.ParserConfigurationException;

/**
 * This app extends the HelloAR Java app to include image tracking functionality.
 *
 * <p>In this example, we assume all images are static or moving slowly with a large occupation of
 * the screen. If the target is actively moving, we recommend to check
 * AugmentedImage.getTrackingMethod() and render only when the tracking method equals to
 * FULL_TRACKING. See details in <a
 * href="https://developers.google.com/ar/develop/java/augmented-images/">Recognize and Augment
 * Images</a>.
 */
public class AugmentedImageActivity extends AppCompatActivity implements GLSurfaceView.Renderer {
  private static final String TAG = AugmentedImageActivity.class.getSimpleName();

  // Rendering. The Renderers are created here, and initialized when the GL surface is created.
  private GLSurfaceView surfaceView;
  private ImageView fitToScanView;
  private View loadingSign;
  private RequestManager glideRequestManager;

  private boolean installRequested;

  private String voiceResult;

  private boolean newChip;

  File boardFile = null;

  File imgFile = null;

  //arbitrary instantiation
  private BoardDto boardInfo = null;

  private Map<String, BoardPartDto> boardPartMap = null;

  private BoardPartDto boardPartInfo = null;

  private Session session;
  private final SnackbarHelper messageSnackbarHelper = new SnackbarHelper();
  private DisplayRotationHelper displayRotationHelper;
  private final TrackingStateHelper trackingStateHelper = new TrackingStateHelper(this);

  private final BackgroundRenderer backgroundRenderer = new BackgroundRenderer();
  private final AugmentedImageRenderer augmentedImageRenderer = new AugmentedImageRenderer();

  private boolean shouldConfigureSession = false;

  // Augmented image configuration and rendering.
  // Load a single image (true) or a pre-generated image database (false).
  private final boolean useSingleImage = true;

  private BoardParser parser;
  // Augmented image and its associated center pose anchor, keyed by index of the augmented image in
  // the
  // database.
  private final Map<Integer, Pair<AugmentedImage, Anchor>> augmentedImageMap = new HashMap<>();

  // debug overlay, disable for real build
  private TextView debugOverlay;

  private TextView chipInformation;

  public static final Integer RecordAudioRequestCode = 1;
  private SpeechRecognizer speechRecognizer;
  static HashMap<String, Integer> numbers= new HashMap<String, Integer>();

  static {
    numbers.put("zero", 0);
    numbers.put("one", 1);
    numbers.put("two", 2);
    numbers.put("three", 3);
    numbers.put("four", 4);
    numbers.put("five", 5);
    numbers.put("six", 6);
    numbers.put("seven", 7);
    numbers.put("eight", 8);
    numbers.put("nine", 9);
    numbers.put("ten", 10);
    numbers.put("eleven", 11);
    numbers.put("twelve", 12);
    numbers.put("thirteen", 13);
    numbers.put("fourteen", 14);
    numbers.put("fifteen", 15);
    numbers.put("sixteen", 16);
    numbers.put("seventeen", 17);
    numbers.put("eighteen", 18);
    numbers.put("nineteen", 19);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    boardFile = (File) getIntent().getSerializableExtra("xmlFile");
    imgFile = (File) getIntent().getSerializableExtra("imgFile");
    setContentView(R.layout.activity_main);
    parser = new BoardParser(boardFile);

    try {
      if (!parser.parseBoard()) { //makes sure that the parsing worked
        System.out.println("Failed to parse");
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    surfaceView = findViewById(R.id.surfaceview);
    displayRotationHelper = new DisplayRotationHelper(/*context=*/ this);

    // Set up renderer.
    surfaceView.setPreserveEGLContextOnPause(true);
    surfaceView.setEGLContextClientVersion(2);
    surfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0); // Alpha used for plane blending.
    surfaceView.setRenderer(this);
    surfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    surfaceView.setWillNotDraw(false);

    fitToScanView = findViewById(R.id.image_view_fit_to_scan);
    loadingSign = findViewById(R.id.progressBar);
    glideRequestManager = Glide.with(this);
    glideRequestManager
        .load(Uri.parse("file:///android_asset/fit_to_scan.png"))
        .into(fitToScanView);

    installRequested = false;
    // setup speech to text
    setupSpeech();

  }

  @SuppressLint("ClickableViewAccessibility")
  private void setupSpeech() {
    debugOverlay = findViewById(R.id.debug_overlay);
    chipInformation = findViewById(R.id.Chip_Information);
    setupSpeechRecognizer();
    startSpeechRecognition();
  }

  @Override
  protected void onDestroy() {
    if (session != null) {
      // Explicitly close ARCore Session to release native resources.
      // Review the API reference for important considerations before calling close() in apps with
      // more complicated lifecycle requirements:
      // https://developers.google.com/ar/reference/java/arcore/reference/com/google/ar/core/Session#close()
      session.close();
      session = null;
    }

    super.onDestroy();
    // Release resources
    speechRecognizer.destroy();
  }

  @Override
  protected void onResume() {
    super.onResume();

    if (session == null) {
      Exception exception = null;
      String message = null;
      try {
        switch (ArCoreApk.getInstance().requestInstall(this, !installRequested)) {
          case INSTALL_REQUESTED:
            installRequested = true;
            return;
          case INSTALLED:
            break;
        }



        session = new Session(/* context = */ this);
      } catch (UnavailableArcoreNotInstalledException
          | UnavailableUserDeclinedInstallationException e) {
        message = "Please install ARCore";
        exception = e;
      } catch (UnavailableApkTooOldException e) {
        message = "Please update ARCore";
        exception = e;
      } catch (UnavailableSdkTooOldException e) {
        message = "Please update this app";
        exception = e;
      } catch (Exception e) {
        message = "This device does not support AR";
        exception = e;
      }

      if (message != null) {
        messageSnackbarHelper.showError(this, message);
        Log.e(TAG, "Exception creating session", exception);
        return;
      }

      shouldConfigureSession = true;
    }

    if (shouldConfigureSession) {
      configureSession();
      shouldConfigureSession = false;
    }

    // Note that order matters - see the note in onPause(), the reverse applies here.
    try {
      session.resume();
    } catch (CameraNotAvailableException e) {
      messageSnackbarHelper.showError(this, "Camera not available. Try restarting the app.");
      session = null;
      return;
    }
    surfaceView.onResume();
    displayRotationHelper.onResume();

    fitToScanView.setVisibility(View.VISIBLE);
  }

  @Override
  public void onPause() {
    super.onPause();
    if (session != null) {
      // Note that the order matters - GLSurfaceView is paused first so that it does not try
      // to query the session. If Session is paused before GLSurfaceView, GLSurfaceView may
      // still call session.update() and get a SessionPausedException.
      displayRotationHelper.onPause();
      surfaceView.onPause();
      session.pause();
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
    super.onRequestPermissionsResult(requestCode, permissions, results);
    if (!CameraPermissionHelper.hasCameraPermission(this)) {
      Toast.makeText(
              this, "Camera permissions are needed to run this application", Toast.LENGTH_LONG)
          .show();
      if (!CameraPermissionHelper.shouldShowRequestPermissionRationale(this)) {
        // Permission denied with checking "Do not ask again".
        CameraPermissionHelper.launchPermissionSettings(this);
      }
      finish();
    }

    if (requestCode == RecordAudioRequestCode && results.length > 0 ){
      if(results[0] == PackageManager.PERMISSION_GRANTED)
        Toast.makeText(this,"Permission Granted",Toast.LENGTH_SHORT).show();
    }
  }

  @Override
  public void onWindowFocusChanged(boolean hasFocus) {
    super.onWindowFocusChanged(hasFocus);
    FullScreenHelper.setFullScreenOnWindowFocusChanged(this, hasFocus);
  }

  @Override
  public void onSurfaceCreated(GL10 gl, EGLConfig config) {
    GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);

    // Prepare the rendering objects. This involves reading shaders, so may throw an IOException.
    try {
      // Create the texture and pass it to ARCore session to be filled during update().
      backgroundRenderer.createOnGlThread(/*context=*/ this);
      augmentedImageRenderer.createOnGlThread(/*context=*/ this);
    } catch (IOException e) {
      Log.e(TAG, "Failed to read an asset file", e);
    }
  }

  @Override
  public void onSurfaceChanged(GL10 gl, int width, int height) {
    displayRotationHelper.onSurfaceChanged(width, height);
    GLES20.glViewport(0, 0, width, height);
  }

  @Override
  public void onDrawFrame(GL10 gl) {
    // Clear screen to notify driver it should not load any pixels from previous frame.
    GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

    if (session == null) {
      return;
    }
    // Notify ARCore session that the view size changed so that the perspective matrix and
    // the video background can be properly adjusted.
    displayRotationHelper.updateSessionIfNeeded(session);

    try {
      session.setCameraTextureName(backgroundRenderer.getTextureId());

      // Obtain the current frame from ARSession. When the configuration is set to
      // UpdateMode.BLOCKING (it is by default), this will throttle the rendering to the
      // camera framerate.
      Frame frame = session.update();
      Camera camera = frame.getCamera();
      // Keep the screen unlocked while tracking, but allow it to lock when tracking stops.
      trackingStateHelper.updateKeepScreenOnFlag(camera.getTrackingState());

      // If frame is ready, render camera preview image to the GL surface.
      backgroundRenderer.draw(frame);

      // Get projection matrix.
      float[] projmtx = new float[16];
      camera.getProjectionMatrix(projmtx, 0, 0.1f, 100.0f);

      // Get camera matrix and draw.
      float[] viewmtx = new float[16];
      camera.getViewMatrix(viewmtx, 0);

      // Compute lighting from average intensity of the image.
      final float[] colorCorrectionRgba = new float[4];
      frame.getLightEstimate().getColorCorrection(colorCorrectionRgba, 0);

      // Visualize augmented images.
      drawAugmentedImages(frame, projmtx, viewmtx, colorCorrectionRgba);
    } catch (Throwable t) {
      // Avoid crashing the application due to unhandled exceptions.
      Log.e(TAG, "Exception on the OpenGL thread", t);
    }
  }

  private void configureSession() {
    Config config = new Config(session);
    config.setFocusMode(Config.FocusMode.AUTO);
    if (!setupAugmentedImageDatabase(config)) {
      messageSnackbarHelper.showError(this, "Could not setup augmented image database");
    }
    session.configure(config);
    try {
      session.resume();
      session.pause();
      session.resume();
    } catch (Exception e) {
      System.out.println(e);
    }
  }

  private void drawAugmentedImages(
      Frame frame, float[] projmtx, float[] viewmtx, float[] colorCorrectionRgba) throws ParserConfigurationException, IOException, SAXException {
    Collection<AugmentedImage> updatedAugmentedImages =
        frame.getUpdatedTrackables(AugmentedImage.class);
    // Iterate to update augmentedImageMap, remove elements swe cannot draw.
    for (AugmentedImage augmentedImage : updatedAugmentedImages) {
      switch (augmentedImage.getTrackingState()) {
        case PAUSED:
          // When an image is in PAUSED state, but the camera is not PAUSED, it has been detected,
          // but not yet tracked.
          // String text = String.format("Detected Image %d", augmentedImage.getIndex());
          //messageSnackbarHelper.showMessage(this, text);
          //messageSnackbarHelper.showMessageForShortDuration(this, text);
          break;

        case TRACKING:
          // Have to switch to UI Thread to update View.
          this.runOnUiThread(
              new Runnable() {
                @Override
                public void run() {
                  fitToScanView.setVisibility(View.GONE);
                  loadingSign.setVisibility(View.GONE);
                }
              });

          // Create a new anchor for newly found images.
          if (!augmentedImageMap.containsKey(augmentedImage.getIndex())) {
            Anchor centerPoseAnchor = augmentedImage.createAnchor(augmentedImage.getCenterPose());
            augmentedImageMap.put(
                augmentedImage.getIndex(), Pair.create(augmentedImage, centerPoseAnchor));
          }
          break;

        case STOPPED:
          augmentedImageMap.remove(augmentedImage.getIndex());
          break;

        default:
          break;
      }
    }


    // Draw all images in augmentedImageMap
    for (Pair<AugmentedImage, Anchor> pair : augmentedImageMap.values()) {
      AugmentedImage augmentedImage = pair.first;
      Anchor centerAnchor = augmentedImageMap.get(augmentedImage.getIndex()).second;
      switch (augmentedImage.getTrackingState()) {
        case TRACKING:
          if (newChip){

            boardInfo = parser.getBoardInfo() ;
            boardPartMap = parser.getBoardPartsInfo(); //list of all board parts

            //search using voice results to select a specific biy
            boardPartInfo = boardPartMap.get(voiceResult);

            if (boardPartInfo != null && boardPartInfo.getDevice_package() != null && boardPartInfo.getMpn() != null) {
              runOnUiThread(() -> chipInformation.setText("Device Package: " + boardPartInfo.getDevice_package() + "\n MPN: " + boardPartInfo.getMpn()));
            }

            newChip = false; //reset flag
          }
          if (boardInfo != null && boardPartInfo != null) {
            augmentedImageRenderer.draw(
                    viewmtx, projmtx, augmentedImage, centerAnchor, colorCorrectionRgba, boardInfo, boardPartInfo);
          }
          break;
        default:
          break;
      }
    }
  }

  private boolean setupAugmentedImageDatabase(Config config) {
    AugmentedImageDatabase augmentedImageDatabase;

    // There are two ways to configure an AugmentedImageDatabase:
    // 1. Add Bitmap to DB directly
    // 2. Load a pre-built AugmentedImageDatabase
    // Option 2) has
    // * shorter setup time
    // * doesn't require images to be packaged in apk.
    if (useSingleImage) {
      Bitmap augmentedImageBitmap = loadAugmentedImageBitmap();
      if (augmentedImageBitmap == null) {
        return false;
      }

      augmentedImageDatabase = new AugmentedImageDatabase(session);
      augmentedImageDatabase.addImage("image_name", augmentedImageBitmap);
      // If the physical size of the image is known, you can instead use:
      //     augmentedImageDatabase.addImage("image_name", augmentedImageBitmap, widthInMeters);
      // This will improve the initial detection speed. ARCore will still actively estimate the
      // physical size of the image as it is viewed from multiple viewpoints.
    } else {
      // This is an alternative way to initialize an AugmenteadImageDatabase instance,
      // load a pre-existing augmented image database.
      try (InputStream is = getAssets().open("pcb/pcb_images.imgdb")) {
        augmentedImageDatabase = AugmentedImageDatabase.deserialize(session, is);
        Log.e(TAG, "Image database successfully loaded.");
      } catch (IOException e) {
        Log.e(TAG, "IO exception loading augmented image database.", e);
        return false;
      }
    }

    config.setAugmentedImageDatabase(augmentedImageDatabase);
    return true;
  }

  private Bitmap loadAugmentedImageBitmap() {
    try (InputStream is = new FileInputStream(imgFile)) {
      return BitmapFactory.decodeStream(is);
    } catch (IOException e) {
      Log.e(TAG, "IO exception loading augmented image bitmap.", e);
    }
    return null;
  }



  private void setupSpeechRecognizer() {
    // Create a new SpeechRecognizer
    speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

    // Set the listener for recognition events
    speechRecognizer.setRecognitionListener(new RecognitionListener() {
      @Override
      public void onReadyForSpeech(Bundle bundle) {
      }

      @Override
      public void onBeginningOfSpeech() {
        debugOverlay.setText("");
        debugOverlay.setHint("Listening...");
        System.out.println("Listening...");
      }

      @Override
      public void onRmsChanged(float v) {

      }

      @Override
      public void onBufferReceived(byte[] bytes) {

      }

      @Override
      public void onEndOfSpeech() {

      }

      @Override
      public void onError(int i) {
        // An error has occurred during recognition
        // Restart the recognition process
          speechRecognizer.startListening(createSpeechRecognizerIntent());
      }

      @Override
      public void onResults(Bundle results) {
        ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        String match = null;
        if (matches != null) {
          for (String m: matches) {
            match = matchToString(m);
            if (match != null) {
              break;
            }
          }
          if (match != null) {
            // match is j1, t15
            voiceResult = match.toUpperCase();
            newChip = true;
            debugOverlay.setText(voiceResult);
            System.out.println(match);
          } else {
            debugOverlay.setText("No match");
            System.out.println("No match");
          }
        } else {
          System.out.println("Matches are empty");
        }

        // Restart the recognition process
        speechRecognizer.startListening(createSpeechRecognizerIntent());


      }

      @Override
      public void onPartialResults(Bundle bundle) {

      }

      @Override
      public void onEvent(int i, Bundle bundle) {

      }
    });
  }

  private String matchToString(String match) {
//        return the match as J3, R7 or null if theres no match

    String s = match.toLowerCase();
    System.out.println("Original match: " + match);
    if (s.contains("you too")) {
      return "u2";
    }
    Pattern youR = Pattern.compile("you ([0-9]+)");
    Matcher youM = youR.matcher(s);
    if (youM.find()) {
      return "u" + youM.group(1);
    }

    Pattern r = Pattern.compile("([a-z][0-9]+)");
    Matcher m = r.matcher(s);
//        make sure matches full string
    if (m.find()) {
      return m.group(0);
    }
//        assume its "[a-z] <number as english word>"
    if (s.matches("^.+[-\\s].+$")) {
      String[] matches = s.split("[-\\s]");
      if (matches.length >= 2) {
        String letter = matches[0];
        if (letter.equals("you")) {
          letter = "u";
        } else {
          letter = letter.substring(0, 1);
        }
        String number = matches[1];
        if (numbers.containsKey(number)) {
          return letter + (Objects.requireNonNull(numbers.get(number))).toString();
        }
        return null;
      }
      return null;
    }
    return null;
  }

  private Intent createSpeechRecognizerIntent() {
    // Create a new Intent to start the speech recognition activity
    Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

    // Specify the language model and offline mode
    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
    return intent;
  }

  private void startSpeechRecognition() {
    // Start the recognition process
    speechRecognizer.startListening(createSpeechRecognizerIntent());
  }

  private void stopSpeechRecognition() {
    // Stop the recognition process
    speechRecognizer.stopListening();
    debugOverlay.setHint("Stopped Listening");
    System.out.println("Stopped.");
  }
}
