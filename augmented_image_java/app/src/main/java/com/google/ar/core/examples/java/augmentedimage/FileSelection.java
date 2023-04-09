package com.google.ar.core.examples.java.augmentedimage;

import static com.google.ar.core.examples.java.augmentedimage.AugmentedImageActivity.RecordAudioRequestCode;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;

import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
//import androidx.navigation.NavController;
//import androidx.navigation.Navigation;
//import androidx.navigation.ui.AppBarConfiguration;
//import androidx.navigation.ui.NavigationUI;
//import com.google.ar.core.examples.java.augmentedimage.databinding.ActivityFileSelectionBinding;
import com.google.ar.core.examples.java.common.helpers.CameraPermissionHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
            checkPermission();
        }

        // ARCore requires camera permissions to operate. If we did not yet obtain runtime
        // permission on Android M and above, now is a good time to ask the user for it.
        if (!CameraPermissionHelper.hasCameraPermission(this)) {
            CameraPermissionHelper.requestCameraPermission(this);
            return;
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
                try {
                    imgFile = getFile(this, fileUri);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                System.out.println(imgFile.getPath());
                System.out.println(imgFile.isFile());
            } else if (requestCode == 2) {
                Uri fileUri = data.getData();
                try {
                    xmlFile = getFile(this, fileUri);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                System.out.println(xmlFile.getPath());
                System.out.println(xmlFile.isFile());
                // do something with the selected file
            }
        }
    }

    public static File getFile(Context context, Uri uri) throws IOException {
        File destinationFilename = new File(context.getFilesDir().getPath() + File.separatorChar + queryName(context, uri));
        try (InputStream ins = context.getContentResolver().openInputStream(uri)) {
            createFileFromStream(ins, destinationFilename);
        } catch (Exception ex) {
            Log.e("Save File", ex.getMessage());
            ex.printStackTrace();
        }
        return destinationFilename;
    }

    public static void createFileFromStream(InputStream ins, File destination) {
        try (OutputStream os = new FileOutputStream(destination)) {
            byte[] buffer = new byte[4096];
            int length;
            while ((length = ins.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
            os.flush();
        } catch (Exception ex) {
            Log.e("Save File", ex.getMessage());
            ex.printStackTrace();
        }
    }

    private static String queryName(Context context, Uri uri) {
        Cursor returnCursor =
                context.getContentResolver().query(uri, null, null, null, null);
        assert returnCursor != null;
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        returnCursor.moveToFirst();
        String name = returnCursor.getString(nameIndex);
        returnCursor.close();
        return name;
    }
}