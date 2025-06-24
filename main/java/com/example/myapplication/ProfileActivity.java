package com.example.myapplication;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {
    private static final int PICK_IMAGE = 1;

    private ImageView imageProfile;
    private TextView textAllergies;
    private Button btnSaveProfile;

    private Uri selectedImageUri;
    private DatabaseHelper dbHelper;
    private String currentUser;

    private final String[] allergyOptions = {
            "Pollen","Dust Mites","Pet Dander","Mold","Ragweed",
            "Grass Pollen","Tree Pollen","Shellfish","Peanuts","Milk",
            "Eggs","Soy","Wheat","Nickel","Fragrances","Latex",
            "Cold Air","Humidity","Red Meat"
    };
    private boolean[] checkedItems;
    private final List<Integer> selectedIndices = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        SharedPreferences prefs = getSharedPreferences("session", MODE_PRIVATE);
        currentUser = prefs.getString("username", null);
        if (currentUser == null) {
            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        imageProfile   = findViewById(R.id.imageProfile);
        textAllergies  = findViewById(R.id.textAllergies);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);

        dbHelper    = new DatabaseHelper(this);
        checkedItems = new boolean[allergyOptions.length];

        loadProfile();

        imageProfile.setOnClickListener(v -> {
            Intent pick = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            pick.addCategory(Intent.CATEGORY_OPENABLE);
            pick.setType("image/*");
            startActivityForResult(pick, PICK_IMAGE);
        });

        textAllergies.setOnClickListener(v -> showAllergyDialog());

        btnSaveProfile.setOnClickListener(v -> {
            String csv = textAllergies.getText().toString();
            String uriStr = selectedImageUri != null
                    ? selectedImageUri.toString()
                    : "";
            boolean ok = dbHelper.updateProfile(
                    currentUser, "", csv, uriStr
            );
            Toast.makeText(
                    this,
                    ok ? "Profile saved" : "Save failed",
                    Toast.LENGTH_SHORT
            ).show();
        });
    }

    private void loadProfile() {
        try (Cursor c = dbHelper.getProfile(currentUser)) {
            if (c.moveToFirst()) {
                // Allergies CSV
                String csv = c.getString(1);
                if (csv != null && !csv.isEmpty()) {
                    StringBuilder display = new StringBuilder();
                    for (String s : csv.split("\\s*,\\s*")) {
                        for (int i = 0; i < allergyOptions.length; i++) {
                            if (allergyOptions[i].equalsIgnoreCase(s)) {
                                checkedItems[i] = true;
                                if (!selectedIndices.contains(i)) selectedIndices.add(i);
                                if (display.length() > 0) display.append(", ");
                                display.append(allergyOptions[i]);
                            }
                        }
                    }
                    textAllergies.setText(display.toString());
                }

                String uriStr = c.getString(2);
                if (uriStr != null && !uriStr.isEmpty()) {
                    selectedImageUri = Uri.parse(uriStr);
                    loadImageFromUri(selectedImageUri);
                }
            }
        }
    }

    private void showAllergyDialog() {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("Select Allergies");
        b.setMultiChoiceItems(
                allergyOptions,
                checkedItems,
                (dialog, which, isChecked) -> {
                    checkedItems[which] = isChecked;
                    if (isChecked) selectedIndices.add(which);
                    else selectedIndices.remove(Integer.valueOf(which));
                }
        );
        b.setPositiveButton("OK", (dialog, which) -> {
            StringBuilder sb = new StringBuilder();
            for (int i : selectedIndices) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(allergyOptions[i]);
            }
            textAllergies.setText(
                    sb.length() > 0 ? sb.toString() : "Select your allergies"
            );
        });
        b.setNegativeButton("Cancel", null);
        b.show();
    }

    private void loadImageFromUri(Uri uri) {
        try {
            final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
            getContentResolver().takePersistableUriPermission(uri, takeFlags);

            try (InputStream is = getContentResolver().openInputStream(uri)) {
                Bitmap bmp = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                imageProfile.setImageBitmap(bmp);
                imageProfile.setBackground(null);
            }
        } catch (SecurityException | IOException ex) {
            ex.printStackTrace();
            Toast.makeText(this, "Unable to load image", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, @Nullable Intent data
    ) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE
                && resultCode == Activity.RESULT_OK
                && data != null
                && data.getData() != null
        ) {
            Uri uri = data.getData();
            selectedImageUri = uri;
            loadImageFromUri(uri);
        }
    }
}
