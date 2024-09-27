package com.example.floating;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 1;

    private TextInputEditText numberEditText;

    final String PHOTO = "PHOTO";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button startButton = findViewById(R.id.start_floating_widget);
        numberEditText = findViewById(R.id.numberEditText);

        startButton.setVisibility(View.GONE);

        // When the start button is clicked, the floating widget service is started and the application is minimized.

        startButton.setOnClickListener(view -> {
                startFloatingWidgetService();
                moveTaskToBack(true); // Minimize the application
        });

        //The following code listens for changes in the numberEditText input field.
        // The start button's visibility is adjusted based on whether the input field is empty or not.
        numberEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Empty implementation
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Empty implementation
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String number = editable.toString().trim();
                if (!number.isEmpty()) {
                    startButton.setVisibility(View.VISIBLE);
                } else {
                    startButton.setVisibility(View.GONE);
                }
            }
        });
    }

    // This method starts the floating widget service by creating an intent,
    // adding the number for the photoID obtained from the numberEditText field as an extra,
    // and starting the service.
    private void startFloatingWidgetService() {
        Intent intent = new Intent(this, FloatingWidgetService.class);
        String number = Objects.requireNonNull(numberEditText.getText()).toString();
        intent.putExtra(PHOTO,number);
        startService(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkOverlayPermission();
    }

    // This method checks if the overlay permission has been granted.
    // If not, it requests the permission from the user by opening the overlay permission settings.
    private void checkOverlayPermission() {
        if (!Settings.canDrawOverlays(this)) {
            // The user has not granted the overlay permission yet.
            // Request the permission.
            Intent intent = new Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName())
            );
            startActivityForResult(intent, REQUEST_CODE);
        }
    }
}