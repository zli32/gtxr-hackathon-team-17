package com.google.ar.core.examples.java.augmentedimage;

import static com.google.ar.core.examples.java.augmentedimage.AugmentedImageActivity.RecordAudioRequestCode;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.google.ar.core.examples.java.augmentedimage.databinding.ActivityFileSelectionBinding;
import com.google.ar.core.examples.java.common.helpers.CameraPermissionHelper;

import java.io.File;

public class FileSelection extends AppCompatActivity {
    private static final String TAG = "ChooseFileActivity";
    private File imgFile = null;
    private File xmlFile = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.READ_EXTERNAL_STORAGE }, 2);
        }
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
            checkPermission();
        }

        // ARCore requires camera permissions to operate. If we did not yet obtain runtime
        // permission on Android M and above, now is a good time to ask the user for it.
        if (!CameraPermissionHelper.hasCameraPermission(this)) {
            CameraPermissionHelper.requestCameraPermission(this);
            return;
        }

        setContentView(R.layout.activity_file_selection);

        Button chooseImageButton = findViewById(R.id.choose_image_file);
        Button chooseXMLButton = findViewById(R.id.choose_xml_file);
        Button goToAR = findViewById(R.id.continue_to_ar);

        chooseImageButton.setOnClickListener(listener -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*"); // allow any file type to be selected
            startActivityForResult(intent, 1); // start the file chooser and wait for the result
        });

        chooseXMLButton.setOnClickListener(listener -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*"); // allow any file type to be selected
            startActivityForResult(intent, 2); // start the file chooser and wait for the result
        });

        goToAR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println(imgFile);
                System.out.println(xmlFile);
                if (imgFile == null || xmlFile == null) {
                    return;
                }
                Intent intent = new Intent(FileSelection.this, AugmentedImageActivity.class);
                intent.putExtra("imgFile", imgFile);
                intent.putExtra("xmlFile", xmlFile);
                startActivity(intent);
            }
        });
    }
    @SuppressLint("ObsoleteSdkInt")
    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.RECORD_AUDIO},RecordAudioRequestCode);
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        System.out.println(requestCode);
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                Uri fileUri = data.getData();
                String filePath = fileUri.getPath(); // get the file path from the URI
                System.out.println(filePath);
                imgFile = new File(fileUri.getPath());
                System.out.println(imgFile);
            } else if (requestCode == 2) {
                Uri fileUri = data.getData();
                String filePath = fileUri.getPath(); // get the file path from the URI
                System.out.println(filePath);
                xmlFile = new File(fileUri.getPath());
                System.out.println(xmlFile.isFile());
                // do something with the selected file
            }
        }
    }
}