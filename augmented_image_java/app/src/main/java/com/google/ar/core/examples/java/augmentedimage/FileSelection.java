package com.google.ar.core.examples.java.augmentedimage;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
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

import java.io.File;

public class FileSelection extends AppCompatActivity {
    private static final String TAG = "ChooseFileActivity";
    private File imgFile = null;
    private File xmlFile = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_selection);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.READ_EXTERNAL_STORAGE }, 2);
        }

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
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (resultCode == RESULT_OK) {
//            if (requestCode == 1) {
//                Uri fileUri = data.getData();
//                String filePath = fileUri.getPath(); // get the file path from the URI
//                System.out.println(filePath);
//                imgFile = new File(fileUri.getPath());
//                System.out.println(imgFile);
//            } else if (resultCode == 2) {
//                Uri fileUri = data.getData();
//                String filePath = fileUri.getPath(); // get the file path from the URI
//                System.out.println(filePath);
//                xmlFile = new File(fileUri.getPath());
//                System.out.println(xmlFile.isFile());
//                // do something with the selected file
//            }
//        }
//    }
}