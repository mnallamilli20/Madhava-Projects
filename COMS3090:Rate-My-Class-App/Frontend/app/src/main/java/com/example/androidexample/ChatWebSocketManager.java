package com.example.androidexample;

import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

//added


/**
 * Singleton WebSocketManager instance used for managing WebSocket connections
 * in the Android application.
 *
 * This instance ensures that there is only one WebSocketManager throughout
 * the application's lifecycle, allowing for centralized WebSocket handling.
 */
public class ChatWebSocketManager {

    private static ChatWebSocketManager instance;
    private MyWebSocketClient webSocketClient;
    private ChatWebSocketListener chatWebSocketListener;

    //added vars to remember current room and users
    private String currentUsername;
    private String currentRoom;

    private ChatWebSocketManager() {}

    /**
     * Retrieves a synchronized instance of the WebSocketManager, ensuring that
     * only one instance of the WebSocketManager exists throughout the application.
     * Synchronization ensures thread safety when accessing or creating the instance.
     *
     * @return A synchronized instance of WebSocketManager.
     */
    public static synchronized ChatWebSocketManager getInstance() {
        if (instance == null) {
            instance = new ChatWebSocketManager();
        }
        return instance;
    }

    /**
     * Sets the WebSocketListener for this WebSocketManager instance. The WebSocketListener
     * is responsible for handling WebSocket events, such as received messages and errors.
     *
     * @param listener The WebSocketListener to be set for this WebSocketManager.
     */
    public void setWebSocketListener(ChatWebSocketListener listener) {
        this.chatWebSocketListener = listener;
    }

    /**
     * Removes the currently set WebSocketListener from this WebSocketManager instance.
     * This action effectively disconnects the listener from handling WebSocket events.
     */
    public void removeWebSocketListener() {
        this.chatWebSocketListener = null;
    }

    /**
     * Initiates a WebSocket connection to the specified server URL. This method
     * establishes a connection with the WebSocket server located at the given URL.
     *
     * @param serverUrl The URL of the WebSocket server to connect to.
     */
    public void connectWebSocket(String serverUrl) {
        try {
            URI serverUri = URI.create(serverUrl);
            webSocketClient = new MyWebSocketClient(serverUri);
            webSocketClient.connect();
            Log.d("WebSocket", "Connecting to: " + serverUrl);
        } catch(Exception e) {
            Log.e("WebSocket", "Connection failed", e);
            if(chatWebSocketListener != null) {
                chatWebSocketListener.onWebSocketError(e);
            }
        }
    }

//    public void connectWebSocket(String serverUrl) {
//        try {
//            URI serverUri = URI.create(serverUrl);
//            webSocketClient = new MyWebSocketClient(serverUri);
//            webSocketClient.connect();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
    /*
    connects to websocket server with username + room
     */
//    private void connectWebSocket(String baseUrl, String username, String room) {
//        try {
//        }
////            currentUsername = username;
////            currentRoom = room;
////
////            String finalUrl = baseUrl
////                    + "?username=" + URLEncoder.encode(username, StandardCharsets.UTF_8.toString())
////                    + "&room=" + URLEncoder.encode(room, StandardCharsets.UTF_8.toString());
////
////            URI serverUri = URI.create(finalUrl);
////            webSocketClient = new MyWebSocketClient(serverUri);
////            webSocketClient.connect();
////
////            Log.d("WebSocket", "Connecting to: " + finalUrl);
////        }
////        catch (Exception e) {
////            Log.e("WebSocket", "Connection failed", e);
////            if(webSocketListener != null) {
////                webSocketListener.onWebSocketError(e);
////            }
////        }
//    }

    /**
     * Sends a WebSocket message to the connected WebSocket server. This method allows
     * the application to send a message to the server through the established WebSocket
     * connection.
     *
     * @param message The message to be sent to the WebSocket server.
     */
    public void sendMessage(String message) {
        if(webSocketClient != null && webSocketClient.isOpen()) {
            webSocketClient.send(message);
        }
    }

    /*
    checks if connected
     */
    public boolean isConnected() {
        return webSocketClient != null && webSocketClient.isOpen();
    }

    /*
    gets current username
     */
    public String getCurrentUsername() {
        return currentUsername;
    }

    /*
    gets current room
     */
    public String getCurrentRoom() {
        return currentRoom;
    }

    /**
     * Disconnects the WebSocket connection, terminating the communication with the
     * WebSocket server.
     */
    public void disconnectWebSocket() {
        if (webSocketClient != null) {
            webSocketClient.close();
        }
    }


    /**
     * A private inner class that extends WebSocketClient and represents a WebSocket
     * client instance tailored for specific functionalities within the WebSocketManager.
     * This class encapsulates the WebSocketClient and provides custom behavior or
     * handling for WebSocket communication as needed by the application.
     */
    private class MyWebSocketClient extends WebSocketClient {

        private MyWebSocketClient(URI serverUri) {
            super(serverUri);
        }

        /**
         * Called when the WebSocket connection is successfully opened and a handshake
         * with the server has been completed. This method is invoked to handle the event
         * when the WebSocket connection becomes ready for sending and receiving messages.
         *
         * @param handshakedata The ServerHandshake object containing details about the
         *                      handshake with the server.
         */
        @Override
        public void onOpen(ServerHandshake handshakedata) {
            Log.d("WebSocket", "Connected");
            if (chatWebSocketListener != null) {
                chatWebSocketListener.onWebSocketOpen(handshakedata);
            }
        }

        /**
         * Called when a WebSocket message is received from the server. This method is
         * invoked to handle incoming WebSocket messages and allows the application to
         * process and respond to messages as needed.
         *
         * @param message The WebSocket message received from the server as a string.
         */
        @Override
        public void onMessage(String message) {
            Log.d("WebSocket", "Received message: " + message);
            if (chatWebSocketListener != null) {
                chatWebSocketListener.onWebSocketMessage(message);
            }
        }

        /**
         * Called when the WebSocket connection is closed, either due to a client request
         * or a server-initiated close. This method is invoked to handle the WebSocket
         * connection closure event and provides details about the closure, such as the
         * closing code, reason, and whether it was initiated remotely.
         *
         * @param code   The WebSocket closing code indicating the reason for closure.
         * @param reason A human-readable explanation for the WebSocket connection closure.
         * @param remote A boolean flag indicating whether the closure was initiated remotely.
         *               'true' if initiated remotely, 'false' if initiated by the client.
         */
        @Override
        public void onClose(int code, String reason, boolean remote) {
            Log.d("WebSocket", "Closed");
            if (chatWebSocketListener != null) {
                chatWebSocketListener.onWebSocketClose(code, reason, remote);
            }
        }

        /**
         * Called when an error occurs during WebSocket communication. This method is
         * invoked to handle WebSocket-related errors and allows the application to
         * respond to and log error details.
         *
         * @param ex The Exception representing the WebSocket communication error.
         */
        @Override
        public void onError(Exception ex) {
            Log.d("WebSocket", "Error");
            if (chatWebSocketListener != null) {
                chatWebSocketListener.onWebSocketError(ex);
            }
        }
    }
}
