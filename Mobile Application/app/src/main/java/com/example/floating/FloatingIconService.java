package com.example.floating;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.Nullable;

public class FloatingIconService extends Service {

    private WindowManager windowManager;
    private View floatingView;
    private WindowManager.LayoutParams params;

    private int initialX;
    private int initialY;
    private float initialTouchX;
    private float initialTouchY;

    public String photoID;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //This method is called when the service is started, it retrieves the photo ID from the intent passed to it.
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        if (intent != null) {
            photoID = intent.getStringExtra("PHOTO");
        }

        return START_STICKY;
    }

    @SuppressLint({"InflateParams", "ClickableViewAccessibility"})
    @Override
    public void onCreate() {
        super.onCreate();
        final String PHOTO = "PHOTO";
        floatingView = LayoutInflater.from(this).inflate(R.layout.layout_floating_icon, null);

        // Set the layout parameters for the floating icon
        int layoutFlag = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                layoutFlag,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        // Set the initial position of the floating icon
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 0;
        params.y = 0;

        // Add the floating icon to the WindowManager
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        windowManager.addView(floatingView, params);

        // Set touch listener to enable drag functionality and handle click
        floatingView.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // Record the initial position and touch coordinates
                    initialX = params.x;
                    initialY = params.y;
                    initialTouchX = event.getRawX();
                    initialTouchY = event.getRawY();
                    return true;

                case MotionEvent.ACTION_MOVE:
                    // Calculate the new position based on the touch movement
                    int dx = (int) (event.getRawX() - initialTouchX);
                    int dy = (int) (event.getRawY() - initialTouchY);
                    params.x = initialX + dx;
                    params.y = initialY + dy;

                    // Update the floating icon position
                    windowManager.updateViewLayout(floatingView, params);

                    // Check if the icon is dragged to the bottom of the screen
                    int screenHeight = getResources().getDisplayMetrics().heightPixels;
                    int iconHeight = floatingView.getHeight();
                    int bottomThreshold = screenHeight - iconHeight;

                    if (params.y >= bottomThreshold) {
                        // Close the service and remove the floating icon
                        stopSelf();
                    }
                    return true;

                case MotionEvent.ACTION_UP:
                    // Check if the icon was clicked
                    if (Math.abs(event.getRawX() - initialTouchX) < 10 && Math.abs(event.getRawY() - initialTouchY) < 10) {
                        // Start the FloatingWidgetService
                        Intent intent = new Intent(FloatingIconService.this, FloatingWidgetService.class);
                        intent.putExtra(PHOTO,photoID);
                        startService(intent);
                    }
                    return true;
            }
            return false;
        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (floatingView != null) {
            windowManager.removeView(floatingView);
        }
    }
}
