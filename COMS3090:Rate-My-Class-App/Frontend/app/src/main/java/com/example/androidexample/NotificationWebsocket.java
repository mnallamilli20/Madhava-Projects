package com.example.androidexample;

import android.util.Log;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
//added
import java.util.concurrent.TimeUnit;

public class NotificationWebsocket {

    private static final String WS_BASE_URL = "ws://coms-3090-018.class.las.iastate.edu:8080";

    private static NotificationWebsocket instance;
    private WebSocket webSocket;
    //changed
    private final OkHttpClient client;
    private long connectedUserId = -1L;

    public interface NotificationListener {
        void onNotificationReceived(String message);
        void onSocketOpen();
        void onSocketClosed();
        void onSocketError(String error);
    }

    private NotificationListener listener;

    //changed
    private NotificationWebsocket() {

        //client = new OkHttpClient();
        client = new OkHttpClient.Builder()
                .pingInterval(30, TimeUnit.SECONDS)
                .build();
    }

    public static synchronized NotificationWebsocket getInstance() {
        if (instance == null) {
            instance = new NotificationWebsocket();
        }
        return instance;
    }

    public void setListener(NotificationListener listener) {
        this.listener = listener;
    }

    public void connect(long userId) {
        if (webSocket != null && connectedUserId == userId) {
            Log.d("WS", "Already connected for user " + userId);
            return;
        }

        disconnect();

        connectedUserId = userId;
        String url = WS_BASE_URL + "/notifications/" + userId;

        //added
        Log.d("WS", "Connecting to " + url);

        Request request = new Request.Builder()
                .url(url)
                .build();

        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                //changed
                Log.d("WS", "Connected: " + response);
                if (listener != null) {
                    listener.onSocketOpen();
                }
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                Log.d("WS", "Message: " + text);
                if (listener != null) {
                    listener.onNotificationReceived(text);
                }
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                Log.d("WS", "Closed: " + code + " reason: " + reason);
                if (listener != null) {
                    listener.onSocketClosed();
                }
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                //changed
                //Log.e("WS", "Error: " + t.getMessage());
                String error = "unknown";

                if(t != null && t.getMessage() != null) {
                    error = t.getMessage();
                }
                if(response != null) {
                    error += " | HTTP " + response.code();
                }

                Log.e("WS", "FAILURE: " + error, t);

                if (listener != null) {
                    listener.onSocketError(t.getMessage());
                }
            }
        });
    }

    public void disconnect() {
        if (webSocket != null) {
            webSocket.close(1000, "Disconnected");
            webSocket = null;
        }
        connectedUserId = -1L;
    }
}