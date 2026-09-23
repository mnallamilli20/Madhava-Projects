package com.example.androidexample;

import android.app.Application;
import android.util.Log;
//added
import android.content.SharedPreferences;

public class MyApp extends Application implements NotificationWebsocket.NotificationListener {

    private boolean notificationsStarted = false;

    @Override
    public void onCreate() {
        super.onCreate();
        NotificationHelper.createChannel(this);

        //added
        SharedPreferences prefs = getSharedPreferences("USER_PREFS", MODE_PRIVATE);
        long userId = prefs.getLong("USER_ID", -1L);

        if(userId != -1L) {
            startNotifications(userId);
        }
    }

    public void startNotifications(long userId) {
        if (userId == -1L) return;

//        if (notificationsStarted) {
//            Log.d("MyApp", "Notifications already started");
//            return;
//        }

        NotificationWebsocket.getInstance().setListener(this);
        NotificationWebsocket.getInstance().connect(userId);
        notificationsStarted = true;

        Log.d("MyApp", "Started notifications for user " + userId);
    }

    public void stopNotifications() {
        NotificationWebsocket.getInstance().disconnect();
        notificationsStarted = false;
    }

    @Override
    public void onNotificationReceived(String message) {
        //added
        Log.d("MyApp", "Notification received: " + message);

        NotificationHelper.showNotification(getApplicationContext(), message);
    }

    @Override
    public void onSocketOpen() {
        Log.d("MyApp", "Socket connected");
    }

    @Override
    public void onSocketClosed() {
        Log.d("MyApp", "Socket closed");
        notificationsStarted = false;

        SharedPreferences prefs = getSharedPreferences("USER_PREFS", MODE_PRIVATE);
        long userId = prefs.getLong("USER_ID", -1L);
        if (userId != -1L) {
            startNotifications(userId);
        }
    }

    @Override
    public void onSocketError(String error) {
        Log.e("MyApp", "Socket error: " + error);
        notificationsStarted = false;

        SharedPreferences prefs = getSharedPreferences("USER_PREFS", MODE_PRIVATE);
        long userId = prefs.getLong("USER_ID", -1L);
        if (userId != -1L) {
            startNotifications(userId);
        }
    }
}