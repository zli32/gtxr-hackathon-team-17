package com.google.ar.core.examples.java.augmentedimage;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.google.ar.core.examples.java.augmentedimage.databinding.ActivityFileSelectionBinding;

public class FileSelection extends AppCompatActivity {
    private static final String TAG = "ChooseFileActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_selection);
    }

    int requestCode = 1;

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Context context = getApplicationContext();

        if (resultCode == Activity.RESULT_OK) {
            if (data == null) {
                return;
            }
            Uri uri = data.getData();
            Toast.makeText(context, uri.getPath(), Toast.LENGTH_SHORT).show();
        }
    }

    public void openFileChooser(View view) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("/");
        startActivityForResult(Intent.createChooser(intent, "Select a file"), requestCode);
    }
}