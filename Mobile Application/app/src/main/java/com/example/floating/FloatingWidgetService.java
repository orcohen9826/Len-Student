package com.example.floating;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FloatingWidgetService extends Service {

    private WindowManager windowManager;
    private View floatingView;
    private ImageView imageView;
    private Button closeButton;

    private String imageUrl;
    private boolean isImageLoaded = false;

    public String photoID;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //This method is called when the service is started, it retrieves the photo ID from the intent passed to it.
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        if (intent != null) {
            photoID = intent.getStringExtra("PHOTO"); // Replace "Number" with the appropriate key

        }

        return START_STICKY;
    }

    @SuppressLint("InflateParams")
    @Override
    public void onCreate() {
        //updating the floating picture to the updated photo in the database
        super.onCreate();
        floatingView = LayoutInflater.from(this).inflate(R.layout.layout_floating_widget, null);

        final String PHOTO = "PHOTO";

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("images");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                checker(databaseReference);
            }

            private void checker(DatabaseReference databaseReference) {
                String path = "image" + photoID;
                databaseReference.child(path).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //display the image to the screen
                        imageUrl = snapshot.getValue(String.class);
                        Picasso
                                .get()
                                .load(imageUrl)
                                .memoryPolicy(MemoryPolicy.NO_CACHE)
                                .into(imageView, new Callback() {
                                    @Override
                                    public void onSuccess() {
                                        isImageLoaded = true;
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                    }
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // Find the ImageView in the inflated layout and set its visibility to VISIBLE
        imageView = floatingView.findViewById(R.id.imageView);
        imageView.setVisibility(View.VISIBLE);

        // Find the Close button in the inflated layout and set its OnClickListener
        closeButton = floatingView.findViewById(R.id.close_button);
        closeButton.setOnClickListener(v -> {
            if (isImageLoaded) {
                saveImageToClipboard(imageUrl);
                Intent intent = new Intent(FloatingWidgetService.this, FloatingIconService.class);
                intent.putExtra(PHOTO, photoID);

                startService(intent);
            } else {
                Toast.makeText(FloatingWidgetService.this, "Image is still loading", Toast.LENGTH_SHORT).show();
            }
            stopSelf();
        });

        // Set the layout parameters for the floating view
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        // Add the floating view to the WindowManager
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        windowManager.addView(floatingView, params);

        // Add an onTouchListener to handle dragging the floating view
        floatingView.setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        // Update the position of the floating view
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(floatingView, params);
                        return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (floatingView != null) {
            windowManager.removeView(floatingView);
        }
    }

    private void saveImageToClipboard(String imageUrl) {
        Picasso.get().load(imageUrl).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                try {
                    // Create a unique file name
                    String fileName = "image" + System.currentTimeMillis() + ".png";

                    // Save the image to the app's internal storage
                    File imagePath = new File(getExternalFilesDir(null), fileName);
                    FileOutputStream fos = new FileOutputStream(imagePath);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    fos.close();

                    // Create a URI for the saved image file
                    Uri imageUri = FileProvider.getUriForFile(FloatingWidgetService.this,
                            BuildConfig.APPLICATION_ID + ".provider", imagePath);

                    // Copy the image URI to the clipboard
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newUri(getContentResolver(), "Image", imageUri);
                    clipboard.setPrimaryClip(clip);

                    // Notify the user that the image has been copied to the clipboard
                    Handler mainHandler = new Handler(Looper.getMainLooper());
                    mainHandler.post(() -> Toast.makeText(FloatingWidgetService.this, "Image saved to clipboard", Toast.LENGTH_SHORT).show());
                } catch (IOException e) {
                    e.printStackTrace();
                    Handler mainHandler = new Handler(Looper.getMainLooper());
                    mainHandler.post(() -> Toast.makeText(FloatingWidgetService.this, "Failed to save image to clipboard", Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                // Handle bitmap load failure
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                // Handle bitmap preparation
            }
        });
    }
}
